package com.lucidworks.connector.plugins.aconex.client.rest;

import com.lucidworks.connector.plugins.aconex.config.AconexConstants;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class RestApiUriBuilder {
    private static final Logger logger = LoggerFactory.getLogger(RestApiUriBuilder.class);

    public static URI buildProjectsUri(@NonNull String apiRootPath) {
        final URI uri = UriBuilder.fromPath(apiRootPath).path(AconexConstants.PROJECTS).build();
        final String uriString = uri.toString();

        logger.info("Built URI: {}, length={}", uriString, uriString.length());
        return uri;
    }

    public static URI buildDocumentsUri(@NonNull String apiRootPath, String projectId) {
        UriBuilder uriBuilder = UriBuilder.fromPath(apiRootPath)
                .path(AconexConstants.PROJECTS)
                .path(projectId)
                .path(AconexConstants.REGISTER);

        uriBuilder.queryParam(AconexConstants.PARAM_SEARCH_TYPE, AconexConstants.SEARCH_TYPE_PAGED);
        uriBuilder.queryParam(AconexConstants.PARAM_PAGE_SIZE, 25);
        uriBuilder.queryParam(AconexConstants.PARAM_PAGE_NUMBER, 1);
        uriBuilder.queryParam(AconexConstants.PARAM_RETURN_FIELDS, AconexConstants.RETURN_FIELDS);

        final URI uri = uriBuilder.build();
        final String uriString = uri.toString();

        logger.info("Built URI: {}, length={}", uriString, uriString.length());
        return uri;
    }

    public static URI buildFetchDocumentsUri(@NonNull String apiRootPath, String projectId, String documentId) {
        final URI uri = UriBuilder.fromPath(apiRootPath)
                .path(AconexConstants.PROJECTS)
                .path(projectId)
                .path(AconexConstants.REGISTER)
                .path(documentId)
                .path(AconexConstants.MARKEDUP)
                .build();

        final String uriString = uri.toString();

        logger.info("Built URI: {}, length={}", uriString, uriString.length());
        return uri;
    }
}
