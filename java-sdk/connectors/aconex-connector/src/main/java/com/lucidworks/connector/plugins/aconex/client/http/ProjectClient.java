package com.lucidworks.connector.plugins.aconex.client.http;

import com.google.gson.Gson;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.model.Project;
import com.lucidworks.connector.plugins.aconex.model.ProjectList;
import com.lucidworks.connector.plugins.aconex.service.rest.RestApiUriBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class ProjectClient {

    private static final Logger logger = LoggerFactory.getLogger(ProjectClient.class);
    private final CloseableHttpClient httpClient;
    private final AconexConfig config;

    @Inject
    public ProjectClient(CloseableHttpClient httpClient, AconexConfig config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    public List<Project> getProjects() throws IOException {
        ProjectList projectList = new ProjectList();
        URI uri = RestApiUriBuilder.buildProjectsUri(config.properties().host());
        HttpGet request = HttpClientHelper.createHttpRequest(uri, config);

        // project endpoint accepts JSON
        request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    projectList = new Gson().fromJson(EntityUtils.toString(entity), ProjectList.class);
                    logger.info("Total projects found: {}", projectList.getSearchResults().size());
                }
            } else {
                logger.error("An error occurred while getting project list. Aconex API response: {}", response != null ? response.getStatusLine() : null);
            }
        }

        return projectList.getSearchResults();
    }
}
