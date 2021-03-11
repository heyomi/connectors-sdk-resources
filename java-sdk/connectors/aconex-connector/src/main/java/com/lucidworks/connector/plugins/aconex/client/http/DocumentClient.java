package com.lucidworks.connector.plugins.aconex.client.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.model.Document;
import com.lucidworks.connector.plugins.aconex.model.RegisterSearch;
import com.lucidworks.connector.plugins.aconex.model.SearchResults;
import com.lucidworks.connector.plugins.aconex.client.rest.RestApiUriBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.lucidworks.connector.plugins.aconex.model.Constants.DOC_FILE_TYPE;

public class DocumentClient {
    private static final Logger logger = LoggerFactory.getLogger(DocumentClient.class);
    private final CloseableHttpClient httpClient;
    private final AconexConfig config;

    @Inject
    public DocumentClient(CloseableHttpClient httpClient, AconexConfig config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    public List<Document> getDocumentsByProjectId(String projectId, int pageNumber) throws IOException {
        logger.info("Getting documents in project: {}/{}", projectId, pageNumber);

        List<Document> documents = new ArrayList<>();
        URI uri = RestApiUriBuilder.buildDocumentsUri(config.properties().host(), projectId, pageNumber, config.properties().limit().pageSize(), config.properties().project().documentReturnFields());
        HttpGet request = HttpClientHelper.createHttpRequest(uri, config);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                if (entity != null) documents = getDocumentsFromXMLResponse(EntityUtils.toString(entity));
            } else {
                logger.warn("An error occurred while accessing project #{}. Aconex API response: {}", projectId, response != null ? response.getStatusLine() : null);
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
                logger.warn("Document search results empty for project.");
            } else {
                documents = applyDocumentFilter(result.getDocuments());
            }

        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }

        return documents;
    }

    private List<Document> applyDocumentFilter(List<Document> documents) {
        Set<String> includedFileExtensions = config.properties().limit().includedFileExtensions();
        Set<String> excludedFileExtensions = config.properties().limit().excludedFileExtensions();
        int maxFileSize = config.properties().limit().maxSizeBytes();
        int minFileSize = config.properties().limit().minSizeBytes();

        logger.debug("{} files returned", documents.size());

        if (includedFileExtensions != null && !includedFileExtensions.isEmpty()) {
            logger.debug("Applying included file type [{}] document filter", includedFileExtensions);
            documents.removeIf(doc -> !includedFileExtensions.contains(doc.getFileType().toLowerCase()));
        } else if (excludedFileExtensions != null && !excludedFileExtensions.isEmpty()) {
            logger.debug("Applying excluded file type [{}] document filter", excludedFileExtensions);
            documents.removeIf(doc -> excludedFileExtensions.contains(doc.getFileType().toLowerCase()));
        }

        if (maxFileSize > 0) {
            logger.debug("Applying max file size [{}] document filter", maxFileSize);
            documents.removeIf(doc -> doc.getFileSize() > maxFileSize);
        }

        if (minFileSize > 0) {
            logger.debug("Applying min file size [{}] document filter", minFileSize);
            documents.removeIf(doc -> doc.getFileSize() < minFileSize);
        }

        logger.debug("{} files are valid documents ({})", documents.size(), DOC_FILE_TYPE);

        return documents;
    }
}
