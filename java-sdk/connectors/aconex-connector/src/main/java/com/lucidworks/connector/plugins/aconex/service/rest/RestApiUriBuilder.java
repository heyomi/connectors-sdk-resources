package com.lucidworks.connector.plugins.aconex.service.rest;

import com.lucidworks.connector.plugins.aconex.model.Constants;
import lombok.NonNull;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static com.lucidworks.connector.plugins.aconex.model.Constants.DEFAULT_PAGE_SIZE;

public class RestApiUriBuilder {

    public static URI buildProjectsUri(@NonNull String apiRootPath) {
        return UriBuilder.fromPath(apiRootPath).path(Constants.PROJECTS).build();
    }

    public static URI buildDocumentsUri(@NonNull String apiRootPath, String projectId) {
        UriBuilder uriBuilder = UriBuilder.fromPath(apiRootPath)
                .path(Constants.PROJECTS)
                .path(projectId)
                .path(Constants.REGISTER);

        uriBuilder.queryParam(Constants.PARAM_SEARCH_TYPE, Constants.SEARCH_TYPE_PAGED);
        uriBuilder.queryParam(Constants.PARAM_PAGE_SIZE, DEFAULT_PAGE_SIZE);
        uriBuilder.queryParam(Constants.PARAM_PAGE_NUMBER, 1);
        uriBuilder.queryParam(Constants.PARAM_RETURN_FIELDS, Constants.RETURN_FIELDS);

        return uriBuilder.build();
    }

    public static URI buildDownloadDocumentsUri(@NonNull String apiRootPath, String projectId, String documentId) {
        return UriBuilder.fromPath(apiRootPath)
                .path(Constants.PROJECTS)
                .path(projectId)
                .path(Constants.REGISTER)
                .path(documentId)
                .path(Constants.MARKEDUP)
                .build();
    }
}
