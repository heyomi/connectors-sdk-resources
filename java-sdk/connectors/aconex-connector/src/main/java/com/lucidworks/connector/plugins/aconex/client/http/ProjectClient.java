package com.lucidworks.connector.plugins.aconex.client.http;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import com.lucidworks.connector.plugins.aconex.client.rest.RestApiUriBuilder;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.model.Project;
import com.lucidworks.connector.plugins.aconex.model.ProjectList;
import com.lucidworks.connector.plugins.aconex.model.RegisterSearch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.HttpEntity;
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

        // project endpoint accepts JSON
        request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    projectList = new Gson().fromJson(EntityUtils.toString(entity), ProjectList.class);
                    projects = projectList.getSearchResults();

                    List<String> projectNames = config.properties().project().projects();

                    if (CollectionUtils.isNotEmpty(projects)) {
                        log.info("Project Filter: {}", projectNames);
                        projects.removeIf(p -> !projectNames.contains(p.getProjectName()));
                    }

                    for (Project p : projects) {
                        int totalResults = getTotalResults(p.getProjectID());
                        int pages = getTotalPages(totalResults, config.properties().limit().pageSize());
                        p.setTotalResults(totalResults);
                        p.setTotalPages(pages);
                    }

                    log.debug("Total projects: {}", projects.size());
                }
            } else {
                log.error("An error occurred while getting project list. Aconex API response: {}", response != null ? response.getStatusLine() : null);
            }
        }

        return projects;
    }

    private int getTotalResults(String projectId) throws IOException {
        URI uri = RestApiUriBuilder.buildCountDocumentsUri(config.properties().api().host(), projectId);
        HttpGet request = HttpClientHelper.createHttpRequest(uri, config);
        int totalResults = 0;

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    XmlMapper xmlMapper = new XmlMapper();
                    RegisterSearch registerSearch = xmlMapper.readValue(EntityUtils.toString(entity), RegisterSearch.class);
                    totalResults = registerSearch.getTotalResults();

                    log.debug("Total results in project: {}", totalResults);
                }
            } else {
                log.error("An error occurred while getting project list. Aconex API response: {}", response != null ? response.getStatusLine() : null);
            }
        }

        return totalResults;
    }

    private int getTotalPages(int totalResults, int pageSize) {
        return (int) Math.ceil(totalResults / pageSize);
    }
}
