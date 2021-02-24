package com.lucidworks.connector.plugins.aconex.client;

import com.google.gson.Gson;
import com.lucidworks.connector.plugins.aconex.client.rest.RestApiUriBuilder;
import com.lucidworks.connector.plugins.aconex.config.AuthenticationProperties;
import com.lucidworks.connector.plugins.aconex.config.TimeoutProperties;
import com.lucidworks.connector.plugins.aconex.model.Project;
import com.lucidworks.connector.plugins.aconex.model.ProjectList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

import static com.lucidworks.connector.plugins.aconex.config.AconexConstants.TIMEOUT_MS;

public class AconexClient {
    private static final Logger logger = LoggerFactory.getLogger(AconexClient.class);
    private final AuthenticationProperties authenticationProperties;
    private final TimeoutProperties timeoutProperties;
    private String apiRoot;
    private String basicAuth;
    private HttpClient httpClient;
    private List<Project> projects;

    public AconexClient(AuthenticationProperties authenticationProperties, TimeoutProperties timeoutProperties) {
        this.authenticationProperties = authenticationProperties;
        this.timeoutProperties = timeoutProperties;

        init();
    }

    private void init() {
        int timeout = TIMEOUT_MS;

        if (timeoutProperties != null && timeoutProperties.properties() != null) {
            timeout = timeoutProperties.properties().connectTimeoutMs();
        }

        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofMillis(timeout))
                .build();

        this.apiRoot = "https://apidev.aconex.com/api";
        this.basicAuth = basicAuth(this.authenticationProperties.authentication().username(), this.authenticationProperties.authentication().password());
    }

    public List<Project> getProjects() {
        try {
            final URI uri = RestApiUriBuilder.buildProjectsUri(apiRoot);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                    .setHeader(HttpHeaders.AUTHORIZATION, basicAuth)
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 401) {
                throw new NotAuthorizedException(response.body());
            }

            ProjectList projectList = new Gson().fromJson(response.body(), ProjectList.class);
            projects = projectList.getSearchResults();
            logger.info("Projects={}", projects.size());
        } catch (IOException | InterruptedException e) {
            logger.error("An error occurred while getting projects", e);
        }

        return projects;
    }

    public Object getDocumentIds() {
        try {
            for (Project project : getProjects()) {
                final URI uri = RestApiUriBuilder.buildDocumentsUri(apiRoot, project.getProjectID());
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(uri)
                        .setHeader(HttpHeaders.AUTHORIZATION, basicAuth)
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                logger.info(response.body());
            }
        } catch (IOException | InterruptedException e) {
            logger.error("An error occurred while getting documents", e);
        }
        return null;
    }

    private static String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }
}
