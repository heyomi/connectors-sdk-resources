package com.lucidworks.connector.plugins.aconex.client.http;

import com.google.gson.Gson;
import com.lucidworks.connector.plugins.aconex.client.rest.RestApiUriBuilder;
import com.lucidworks.connector.plugins.aconex.model.ProjectList;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

import static com.lucidworks.connector.plugins.aconex.model.Constants.*;

@Slf4j
public class AconexHttpClient {
    private final AconexHttpClientOptions options;
    private final HttpClient httpClient;
    private String basicAuth;

    @Inject
    public AconexHttpClient(AconexHttpClientOptions options) {
        this.options = options;
        this.httpClient = createHttpClient(options);
    }

    private HttpClient createHttpClient(AconexHttpClientOptions options) {
        setBasicAuth(basicAuth(options.getUsername(), options.getPassword()));

        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofMillis(options.getConnectionTimeout()))
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
                    .setHeader(HTTP_HEADER_APPLICATION_KEY, options.getApiKey())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("An error occurred while getting project list. Aconex API response: {}", response.statusCode());
            } else {
                projectList = new Gson().fromJson(response.body(), ProjectList.class);
                log.debug("Total projects found: {}", projectList.getSearchResults().size());
            }
        } catch (IOException e) {
            log.error("An error occurred while getting project list", e);
        } catch (InterruptedException e) {
            log.error("An error occurred while getting project list", e);
            Thread.currentThread().interrupt();
        }

        return projectList;
    }

    public String getDocumentsByProject(String projectId) {
        return getDocuments(projectId, 1, DEFAULT_PAGE_SIZE);
    }

    public String getDocuments(@NonNull String projectId, int pageNumber, int pageSize) {
        log.debug("Getting documents in project: {}/{}", projectId, pageNumber);

        String documents = null;
        if (pageNumber < 1) pageNumber = DEFAULT_PAGE_NUMBER;

        try {
            final URI uri = RestApiUriBuilder.buildDocumentsUri(options.getHostname(), projectId, pageNumber, pageSize);
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .setHeader(HttpHeaders.AUTHORIZATION, getBasicAuth())
                    .setHeader(HTTP_HEADER_APPLICATION_KEY, options.getApiKey())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("An error occurred while accessing project #{}. Aconex API response: {}", projectId, response.statusCode());
            } else {
                documents = response.body();
            }
        } catch (IOException e) {
            log.error("An error occurred while getting documents", e);
        } catch (InterruptedException e) {
            log.error("An error occurred while project list", e);
            Thread.currentThread().interrupt();
        }

        return documents;
    }

    public byte[] getDocument(@NonNull String projectId, @NonNull String documentId) {
        log.debug("Getting doc:{}", documentId);
        byte[] content = null;
        try {
            final URI uri = RestApiUriBuilder.buildDownloadDocumentsUri(options.getHostname(), projectId, documentId);
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .setHeader(HttpHeaders.AUTHORIZATION, getBasicAuth())
                    .setHeader(HTTP_HEADER_APPLICATION_KEY, options.getApiKey())
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                log.warn("An error occurred while accessing document:{} in project:{}. Aconex API response: {}", documentId, projectId, response.statusCode());
            } else {
                content = response.body();
            }
        } catch (IOException e) {
            log.error("An error occurred while getting documents from project={}", projectId, e);
        } catch (InterruptedException e) {
            log.error("An error occurred while getting documents from project={}", projectId, e);
            Thread.currentThread().interrupt();
        }

        return content;
    }

    private static String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    public String getBasicAuth() {
        return basicAuth;
    }

    public void setBasicAuth(String basicAuth) {
        this.basicAuth = basicAuth;
    }

}
