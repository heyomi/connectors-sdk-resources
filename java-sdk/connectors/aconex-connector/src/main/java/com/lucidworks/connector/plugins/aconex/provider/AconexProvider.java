package com.lucidworks.connector.plugins.aconex.provider;

import com.lucidworks.connector.plugins.aconex.client.AconexClient;
import com.lucidworks.connector.plugins.aconex.client.http.DocumentListClient;
import com.lucidworks.connector.plugins.aconex.client.http.DocumentContentClient;
import com.lucidworks.connector.plugins.aconex.client.http.ProjectClient;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.client.AconexService;

import javax.inject.Inject;
import javax.inject.Provider;

public class AconexProvider implements Provider<AconexClient> {
    private final ProjectClient projectClient;
    private final DocumentListClient documentClient;
    private final DocumentContentClient contentClient;
    private final AconexConfig config;

    @Inject
    AconexProvider(ProjectClient projectClient, DocumentListClient documentClient, DocumentContentClient contentClient, AconexConfig config) {
        this.projectClient = projectClient;
        this.documentClient = documentClient;
        this.contentClient = contentClient;
        this.config = config;
    }

    @Override
    public AconexClient get() {
        return new AconexService(projectClient, documentClient, contentClient, config);
    }
}
