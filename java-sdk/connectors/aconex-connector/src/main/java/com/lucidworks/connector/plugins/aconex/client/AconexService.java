package com.lucidworks.connector.plugins.aconex.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.lucidworks.connector.plugins.aconex.client.http.AconexHttpClient;
import com.lucidworks.connector.plugins.aconex.config.AdditionalProperties;
import com.lucidworks.connector.plugins.aconex.config.AuthenticationProperties;
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

public class AconexService {
    private static final Logger logger = LoggerFactory.getLogger(AconexService.class);
    private final AconexHttpClient httpClient;
    private final LoadingCache<String, ProjectList> projectListCache;
    private final String apiEndpoint;

    public AconexService(AuthenticationProperties.Properties auth, TimeoutProperties.Properties timeout, AdditionalProperties.Properties additional) {
        this.httpClient = new AconexHttpClient(auth, timeout, additional);
        this.apiEndpoint = httpClient.getApiEndpoint();
        this.projectListCache = getProjectsCache(apiEndpoint);
    }

    public Map<String, Map<String, Object>> getContent() {
        logger.info("Getting content...");

        Map<String, Map<String, Object>> content = new HashMap<>();
        Project p = new Project("1879048409", "");
        List<Project> projects = getProjects();
        projects = Collections.singletonList(p);

        for (Project project : projects) {
            List<Document> documents = httpClient.getDocuments(project.getProjectID());
            List<String> ids = documents.stream().map(Document::getId).collect(Collectors.toList());
            Map<String, Object> document;

            for (String id: ids) {
                document = httpClient.getDocumentContent(project.getProjectID(), id);

                if (document != null && !document.isEmpty()) {
                    document.put("project:id", project.getProjectID());
                    content.put(project.getProjectID() + ":" + id, document);
                }
            }
        }

        return content;
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
