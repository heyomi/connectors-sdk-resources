package com.lucidworks.connector.plugins.aconex.client.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.lucidworks.connector.plugins.aconex.client.rest.RestApiUriBuilder;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.model.Document;
import com.lucidworks.connector.plugins.aconex.model.RegisterSearch;
import com.lucidworks.connector.plugins.aconex.model.SearchResults;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.lucidworks.connector.plugins.aconex.model.Constants.DOC_FILE_TYPE;

@Slf4j
public class DocumentListClient {
    private final CloseableHttpClient httpClient;
    private final AconexConfig config;
    private int totalPages;

    @Inject
    public DocumentListClient(CloseableHttpClient httpClient, AconexConfig config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    public List<Document> getDocuments(String projectId, int pageNumber) throws IOException {
        log.info("Getting documents {}:{}", projectId, pageNumber);

        List<Document> documents = new ArrayList<>();

        int maxItems = config.properties().limit().maxItems();

        URI uri = RestApiUriBuilder.buildDocumentsUri(config.properties().host(), projectId, pageNumber, config.properties().limit().pageSize(), config.properties().project().documentReturnFields());
        if (maxItems > 0 && maxItems <= 500) { // 500 is the max for a single Aconex request
            uri = RestApiUriBuilder.buildLimitedDocumentsUri(config.properties().host(), projectId, maxItems, config.properties().project().documentReturnFields());
        }

        HttpGet request = HttpClientHelper.createHttpRequest(uri, config);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    documents = getDocumentsFromXMLResponse(EntityUtils.toString(entity));
                    documents.forEach(d -> {
                        d.setUrl(projectId);
                    });
                }
            } else {
                log.warn("An error occurred while accessing project #{}. Aconex API response: {}", projectId, response != null ? response.getStatusLine() : null);
            }
        }

        return documents;
    }

    private List<Document> getDocumentsFromXMLResponse(String xml) {
        List<Document> documents = new ArrayList<>();
        try {
            XmlMapper xmlMapper = new XmlMapper();
            RegisterSearch registerSearch = xmlMapper.readValue(xml, RegisterSearch.class);
            SearchResults result = registerSearch.getSearchResults();

            if (result == null || result.getDocuments() == null) {
                log.warn("Document search results empty for project.");
            } else {
                totalPages = registerSearch.getTotalPages();
                documents = applyDocumentFilter(result.getDocuments());
            }

        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }

        return documents;
    }

    private List<Document> applyDocumentFilter(@NonNull List<Document> documents) {
        Set<String> includedFileExtensions = config.properties().limit().includedFileExtensions();
        Set<String> excludedFileExtensions = config.properties().limit().excludedFileExtensions();
        int maxFileSize = config.properties().limit().maxSizeBytes();
        int minFileSize = config.properties().limit().minSizeBytes();

        log.debug("{} files returned", documents.size());

        if (includedFileExtensions != null && !includedFileExtensions.isEmpty()) {
            log.debug("Applying included file type [{}] document filter", includedFileExtensions);
            documents.removeIf(doc -> !includedFileExtensions.contains(doc.getFileType().toLowerCase()));
        } else if (excludedFileExtensions != null && !excludedFileExtensions.isEmpty()) {
            log.debug("Applying excluded file type [{}] document filter", excludedFileExtensions);
            documents.removeIf(doc -> excludedFileExtensions.contains(doc.getFileType().toLowerCase()));
        }

        if (maxFileSize > 0) {
            log.debug("Applying max file size [{}] document filter", maxFileSize);
            documents.removeIf(doc -> doc.getFileSize() > maxFileSize);
        }

        if (minFileSize > 0) {
            log.debug("Applying min file size [{}] document filter", minFileSize);
            documents.removeIf(doc -> doc.getFileSize() < minFileSize);
        }

        log.debug("{} files are valid documents ({})", documents.size(), DOC_FILE_TYPE);

        return documents;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
