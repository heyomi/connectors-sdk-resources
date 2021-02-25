package com.lucidworks.connector.plugins.aconex.client.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import com.lucidworks.connector.plugins.aconex.client.rest.RestApiUriBuilder;
import com.lucidworks.connector.plugins.aconex.config.AuthenticationProperties;
import com.lucidworks.connector.plugins.aconex.config.TimeoutProperties;
import com.lucidworks.connector.plugins.aconex.model.Document;
import com.lucidworks.connector.plugins.aconex.model.ProjectList;
import com.lucidworks.connector.plugins.aconex.model.RegisterSearch;
import com.lucidworks.connector.plugins.aconex.model.SearchResults;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static com.lucidworks.connector.plugins.aconex.config.AconexConstants.TIMEOUT_MS;

public class AconexHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(AconexHttpClient.class);
    private final HttpClient httpClient;
    private String basicAuth;
    private String apiEndpoint;

    public AconexHttpClient(AuthenticationProperties authenticationProperties, TimeoutProperties timeoutProperties) {
        this.httpClient = createHttpClient(authenticationProperties, timeoutProperties);
        this.apiEndpoint = getApiEndpoint(authenticationProperties);
    }

    private String getApiEndpoint(AuthenticationProperties properties) {
        return properties.auth().instanceUrl() + "/api";
    }

    private HttpClient createHttpClient(AuthenticationProperties authenticationProperties, TimeoutProperties timeoutProperties) {
        int timeout = TIMEOUT_MS;

        if (timeoutProperties != null && timeoutProperties.properties() != null) {
            timeout = timeoutProperties.properties().connectTimeoutMs();
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

    private static String basicAuth(@NonNull String username, @NonNull String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    private List<Document> getDocumentsFromXML(String xml) throws JsonProcessingException {
        XmlMapper xmlMapper = new XmlMapper();
        RegisterSearch value = xmlMapper.readValue(xml, RegisterSearch.class);
        SearchResults result = value.getSearchResults();

        return result.getDocuments();
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
