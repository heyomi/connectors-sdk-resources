package com.lucidworks.connector.plugins.aconex.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lucidworks.connector.plugins.aconex.model.Project;
import com.lucidworks.connector.plugins.aconex.client.rest.RestApiUriBuilder;
import com.lucidworks.connector.plugins.aconex.config.AuthenticationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class AconexClient {
    private static final Logger logger = LoggerFactory.getLogger(AconexClient.class);
    private final AuthenticationProperties authenticationProperties;
    private final HttpClient httpClient;
    private final RestApiUriBuilder restApiUriBuilder;
    private final String apiRoot;
    private List<Project> projects;

    @Inject
    public AconexClient(AuthenticationProperties authenticationProperties, RestApiUriBuilder restApiUriBuilder) {
        this.authenticationProperties = authenticationProperties;
        this.restApiUriBuilder = restApiUriBuilder;
        this.apiRoot = "https://apidev.aconex.com//api";
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    private List<Project> getProjects() {
        try {
            final URI uri = restApiUriBuilder.buildProjectsUri(apiRoot);
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .setHeader("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Type projectListType = new TypeToken<ArrayList<Project>>() {
            }.getType();
            projects = new Gson().fromJson(response.body(), projectListType);
            logger.info("Projects={}", projects.size());
        } catch (IOException | InterruptedException e) {
            logger.error("An error occurred while getting projects", e);
        }
        return projects;
    }

    public Object getDocumentIds() {
        try {
            for (Project project : getProjects()) {
                final URI uri = restApiUriBuilder.buildDocumentsUri(apiRoot, project.getId());
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(uri)
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                logger.info(response.body());
            }
        } catch (IOException | InterruptedException e) {
            logger.error("An error occurred while getting documents", e);
        }
        return null;
    }
}
