package com.lucidworks.connector.plugins.aconex.client;

import com.lucidworks.connector.plugins.aconex.client.http.DocumentContentClient;
import com.lucidworks.connector.plugins.aconex.client.http.DocumentListClient;
import com.lucidworks.connector.plugins.aconex.client.http.ProjectClient;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.model.Document;
import com.lucidworks.connector.plugins.aconex.model.Project;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class AconexService implements AconexClient {
    private final ProjectClient projectClient;
    private final DocumentListClient documentListClient;
    private final DocumentContentClient documentClient;
    private final AconexConfig config;

    public AconexService(ProjectClient projectClient, DocumentListClient documentListClient, DocumentContentClient documentClient, AconexConfig config) {
        this.projectClient = projectClient;
        this.documentListClient = documentListClient;
        this.documentClient = documentClient;
        this.config = config;
    }

    @Override
    public List<Project> getProjects() throws IOException {
        return projectClient.getProjects();
    }

    @Override
    public List<Document> getDocuments(String projectId, int pageNumber) throws IOException {
        return documentListClient.getDocuments(projectId, pageNumber);
    }

    @Override
    public Map<String, Object> getDocumentRegister(String projectId, int pageNumber) throws IOException {
        return documentListClient.getDocumentRegister(projectId, pageNumber);
    }

    @Override
    public Supplier<InputStream> getDocument(String projectId, String documentId) throws IOException {
        final InputStream in = documentClient.getDocumentContent(projectId, documentId);
        return () -> in ;
    }
}
