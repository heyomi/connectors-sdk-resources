package com.lucidworks.connector.plugins.feed.client.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static com.lucidworks.connector.plugins.feed.config.AconexConstants.*;

public class RestApiUriBuilder {
    private static final Logger logger = LoggerFactory.getLogger(RestApiUriBuilder.class);

    public URI buildProjectsUri(String apiRootPath) {
        final URI uri = UriBuilder.fromPath(apiRootPath).path(PROJECT).build();
        final String uriString = uri.toString();

        logger.info("Built URI: {}, length={}", uriString, uriString.length());
        return uri;
    }

    public URI buildDocumentsUri(String apiRootPath, String projectId) {
        UriBuilder uriBuilder = UriBuilder.fromPath(apiRootPath)
                .path(PROJECT)
                .path(projectId)
                .path(REGISTER);

        uriBuilder.queryParam(PARAM_SEARCH_TYPE, SEARCH_TYPE_PAGED);
        uriBuilder.queryParam(PARAM_PAGE_SIZE, 25);
        uriBuilder.queryParam(PARAM_PAGE_NUMBER, 1);
        uriBuilder.queryParam(PARAM_RETURN_FIELDS, RETURN_FIELDS);

        final URI uri = uriBuilder.build();
        final String uriString = uri.toString();

        logger.info("Built URI: {}, length={}", uriString, uriString.length());
        return uri;
    }

    public URI buildFetchDocumentsUri(String apiRootPath, String projectId, String documentId) {
        final URI uri = UriBuilder.fromPath(apiRootPath)
                .path(PROJECT)
                .path(projectId)
                .path(REGISTER)
                .path(documentId)
                .path(MARKEDUP)
                .build();

        final String uriString = uri.toString();

        logger.info("Built URI: {}, length={}", uriString, uriString.length());
        return uri;
    }
}
