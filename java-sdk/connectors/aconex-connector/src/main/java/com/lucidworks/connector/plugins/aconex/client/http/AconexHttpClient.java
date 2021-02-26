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
import java.util.stream.Collectors;

import static com.lucidworks.connector.plugins.aconex.config.AconexConstants.TIMEOUT_MS;

public class AconexHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(AconexHttpClient.class);
    private final HttpClient httpClient;
    private String basicAuth;
    private String apiEndpoint;
    private String fileTypes;

    public AconexHttpClient(AuthenticationProperties authenticationProperties, TimeoutProperties timeoutProperties, AdditionalProperties additionalProperties) {
        this.httpClient = createHttpClient(authenticationProperties, timeoutProperties);
        this.apiEndpoint = getApiEndpoint(authenticationProperties);
        this.fileTypes = additionalProperties.properties().fileType();
    }

    private String getApiEndpoint(AuthenticationProperties properties) {
        return properties.auth().instanceUrl() + "/api";
    }

    private HttpClient createHttpClient(AuthenticationProperties authenticationProperties, TimeoutProperties timeoutProperties) {
        int timeout = TIMEOUT_MS;

        if (timeoutProperties != null && timeoutProperties.timeout() != null) {
            timeout = timeoutProperties.timeout().connectTimeoutMs();
        }

        setBasicAuth(basicAuth(authenticationProperties.auth().username(), authenticationProperties.auth().password()));

        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofMillis(timeout))
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

            if (response.statusCode() == 401) {
                throw new NotAuthorizedException(response.body());
            }

            if (response.statusCode() == 403) {
                throw new ForbiddenException(response.body());
            }

            if (response.statusCode() != 200) {
                throw new RuntimeException("Could not get project list: " + response.body());
            }

            projectList = new Gson().fromJson(response.body(), ProjectList.class);
            logger.info("Projects={}", projectList.getSearchResults().size());
        } catch (IOException | InterruptedException e) {
            logger.error("An error occurred while getting projects", e);
        }

        return projectList;
    }

    public List<Document> getDocuments(String projectId) {
        logger.info("Getting document ids from project={}", projectId);

        // TODO: Create PAGED logic
        List<Document> documents = new ArrayList<>();
        try {
            final URI uri = RestApiUriBuilder.buildDocumentsUri(apiEndpoint, projectId);
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .setHeader(HttpHeaders.AUTHORIZATION, getBasicAuth())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 401) {
                throw new NotAuthorizedException(response.body());
            }

            if (response.statusCode() == 403) {
                throw new ForbiddenException(response.body());
            }

            if (response.statusCode() != 200) {
                logger.warn("An error occurred while accessing project #{}, response: {}", projectId, response.body());
            } else {
                documents = getDocumentsFromXML(response.body());
            }
        } catch (IOException | InterruptedException e) {
            logger.error("An error occurred while getting documents", e);
        }

        return documents;
    }

    public Map<String, String> getDocumentContent(@NonNull String projectId, @NonNull String documentId) {
        logger.info("Getting document content from project={}", projectId);

        Map<String, String> content = null;
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
            logger.error("An error occurred while getting documents", e);
        } catch (InterruptedException e) {
            logger.warn("Interrupted!", e);
            Thread.currentThread().interrupt();
        }

        return content;
    }

    private Map<String, String> parseDocument(byte[] body) throws TikaException, SAXException, IOException {
        Map<String, String> content = new HashMap<>();
        Parser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext parseContext = new ParseContext();
        InputStream targetStream = new ByteArrayInputStream(body);

        // parsing the document
        parser.parse(targetStream, handler, metadata, parseContext);

        //getting the content of the document
        content.put("body", handler.toString());

        //getting metadata of the document
        String[] metadataNames = metadata.names();

        for(String name : metadataNames) {
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

        if(fileTypes != null) {
            logger.info("Applying file type [{}] document filter", fileTypes);
            documents = documents.stream()
                    .filter(p -> (p.getFileType() != null && fileTypes.contains(p.getFileType().toLowerCase())))
                    .collect(Collectors.toList());
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
