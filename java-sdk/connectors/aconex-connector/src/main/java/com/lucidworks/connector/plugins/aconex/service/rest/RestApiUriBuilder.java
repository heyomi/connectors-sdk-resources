package com.lucidworks.connector.plugins.aconex.service.rest;

import com.lucidworks.connector.plugins.aconex.model.Constants;
import lombok.NonNull;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static com.lucidworks.connector.plugins.aconex.model.Constants.*;

public class RestApiUriBuilder {

    public static URI buildProjectsUri(@NonNull String hostname) {
        return UriBuilder.fromPath(hostname).path(API).path(Constants.PROJECTS).build();
    }

    public static URI buildDocumentsUri(@NonNull String hostname, @NonNull String projectId) {
        return buildDocumentsUri(hostname, projectId, DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
    }

    public static URI buildDocumentsUri(@NonNull String hostname, @NonNull String projectId, int pageNumber, int pageSize) {
        if (pageSize == 0 || pageNumber == 0)
            return buildDocumentsUri(hostname, projectId);

        UriBuilder uriBuilder = UriBuilder.fromPath(hostname)
                .path(API)
                .path(Constants.PROJECTS)
                .path(projectId)
                .path(Constants.REGISTER);

        uriBuilder.queryParam(Constants.PARAM_SEARCH_TYPE, Constants.SEARCH_TYPE_PAGED)
                .queryParam(Constants.PARAM_PAGE_SIZE, pageSize)
                .queryParam(Constants.PARAM_PAGE_NUMBER, pageNumber)
                .queryParam(Constants.PARAM_RETURN_FIELDS, Constants.RETURN_FIELDS);

        return uriBuilder.build();
    }

    public static URI buildDownloadDocumentsUri(@NonNull String hostname, String projectId, String documentId) {
        return UriBuilder.fromPath(hostname)
                .path(API)
                .path(Constants.PROJECTS)
                .path(projectId)
                .path(Constants.REGISTER)
                .path(documentId)
                .path(Constants.MARKEDUP)
                .build();
    }

    public static String buildDocumentViewerUri(@NonNull String projectId, @NonNull String documentId) {
        return "https://app35.qa.acx/ViewDoc?docid=" + documentId + "&projectid=" + projectId;
    }
}
