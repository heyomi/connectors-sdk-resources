package com.lucidworks.connector.plugins.aconex.client.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.lucidworks.connector.plugins.aconex.client.rest.RestApiUriBuilder;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.model.Document;
import com.lucidworks.connector.plugins.aconex.model.RegisterSearch;
import com.lucidworks.connector.plugins.aconex.model.SearchResults;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import static com.lucidworks.connector.plugins.aconex.model.Constants.*;

/**
 * The service executes a search of an organization's document register for a project.
 *
 * @see <a href="https://help.aconex.com/api-developer-guide/document#list-documents">List Documents</a>
 */
@Slf4j
public class DocumentListClient {
    private final CloseableHttpClient httpClient;
    private final AconexConfig config;

    @Getter @Setter
    private RegisterSearch documentRegister;

    @Inject
    public DocumentListClient(CloseableHttpClient httpClient, AconexConfig config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    public List<Document> getDocuments(String projectId, int pageNumber) throws IOException {
        log.debug("Getting documents {}:{}", projectId, pageNumber);

        List<Document> documents = new ArrayList<>();
        URI uri = RestApiUriBuilder.buildDocumentsUri(config.properties().api().host(), projectId, pageNumber, config.properties().limit().pageSize(), config.properties().project().documentReturnFields());
        HttpGet request = HttpClientHelper.createHttpRequest(uri, config);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    documents = getDocumentsFromXMLResponse(EntityUtils.toString(entity));
                    documents.forEach(d -> d.setUrl(config.properties().api().host(), projectId));
                }
            } else {
                log.error("{} response. Error: {}", response.getStatusLine().getReasonPhrase(), EntityUtils.toString(response.getEntity()));
                // throw new IOException(response.getStatusLine().getReasonPhrase());
            }
        }

        log.debug("{} documents processed", documents.size());

        return documents;
    }

    /**
     * A wrapper method for {@link #getDocuments(String, int)}
     *
     * @param projectId Project ID
     * @param pageNumber Page Number
     * @return Map - String, Object: {@link RegisterSearch} | List of {@link Document}
     */
    public Map<String, Object> getDocumentRegister(String projectId, int pageNumber) throws IOException {
        Map<String, Object> results = new HashMap<>();
        results.put(DOCUMENTS, getDocuments(projectId, pageNumber));
        results.put(REGISTER, getDocumentRegister());

        return results;
    }

    /**
     * Maps the XML response from to from {@link #getDocuments} to a List of {@link Document}
     *
     * @param xmlDocumentList XML response
     * @return List of {@link Document}
     */
    private List<Document> getDocumentsFromXMLResponse(String xmlDocumentList) throws JsonProcessingException {
        List<Document> documents = new ArrayList<>();
        XmlMapper xmlMapper = new XmlMapper();
        RegisterSearch registerSearch = xmlMapper.readValue(xmlDocumentList, RegisterSearch.class);
        SearchResults result = registerSearch.getSearchResults();

        if (result == null || result.getDocuments() == null) {
            log.warn("Document search results empty for project.");
        } else {
            log.debug("{} documents found", result.getDocuments().size());

            setDocumentRegister(registerSearch);
            documents = applyDocumentFilter(result.getDocuments());
        }

        return documents;
    }

    /**
     * Applies all the limits set in connector configuration to the document results list.
     *
     * @param documents List of {@link Document}
     * @return A filtered List of {@link Document}
     */
    private List<Document> applyDocumentFilter(@NonNull List<Document> documents) {
        Set<String> includedFileExtensions = config.properties().limit().includedFileExtensions();
        Set<String> excludedFileExtensions = config.properties().limit().excludedFileExtensions();
        boolean excludeEmptyDocument = config.properties().limit().excludeEmptyDocuments();
        boolean includeNonDocMetadata = config.properties().limit().includeMetadata();
        int maxFileSize = config.properties().limit().maxSizeBytes();
        int minFileSize = config.properties().limit().minSizeBytes();

        if (excludeEmptyDocument) {
            //SP-57: 1348828088672012186 1348828088682002271
            log.debug("Applying excluded empty file {} document filter", excludedFileExtensions);
            documents.removeIf(doc -> doc.getFileType() == null || doc.getFileType().equals("") || doc.getFileSize() <= 0);
        }

        if (CollectionUtils.isNotEmpty(includedFileExtensions)) {
            log.debug("Applying included file type {} document filter", includedFileExtensions);
            documents.removeIf(doc -> doc.getFileType() == null || !includedFileExtensions.contains(doc.getFileType().toLowerCase()));
        } else if (CollectionUtils.isNotEmpty(excludedFileExtensions)) {
            log.debug("Applying excluded file type {} document filter", excludedFileExtensions);
            documents.removeIf(doc -> doc.getFileType() == null || excludedFileExtensions.contains(doc.getFileType().toLowerCase()));
        }

        if (!includeNonDocMetadata) {
            log.debug("Applying excluded file type {} document filter", excludedFileExtensions);
            documents.removeIf(doc -> !DEFAULT_DOC_FILE_TYPES.contains(doc.getFileType()));
        }

        if (maxFileSize > 0) {
            log.debug("Applying max file size {} document filter", maxFileSize);
            documents.removeIf(doc -> doc.getFileSize() > maxFileSize);
        }

        if (minFileSize > 0) {
            log.debug("Applying min file size {} document filter", minFileSize);
            documents.removeIf(doc -> doc.getFileSize() < minFileSize);
        }

        return documents;
    }


}
