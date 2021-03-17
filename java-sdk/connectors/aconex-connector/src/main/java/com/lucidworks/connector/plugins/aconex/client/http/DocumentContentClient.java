package com.lucidworks.connector.plugins.aconex.client.http;

import com.lucidworks.connector.plugins.aconex.client.rest.RestApiUriBuilder;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

/**
 * The service retrieves the backing file associated with the specified document.
 * @see <a href="https://help.aconex.com/api-developer-guide/document#download-document-file">Download Document File</a>
 */
@Slf4j
public class DocumentContentClient {
    private final CloseableHttpClient httpClient;
    private final AconexConfig config;

    @Inject
    public DocumentContentClient(CloseableHttpClient httpClient, AconexConfig config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    /**
     * The connection and response resource needs to be open until the process {@link com.lucidworks.connector.plugins.aconex.fetcher.AconexFetcher#fetch} is done.
     * Closing the resources in this method will throw exceptions.
     *
     * <li>ConnectionClosedException: Premature end of Content-Length delimited message body (expected: 4; received: 0)</li>
     * <li>ConnectionPoolTimeoutException: Timeout waiting for connection from pool</li>
     *
     * @param projectId Project ID
     * @param documentId Document ID
     * @return The response is a binary stream of the file associated with the document
     */
    @SneakyThrows
    public InputStream getDocumentContent(String projectId, String documentId) {
        log.debug("Getting document content:{}", documentId);

        URI uri = RestApiUriBuilder.buildDownloadDocumentsUri(config.properties().api().host(), projectId, documentId);
        HttpGet request = HttpClientHelper.createHttpRequest(uri, config);
        CloseableHttpResponse response = httpClient.execute(request);

        if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
            HttpEntity entity = response.getEntity();
            if (entity != null) return entity.getContent();
        } else {
            log.error("An error occurred while getting document:{}/{}. Aconex API response: {}", projectId, documentId, response.getStatusLine());
        }

        return new ByteArrayInputStream(new byte[0]);
    }

    /**
     * A wrapper method for {@link #getDocumentContent(String, String)}
     *
     * @param projectId Project ID
     * @param documentId Document ID
     * @param isDocument Document Type
     * @return The response is a binary stream of the file associated with the document
     */
    public InputStream getDocumentContent(String projectId, String documentId, boolean isDocument) {
        if (isDocument) return getDocumentContent(projectId, documentId);
        else return new ByteArrayInputStream(new byte[0]);
    }
}
