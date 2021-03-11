package com.lucidworks.connector.plugins.aconex.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.lucidworks.connector.plugins.aconex.client.AconexClient;
import com.lucidworks.connector.plugins.aconex.client.http.AconexHttpClient;
import com.lucidworks.connector.plugins.aconex.client.http.AconexHttpClientOptions;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.lucidworks.connector.plugins.aconex.model.Constants.*;
import static com.lucidworks.connector.plugins.aconex.client.rest.RestApiUriBuilder.buildDocumentViewerUri;

public class AconexService implements AconexClient {
    private static final Logger logger = LoggerFactory.getLogger(AconexService.class);

    private final AconexHttpClient client;
    private final AconexConfig config;
    private final LoadingCache<String, ProjectList> projectListCache;
    private final String hostname;
    private SearchResultsStats stats = new SearchResultsStats();

    public AconexService(AconexConfig config) {
        this.config = config;
        this.client = setClient(config);
        this.hostname = config.properties().host();
        this.projectListCache = getProjectsCache(hostname);
        this.stats.setProjectIds(getProjectIds());
    }

    @Override
    public Map<String, Map<String, Object>> getDocuments() {
        return getDocuments(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
    }

    @Override
    public Map<String, Map<String, Object>> getDocuments(int pageNumber, int pageSize) {
        Map<String, Map<String, Object>> content = new HashMap<>();
        List<String> projectIds = config.properties().project().projects();

        if (projectIds.isEmpty()) {
            logger.info("No project selected in configuration. All projects will be crawled.");
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
        logger.info("{} documents available in project:{}/{}", documents.size(), projectId, pageNumber);
        Map<String, Object> document;

        for (Document d : documents) {
            logger.info("doc:{}", d.getId());
            byte[] doc = client.getDocument(projectId, d.getId());

            if (doc != null) {
                document = parseDocument(doc);

                if (document != null && !document.isEmpty()) {
                    document.put("url", buildDocumentViewerUri(projectId, d.getId()));
                    document.put(TYPE_FIELD, "document");
                    document.put(PROJECT_NAME_FIELD, projectId); //TODO: Fix this
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

    public Map<String, Map<String, Object>> getPagedDoc() {
        int pageNumber = 1;
        Map<String, Map<String, Object>> content = new HashMap<>();
        List<String> ids = getProjectIds();
        for (String id : ids) {
            int pageSize = config.properties().limit().pageSize();
            content = getDocumentsByProject(id, pageNumber, pageSize);
            SearchResultsStats results = getSearchResultsStats();
            logger.info(results.toString());

            while (results.getTotalPages() > results.getCurrentPage()) {
                logger.info("stats:{}", results);

                content = getDocumentsByProject(id, ++pageNumber, pageSize);
                results = getSearchResultsStats();
            }
        }

        return content;
    }

    private Map<String, Object> parseDocument(byte[] body) {
        Map<String, Object> content = new HashMap<>();
        /*try {
            Parser parser = new AutoDetectParser();
            // No limit; Your document contained more than 100000 characters, and so your requested limit has been reached. To receive the full text of the document.
            BodyContentHandler handler = new BodyContentHandler(config.properties().limit().write());
            Metadata metadata = new Metadata();
            ParseContext parseContext = new ParseContext();
            InputStream targetStream = new ByteArrayInputStream(body);

            // parsing the document
            parser.parse(targetStream, handler, metadata, parseContext);

            //getting the content of the document
            content.put("body", handler.toString());

            if(config.properties().limit().includeMetadata()) {
                //getting metadata of the document
                String[] metadataNames = metadata.names();
                for (String name : metadataNames) {
                    content.put(name.replace(":", "_"), metadata.get(name));
                }
            }
        } catch (IOException | SAXException | TikaException e) {
            logger.error(e.getMessage());
        }*/

        return content;
    }

    private List<Document> getDocumentsFromXML(String xml) {
        List<Document> documents = new ArrayList<>();
        try {
            Set<String> includedFileExtensions = config.properties().limit().includedFileExtensions();
            Set<String> excludedFileExtensions = config.properties().limit().excludedFileExtensions();
            int maxFileSize = config.properties().limit().maxSizeBytes();
            int minFileSize = config.properties().limit().minSizeBytes();

            XmlMapper xmlMapper = new XmlMapper();
            RegisterSearch registerSearch = xmlMapper.readValue(xml, RegisterSearch.class);
            SearchResults result = registerSearch.getSearchResults();

            if (result == null) {
                logger.warn("Document search results empty for project.");
            } else {
                documents = result.getDocuments();
                setStats(registerSearch);

                logger.info("{} files returned", documents.size());

                if (includedFileExtensions != null && !includedFileExtensions.isEmpty()) {
                    logger.info("Applying included file type [{}] document filter", includedFileExtensions);
                    documents.removeIf(doc -> !includedFileExtensions.contains(doc.getFileType().toLowerCase()));
                } else if (excludedFileExtensions != null && !excludedFileExtensions.isEmpty()) {
                    logger.info("Applying excluded file type [{}] document filter", excludedFileExtensions);
                    documents.removeIf(doc -> excludedFileExtensions.contains(doc.getFileType().toLowerCase()));
                }

                if (maxFileSize > 0) {
                    logger.info("Applying max file size [{}] document filter", maxFileSize);
                    documents.removeIf(doc -> doc.getFileSize() > maxFileSize);
                }

                if (minFileSize > 0) {
                    logger.info("Applying min file size [{}] document filter", minFileSize);
                    documents.removeIf(doc -> doc.getFileSize() < minFileSize);
                }

                /* else {
                    // logger.info("Applying image file type documents");
                    // documents.removeIf(doc -> IMAGE_FILE_TYPE.contains(doc.getFileType().toLowerCase()));
                    documents.removeIf(doc -> !DOC_FILE_TYPE.contains(doc.getFileType().toLowerCase()));
                }*/

                logger.info("{} files are valid documents ({})", documents.size(), DOC_FILE_TYPE);
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
        List<Project> projects = new ArrayList<>();
        try {
            ProjectList projectList = null;
            projectList = projectListCache.get(hostname);
            List<String> projectNames = config.properties().project().projects();

            if (projectList == null) {
                logger.warn("Project List is empty.");
            } else {
                projects = projectList.getSearchResults();

                if (projects != null && !projectNames.isEmpty())
                    projects.removeIf(p -> !projectNames.contains(p.getProjectName()));
            }
        } catch (CacheLoader.InvalidCacheLoadException | ExecutionException e) {
            logger.error("Could not load project instance " + hostname, e.getCause());
        }

        return projects;
    }

    private AconexHttpClient setClient(AconexConfig config) {
        return new AconexHttpClient(new AconexHttpClientOptions(
                config.properties().host(),
                config.properties().apiKey(),
                AconexHttpClientOptions.AuthType.BASIC,
                config.properties().auth().basic().username(),
                config.properties().auth().basic().password(),
                config.properties().timeout().connection()
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
