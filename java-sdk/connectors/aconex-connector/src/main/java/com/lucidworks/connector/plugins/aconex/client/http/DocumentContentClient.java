package com.lucidworks.connector.plugins.aconex.client.http;

import com.lucidworks.connector.plugins.aconex.client.rest.RestApiUriBuilder;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

@Slf4j
public class DocumentContentClient {
    private final CloseableHttpClient httpClient;
    private final AconexConfig config;

    @Inject
    public DocumentContentClient(CloseableHttpClient httpClient, AconexConfig config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    public InputStream getDocumentContent(String projectId, String documentId) throws IOException {
        log.debug("Getting document content:{}", documentId);


        URI uri = RestApiUriBuilder.buildDownloadDocumentsUri(config.properties().api().host(), projectId, documentId);
        HttpGet request = HttpClientHelper.createHttpRequest(uri, config);
        CloseableHttpResponse response = httpClient.execute(request);
        // SP-57: I think the connection and response resource needs to until Fusion modules process the data in Fetcher.newContent
        // If I close the resources I get ConnectionClosedExceptions
        // org.apache.http.ConnectionClosedException: Premature end of Content-Length delimited message body (expected: 4; received: 0)
        if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
            HttpEntity entity = response.getEntity();

            if (entity != null) return entity.getContent();
        } else {
            log.error("An error occurred while getting document:{}/{}. Aconex API response: {}", projectId, documentId, response.getStatusLine());
        }

        // ConnectionPoolTimeoutException: Timeout waiting for connection from pool
        return new ByteArrayInputStream(new byte[0]);
    }
}
