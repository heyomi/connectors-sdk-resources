package com.lucidworks.connector.plugins.aconex.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.lucidworks.connector.plugins.aconex.client.http.AconexHttpClient;
import com.lucidworks.connector.plugins.aconex.config.AuthenticationProperties;
import com.lucidworks.connector.plugins.aconex.config.AdditionalProperties;
import com.lucidworks.connector.plugins.aconex.config.TimeoutProperties;
import com.lucidworks.connector.plugins.aconex.model.Document;
import com.lucidworks.connector.plugins.aconex.model.Project;
import com.lucidworks.connector.plugins.aconex.model.ProjectList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AconexClient {
    private static final Logger logger = LoggerFactory.getLogger(AconexClient.class);
    private final AuthenticationProperties authenticationProperties;
    private final TimeoutProperties timeoutProperties;
    private final AdditionalProperties additionalProperties;
    private final AconexHttpClient httpClient;
    private LoadingCache<String, ProjectList> projectListCache;
    private String apiEndpoint;

    private Map<String, List<String>> projectDocumentIds = new HashMap<>();

    public AconexClient(AuthenticationProperties authenticationProperties, TimeoutProperties timeoutProperties, AdditionalProperties additionalProperties) {
        this.httpClient = new AconexHttpClient(authenticationProperties, timeoutProperties, additionalProperties);
        this.apiEndpoint = httpClient.getApiEndpoint();
        this.authenticationProperties = authenticationProperties;
        this.timeoutProperties = timeoutProperties;
        this.additionalProperties = additionalProperties;
        this.projectListCache = getProjectsCache(apiEndpoint);
    }

    private List<Project> getProjects() {
        try {
            ProjectList projectList = projectListCache.get(apiEndpoint);
            return projectList.getSearchResults();
        } catch (ExecutionException e) {
            throw new RuntimeException("Could not load project instance " + apiEndpoint, e.getCause());
        }
    }

    private void processProjectDocumentIds() {
        Project p = new Project("1879048409", "");
        List<Project> projects = getProjects();
        projects = Collections.singletonList(p);

        for (Project project : projects) {
            List<Document> documents = httpClient.getDocuments(project.getProjectID());
            List<String> ids = documents.stream().map(Document::getId).collect(Collectors.toList());
            projectDocumentIds.putIfAbsent(project.getProjectID(), ids);
        }

        setProjectDocumentIds(projectDocumentIds);
    }

    private Map<String, List<String>> getProjectDocumentIds() {
        return projectDocumentIds;
    }

    public void setProjectDocumentIds(Map<String, List<String>> projectDocumentIds) {
        this.projectDocumentIds = (projectDocumentIds == null) ? new HashMap<>() : projectDocumentIds;
    }

    private LoadingCache<String, ProjectList> getProjectsCache(String apiEndpoint) {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build(new CacheLoader<String, ProjectList>() {
                    @Override
                    public ProjectList load(String apiEndpoint) throws Exception {
                        return httpClient.getProjectList(apiEndpoint);
                    }
                });
    }
}
