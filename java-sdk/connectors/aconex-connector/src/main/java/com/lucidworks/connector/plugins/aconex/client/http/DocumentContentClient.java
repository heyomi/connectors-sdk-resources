package com.lucidworks.connector.plugins.aconex.client.http;

import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.client.rest.RestApiUriBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class DocumentContentClient {
    private static final Logger logger = LoggerFactory.getLogger(DocumentContentClient.class);
    private final CloseableHttpClient httpClient;
    private final AconexConfig config;

    @Inject
    public DocumentContentClient(CloseableHttpClient httpClient, AconexConfig config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    public InputStream getDocumentContent(String projectId, String documentId) throws IOException {
        logger.debug("Crawling doc:{}", documentId);

        URI uri = RestApiUriBuilder.buildDownloadDocumentsUri(config.properties().host(), projectId, documentId);
        HttpGet request = HttpClientHelper.createHttpRequest(uri, config);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();

                if (entity != null) return entity.getContent();
            } else {
                logger.error("An error occurred while getting document:{}/{}. Aconex API response: {}", projectId, documentId, response != null ? response.getStatusLine() : null);
            }
        }

        return null;
    }
}
