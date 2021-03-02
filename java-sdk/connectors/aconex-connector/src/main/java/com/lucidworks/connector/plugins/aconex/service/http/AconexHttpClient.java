package com.lucidworks.connector.plugins.aconex.service.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.model.*;
import com.lucidworks.connector.plugins.aconex.service.rest.RestApiUriBuilder;
import lombok.NonNull;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

import static com.lucidworks.connector.plugins.aconex.model.Constants.DEFAULT_PAGE_NUMBER;
import static com.lucidworks.connector.plugins.aconex.model.Constants.DOC_FILE_TYPE;

public class AconexHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(AconexHttpClient.class);
    private final HttpClient httpClient;
    private final String apiEndpoint;
    private final List<String> fileTypes;
    private final int pageSize;
    private String basicAuth;
    private SearchResultsStats stats;

    public AconexHttpClient(AconexConfig config) {
        this.httpClient = createHttpClient(config.properties().auth().username(), config.properties().auth().password(), config.properties().timeout().connectTimeoutMs());
        this.apiEndpoint = config.properties().auth().instanceUrl() + "/api";
        this.fileTypes = config.properties().fileTypes();
        this.pageSize = config.properties().documentsPerPage();
    }

    private HttpClient createHttpClient(String username, String password, int connectionTimeout) {
        setBasicAuth(basicAuth(username, password));

        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofMillis(connectionTimeout))
                .build();
    }

    public ProjectList getProjectList(String apiEndpoint) {
        ProjectList projectList = null;

        try {
            final URI uri = RestApiUriBuilder.buildProjectsUri(apiEndpoint);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                    .setHeader(HttpHeaders.AUTHORIZATION, getBasicAuth())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.warn("An error occurred while getting project list. Aconex API response: {}", response.statusCode());
            } else {
                projectList = new Gson().fromJson(response.body(), ProjectList.class);
                logger.info("Total projects found: {}", projectList.getSearchResults().size());
            }
        } catch (IOException e) {
            logger.error("An error occurred while getting project list", e);
        } catch (InterruptedException e) {
            logger.error("An error occurred while getting project list", e);
            Thread.currentThread().interrupt();
        }

        return projectList;
    }

    public List<Document> getDocuments(String projectId) {
        return getDocuments(projectId, 1);
    }

    public List<Document> getDocuments(@NonNull String projectId, int pageNumber) {
        logger.info("Getting documents in project: {}, page: {}", projectId, pageNumber);

        List<Document> documents = new ArrayList<>();
        if (pageNumber < 1) pageNumber = DEFAULT_PAGE_NUMBER;

        try {
            final URI uri = RestApiUriBuilder.buildDocumentsUri(apiEndpoint, projectId, pageNumber, pageSize);
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .setHeader(HttpHeaders.AUTHORIZATION, getBasicAuth())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.warn("An error occurred while accessing project #{}. Aconex API response: {}", projectId, response.statusCode());
            } else {
                documents = getDocumentsFromXML(response.body());
            }
        } catch (IOException e) {
            logger.error("An error occurred while getting documents", e);
        } catch (InterruptedException e) {
            logger.error("An error occurred while project list", e);
            Thread.currentThread().interrupt();
        }

        logger.info("{} documents crawled from project:{}", documents.size(), projectId);

        return documents;
    }

    public Map<String, Object> getDocumentContent(@NonNull String projectId, @NonNull String documentId) {
        Map<String, Object> content = null;
        try {
            final URI uri = RestApiUriBuilder.buildDownloadDocumentsUri(apiEndpoint, projectId, documentId);
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .setHeader(HttpHeaders.AUTHORIZATION, getBasicAuth())
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                logger.warn("An error occurred while accessing document:{} in project:{}. Aconex API response: {}", documentId, projectId, response.statusCode());
            } else {
                content = parseDocument(response.body());
            }
        } catch (IOException | TikaException | SAXException e) {
            logger.error("An error occurred while getting documents from project={}", projectId, e);
        } catch (InterruptedException e) {
            logger.error("An error occurred while getting documents from project={}", projectId, e);
            Thread.currentThread().interrupt();
        }

        return content;
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 401) {
            throw new NotAuthorizedException(response.body());
        }

        if (response.statusCode() == 403) {
            throw new ForbiddenException(response.body());
        }

        return response;
    }

    private Map<String, Object> parseDocument(byte[] body) throws TikaException, SAXException, IOException {
        Map<String, Object> content = new HashMap<>();
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

        return content;
    }

    private static String basicAuth(@NonNull String username, @NonNull String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    private List<Document> getDocumentsFromXML(String xml) throws JsonProcessingException {
        XmlMapper xmlMapper = new XmlMapper();
        RegisterSearch registerSearch = xmlMapper.readValue(xml, RegisterSearch.class);
        SearchResults result = registerSearch.getSearchResults();
        List<Document> documents = result.getDocuments();
        setStats(registerSearch);

        if (fileTypes != null && !fileTypes.isEmpty()) {
            logger.info("Applying file type [{}] document filter", fileTypes);
            documents.removeIf(doc -> !fileTypes.contains(doc.getFileType().toLowerCase()));
        } else {
            // logger.info("Applying image file type documents");
            // documents.removeIf(doc -> IMAGE_FILE_TYPE.contains(doc.getFileType().toLowerCase()));
            documents.removeIf(doc -> !DOC_FILE_TYPE.contains(doc.getFileType().toLowerCase()));
        }

        return documents;
    }

    public String getBasicAuth() {
        return basicAuth;
    }

    public void setBasicAuth(String basicAuth) {
        this.basicAuth = basicAuth;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
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
