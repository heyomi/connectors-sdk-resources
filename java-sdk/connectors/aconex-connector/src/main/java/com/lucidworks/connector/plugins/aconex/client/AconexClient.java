package com.lucidworks.connector.plugins.aconex.client;

import com.lucidworks.connector.plugins.aconex.model.Document;
import com.lucidworks.connector.plugins.aconex.model.Project;

import java.io.IOException;
import java.util.List;

public interface AconexClient {

    List<Project> getProjects() throws IOException;

    List<Document> getDocuments(String projectId, int pageNumber) throws IOException;
}
