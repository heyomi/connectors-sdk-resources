package com.lucidworks.connector.plugins.aconex.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.lucidworks.connector.plugins.aconex.client.AconexClient;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.model.Document;
import com.lucidworks.connector.plugins.aconex.model.Project;
import com.lucidworks.connector.plugins.aconex.model.ProjectList;
import com.lucidworks.connector.plugins.aconex.model.SearchResultsStats;
import com.lucidworks.connector.plugins.aconex.service.http.AconexHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.lucidworks.connector.plugins.aconex.model.Constants.DEFAULT_PAGE_NUMBER;

public class AconexService implements AconexClient {
    private static final Logger logger = LoggerFactory.getLogger(AconexService.class);

    private final AconexHttpClient httpClient;
    private final AconexConfig config;
    private final LoadingCache<String, ProjectList> projectListCache;
    private final String apiEndpoint;

    public AconexService(AconexConfig config) {
        this.httpClient = new AconexHttpClient(config);
        this.apiEndpoint = httpClient.getApiEndpoint();
        this.projectListCache = getProjectsCache(apiEndpoint);
        this.config = config;
    }

    @Override
    public Map<String, Map<String, Object>> getDocuments() {
        return getDocuments(DEFAULT_PAGE_NUMBER);
    }

    @Override
    public Map<String, Map<String, Object>> getDocuments(int pageNumber) {
        Map<String, Map<String, Object>> content = new HashMap<>();
        List<String> projectIds = config.properties().projects();

        if (projectIds.isEmpty()) {
            logger.warn("No project selected in configuration. All projects will be crawled.");
            projectIds = getProjectIds();
        }
        // Project p = new Project("1879048409", "");
        // projects = Collections.singletonList(p);

        for (String projectId : projectIds) {
            content.putAll(getDocumentsByProject(projectId, pageNumber));
        }

        return content;
    }

    public List<String> getProjectIds() {
        return getProjects().stream().map(Project::getProjectID).collect(Collectors.toList());
    }

    public Map<String, Map<String, Object>> getDocumentsByProject(String projectId, int pageNumber) {
        Map<String, Map<String, Object>> content = new HashMap<>();
        List<Document> documents = httpClient.getDocuments(projectId, pageNumber);
        List<String> ids = documents.stream().map(Document::getId).collect(Collectors.toList());
        Map<String, Object> document;

        for (String id : ids) {
            document = httpClient.getDocumentContent(projectId, id);

            if (document != null && !document.isEmpty()) {
                document.put("document:id", id);
                document.put("project:id", projectId);
                content.put(projectId + ":" + id, document);
            }
        }

        return content;
    }

    @Override
    public SearchResultsStats getSearchResultsStats() {
        return httpClient.getStats();
    }

    private List<Project> getProjects() {
        try {
            ProjectList projectList = projectListCache.get(apiEndpoint);
            return projectList.getSearchResults();
        } catch (ExecutionException e) {
            throw new InternalError("Could not load project instance " + apiEndpoint, e.getCause());
        }
    }

    private LoadingCache<String, ProjectList> getProjectsCache(String apiEndpoint) {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build(new CacheLoader<>() {
                    @Override
                    public ProjectList load(String apiEndpoint) throws Exception {
                        return httpClient.getProjectList(apiEndpoint);
                    }
                });
    }
}
