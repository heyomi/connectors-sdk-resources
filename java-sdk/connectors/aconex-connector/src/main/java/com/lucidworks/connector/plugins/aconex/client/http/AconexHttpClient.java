package com.lucidworks.connector.plugins.aconex.client.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import com.lucidworks.connector.plugins.aconex.client.rest.RestApiUriBuilder;
import com.lucidworks.connector.plugins.aconex.config.AdditionalProperties;
import com.lucidworks.connector.plugins.aconex.config.AuthenticationProperties;
import com.lucidworks.connector.plugins.aconex.config.TimeoutProperties;
import com.lucidworks.connector.plugins.aconex.model.Document;
import com.lucidworks.connector.plugins.aconex.model.ProjectList;
import com.lucidworks.connector.plugins.aconex.model.RegisterSearch;
import com.lucidworks.connector.plugins.aconex.model.SearchResults;
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
import java.net.UnknownServiceException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

import static com.lucidworks.connector.plugins.aconex.config.AconexConstants.IMAGE_FILE_TYPE;

public class AconexHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(AconexHttpClient.class);
    private final HttpClient httpClient;
    private final String apiEndpoint;
    private final String fileTypes;
    private String basicAuth;

    public AconexHttpClient(AuthenticationProperties.Properties auth, TimeoutProperties.Properties timeout, AdditionalProperties.Properties additional) {
        this.httpClient = createHttpClient(auth.username(), auth.password(), timeout.connectTimeoutMs());
        this.apiEndpoint = auth.instanceUrl() + "/api";
        this.fileTypes = additional.fileType();
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

            HttpResponse<String> response = sendRequest(request);

            if (response.statusCode() != 200) {
                throw new RuntimeException("Could not get project list: " + response.body());
            }

            projectList = new Gson().fromJson(response.body(), ProjectList.class);
            logger.info("Total number of projects found:{}", projectList.getSearchResults().size());
        } catch (IOException e) {
            logger.error("An error occurred while getting project list", e);
        } catch (InterruptedException e) {
            logger.error("An error occurred while getting project list", e);
            Thread.currentThread().interrupt();
        }

        return projectList;
    }

    public List<Document> getDocuments(String projectId) {
        logger.info("Getting document IDs...");

        // TODO: Create PAGED logic
        List<Document> documents = new ArrayList<>();
        try {
            final URI uri = RestApiUriBuilder.buildDocumentsUri(apiEndpoint, projectId);
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .setHeader(HttpHeaders.AUTHORIZATION, getBasicAuth())
                    .build();

            HttpResponse<String> response = sendRequest(request);

            if (response.statusCode() != 200) {
                logger.warn("An error occurred while accessing project #{}, response: {}", projectId, response.body());
            } else {
                documents = getDocumentsFromXML(response.body());
            }
        } catch (IOException e) {
            logger.error("An error occurred while getting documents", e);
        } catch (InterruptedException e) {
            logger.error("An error occurred while project list", e);
            Thread.currentThread().interrupt();
        }

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

            if (response.statusCode() == 401) {
                throw new NotAuthorizedException(Arrays.toString(response.body()));
            }

            if (response.statusCode() == 403) {
                throw new ForbiddenException(Arrays.toString(response.body()));
            }

            if (response.statusCode() != 200) {
                logger.warn("An error occurred while accessing project #{}, response: {}", projectId, response.body());
                throw new UnknownServiceException(Arrays.toString(response.body()));
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
        RegisterSearch value = xmlMapper.readValue(xml, RegisterSearch.class);
        SearchResults result = value.getSearchResults();
        List<Document> documents = result.getDocuments();

        if (fileTypes != null) {
            logger.info("Applying file type [{}] document filter", fileTypes);
            documents.removeIf(doc -> !fileTypes.contains(doc.getFileType().toLowerCase()));
        } else {
            logger.info("Applying image file type documents");
            documents.removeIf(doc -> IMAGE_FILE_TYPE.contains(doc.getFileType().toLowerCase()));
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
}
