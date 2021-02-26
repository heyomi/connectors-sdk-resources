package com.lucidworks.connector.plugins.aconex.client.rest;

import com.lucidworks.connector.plugins.aconex.config.AconexConstants;
import lombok.NonNull;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class RestApiUriBuilder {

    public static URI buildProjectsUri(@NonNull String apiRootPath) {
        return UriBuilder.fromPath(apiRootPath).path(AconexConstants.PROJECTS).build();
    }

    public static URI buildDocumentsUri(@NonNull String apiRootPath, String projectId) {
        UriBuilder uriBuilder = UriBuilder.fromPath(apiRootPath)
                .path(AconexConstants.PROJECTS)
                .path(projectId)
                .path(AconexConstants.REGISTER);

        uriBuilder.queryParam(AconexConstants.PARAM_SEARCH_TYPE, AconexConstants.SEARCH_TYPE_PAGED);
        uriBuilder.queryParam(AconexConstants.PARAM_PAGE_SIZE, 250);
        uriBuilder.queryParam(AconexConstants.PARAM_PAGE_NUMBER, 1);
        uriBuilder.queryParam(AconexConstants.PARAM_RETURN_FIELDS, AconexConstants.RETURN_FIELDS);

        return uriBuilder.build();
    }

    public static URI buildDownloadDocumentsUri(@NonNull String apiRootPath, String projectId, String documentId) {
        return UriBuilder.fromPath(apiRootPath)
                .path(AconexConstants.PROJECTS)
                .path(projectId)
                .path(AconexConstants.REGISTER)
                .path(documentId)
                .path(AconexConstants.MARKEDUP)
                .build();
    }
}
