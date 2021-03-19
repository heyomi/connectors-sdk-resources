package com.lucidworks.connector.plugins.aconex.client.http;

import com.google.gson.Gson;
import com.lucidworks.connector.plugins.aconex.client.rest.RestApiUriBuilder;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.model.Project;
import com.lucidworks.connector.plugins.aconex.model.ProjectList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * The service executes a search of an organization's list of projects.
 * @see <a href="https://help.aconex.com/aconex/aconex-apis/api-documentation/projects">Projects</a>
 */
@Slf4j
public class ProjectClient {
    private final CloseableHttpClient httpClient;
    private final AconexConfig config;

    @Inject
    public ProjectClient(CloseableHttpClient httpClient, AconexConfig config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    public List<Project> getProjects() throws IOException {
        ProjectList projectList;
        URI uri = RestApiUriBuilder.buildProjectsUri(config.properties().api().host());
        HttpGet request = HttpClientHelper.createHttpRequest(uri, config);
        List<Project> projects = new ArrayList<>();

        // The Aconex API Project endpoint accepts JSON
        request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    projectList = new Gson().fromJson(EntityUtils.toString(entity), ProjectList.class);
                    projects = projectList.getSearchResults();

                    // Apply Project Filter by Name
                    List<String> projectNames = config.properties().project().projects();
                    if (CollectionUtils.isNotEmpty(projectNames)) {
                        log.debug("Project Filter: {}", projectNames);
                        projects.removeIf(p -> !projectNames.contains(p.getProjectName()));
                    }

                    log.debug("Total projects: {}", projects.size());
                }
            } else {
                log.error("{} response. Error: {}", response.getStatusLine().getReasonPhrase(), EntityUtils.toString(response.getEntity()));
                // throw new IOException(response.getStatusLine().getReasonPhrase());
            }
        }

        return projects;
    }
}
