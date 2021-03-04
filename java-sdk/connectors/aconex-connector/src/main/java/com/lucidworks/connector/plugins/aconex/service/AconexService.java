package com.lucidworks.connector.plugins.aconex.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.lucidworks.connector.plugins.aconex.client.AconexClient;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.model.*;
import com.lucidworks.connector.plugins.aconex.service.http.AconexHttpClient;
import com.lucidworks.connector.plugins.aconex.service.http.AconexHttpClientOptions;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.lucidworks.connector.plugins.aconex.model.Constants.*;
import static com.lucidworks.connector.plugins.aconex.service.rest.RestApiUriBuilder.buildDocumentViewerUri;

public class AconexService implements AconexClient {
    private static final Logger logger = LoggerFactory.getLogger(AconexService.class);

    private final AconexHttpClient client;
    private final AconexConfig config;
    private final LoadingCache<String, ProjectList> projectListCache;
    private final String apiEndpoint;
    private SearchResultsStats stats = new SearchResultsStats();

    public AconexService(AconexConfig config) {
        this.config = config;
        this.client = setClient(config);
        this.apiEndpoint = client.getApiEndpoint();
        this.projectListCache = getProjectsCache(apiEndpoint);

        if (config.properties().projects() != null || !config.properties().projects().isEmpty()) {
            this.stats.setProjectIds(config.properties().projects());
        } else {
            this.stats.setProjectIds(getProjectIds());
        }
    }

    @Override
    public Map<String, Map<String, Object>> getDocuments() {
        return getDocuments(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
    }

    @Override
    public Map<String, Map<String, Object>> getDocuments(int pageNumber, int pageSize) {
        Map<String, Map<String, Object>> content = new HashMap<>();
        List<String> projectIds = config.properties().projects();

        if (projectIds.isEmpty()) {
            logger.warn("No project selected in configuration. All projects will be crawled.");
            projectIds = getProjectIds();
        }
        this.stats.setProjectIds(projectIds);
        this.stats.setTotalProjects(projectIds.size());

        for (String projectId : projectIds) {
            content.putAll(getDocumentsByProject(projectId, pageNumber, pageSize));
        }

        return content;
    }

    @Override
    public SearchResultsStats getSearchResultsStats() {
        return stats;
    }

    @Override
    public Map<String, Map<String, Object>> getDocumentsByProject(String projectId, int pageNumber, int pageSize) {
        Map<String, Map<String, Object>> content = new HashMap<>();
        String documentXmlResponse = client.getDocuments(projectId, pageNumber, pageSize);

        if (documentXmlResponse == null) {
            return content;
        }

        List<Document> documents = getDocumentsFromXML(documentXmlResponse);
        logger.info("{} documents crawled from project:{}", documents.size(), projectId);
        Map<String, Object> document;

        for (Document d : documents) {
            byte[] doc = client.getDocument(projectId, d.getId());

            if (doc != null) {
                document = parseDocument(doc);

                if (document != null && !document.isEmpty()) {
                    document.put("url", buildDocumentViewerUri(projectId, d.getId()));
                    document.put(TYPE_FIELD, "document");
                    document.put(PROJECT_ID_FIELD, projectId);
                    document.put(DOCUMENT_ID_FIELD, d.getId());
                    content.put(projectId + ":" + d.getId(), document);
                }
            }
        }

        return content;
    }

    @Override
    public List<String> getProjectIds() {
        return getProjects().stream().map(Project::getProjectID).collect(Collectors.toList());
    }

    private Map<String, Object> parseDocument(byte[] body) {
        Map<String, Object> content = new HashMap<>();
        try {
            Parser parser = new AutoDetectParser();
            // No limit; Your document contained more than 100000 characters, and so your requested limit has been reached. To receive the full text of the document.
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            ParseContext parseContext = new ParseContext();
            InputStream targetStream = new ByteArrayInputStream(body);

            // parsing the document
            parser.parse(targetStream, handler, metadata, parseContext);

            //getting the content of the document
            content.put("body", handler.toString());

            //getting metadata of the document
            String[] metadataNames = metadata.names();
            for (String name : metadataNames) {
                content.put(name, metadata.get(name));
            }
        } catch (IOException | SAXException | TikaException e) {
            logger.error(e.getMessage());
        }

        return content;
    }

    private List<Document> getDocumentsFromXML(String xml) {
        List<Document> documents = new ArrayList<>();
        try {
            List<String> fileTypes = config.properties().fileTypes();
            XmlMapper xmlMapper = new XmlMapper();
            RegisterSearch registerSearch = xmlMapper.readValue(xml, RegisterSearch.class);
            SearchResults result = registerSearch.getSearchResults();

            if (result == null) {
                logger.warn("Document search results empty for project.");
            } else {
                documents = result.getDocuments();
                setStats(registerSearch);

                if (fileTypes != null && !fileTypes.isEmpty()) {
                    logger.info("Applying file type [{}] document filter", fileTypes);
                    documents.removeIf(doc -> !fileTypes.contains(doc.getFileType().toLowerCase()));
                } else {
                    // logger.info("Applying image file type documents");
                    // documents.removeIf(doc -> IMAGE_FILE_TYPE.contains(doc.getFileType().toLowerCase()));
                    documents.removeIf(doc -> !DOC_FILE_TYPE.contains(doc.getFileType().toLowerCase()));
                }
            }

        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }

        return documents;
    }

    private LoadingCache<String, ProjectList> getProjectsCache(String apiEndpoint) {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build(new CacheLoader<>() {
                    @Override
                    public ProjectList load(String apiEndpoint) throws Exception {
                        return client.getProjectList(apiEndpoint);
                    }
                });
    }

    private List<Project> getProjects() {
        try {
            ProjectList projectList = projectListCache.get(apiEndpoint);
            return projectList.getSearchResults();
        } catch (ExecutionException e) {
            throw new InternalError("Could not load project instance " + apiEndpoint, e.getCause());
        }
    }

    private AconexHttpClient setClient(AconexConfig config) {
        return new AconexHttpClient(new AconexHttpClientOptions(
                config.properties().host(),
                config.properties().auth().basic().username(),
                config.properties().auth().basic().password(),
                config.properties().timeout().connectTimeoutMs()
        ));
    }

    public SearchResultsStats getStats() {
        return stats;
    }

    public void setStats(SearchResultsStats stats) {
        this.stats = stats;
    }

    public void setStats(RegisterSearch registerSearch) {
        this.stats = new SearchResultsStats(registerSearch);
    }
}
