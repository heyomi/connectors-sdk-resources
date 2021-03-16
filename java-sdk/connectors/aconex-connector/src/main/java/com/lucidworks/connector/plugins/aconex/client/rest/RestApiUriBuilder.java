package com.lucidworks.connector.plugins.aconex.client.rest;

import lombok.NonNull;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static com.lucidworks.connector.plugins.aconex.model.Constants.*;

public class RestApiUriBuilder {

    public static URI buildProjectsUri(@NonNull String hostname) {
        return UriBuilder.fromPath(hostname).path(API).path(PROJECTS).build();
    }

    public static URI buildCountDocumentsUri(@NonNull String hostname, @NonNull String projectId) {
        return UriBuilder.fromPath(hostname)
                .path(API)
                .path(PROJECTS)
                .path(projectId)
                .path(REGISTER)
                .queryParam(PARAM_SEARCH_TYPE, SEARCH_TYPE_COUNT)
                .build();
    }

    public static URI buildDocumentsUri(@NonNull String hostname, @NonNull String projectId) {
        return buildDocumentsUri(hostname, projectId, DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
    }

    public static URI buildDocumentsUri(@NonNull String hostname, @NonNull String projectId, int pageNumber, int pageSize) {
        if (pageSize == 0 || pageNumber == 0) return buildDocumentsUri(hostname, projectId);

        UriBuilder uriBuilder = UriBuilder.fromPath(hostname)
                .path(API)
                .path(PROJECTS)
                .path(projectId)
                .path(REGISTER);

        uriBuilder.queryParam(PARAM_SEARCH_TYPE, SEARCH_TYPE_PAGED)
                .queryParam(PARAM_PAGE_SIZE, pageSize)
                .queryParam(PARAM_PAGE_NUMBER, pageNumber)
                .queryParam(PARAM_RETURN_FIELDS, RETURN_FIELDS);

        return uriBuilder.build();
    }

    public static URI buildDocumentsUri(@NonNull String hostname, @NonNull String projectId, int pageNumber, int pageSize, String returnFields) {
        if (pageSize <= 0 || pageNumber <= 0) return buildDocumentsUri(hostname, projectId);
        if (returnFields == null) returnFields = RETURN_FIELDS;
        if (pageSize % DEFAULT_PAGE_SIZE_DIVISOR != 0) pageSize = 25;

        UriBuilder uriBuilder = UriBuilder.fromPath(hostname)
                .path(API)
                .path(PROJECTS)
                .path(projectId)
                .path(REGISTER);

        uriBuilder.queryParam(PARAM_SEARCH_TYPE, SEARCH_TYPE_PAGED)
                .queryParam(PARAM_PAGE_SIZE, pageSize)
                .queryParam(PARAM_PAGE_NUMBER, pageNumber)
                .queryParam(PARAM_RETURN_FIELDS, returnFields)
                .queryParam(PARAM_SORT_FIELD, DEFAULT_SORT_FIELD); // default direction ASC; process smaller files

        return uriBuilder.build();
    }

    public static URI buildLimitedDocumentsUri(@NonNull String hostname, @NonNull String projectId, int pageSize, String returnFields) {
        if (returnFields == null) returnFields = RETURN_FIELDS;
        if (pageSize % DEFAULT_PAGE_SIZE_DIVISOR != 0) pageSize = DEFAULT_PAGE_SIZE;

        UriBuilder uriBuilder = UriBuilder.fromPath(hostname)
                .path(API)
                .path(PROJECTS)
                .path(projectId)
                .path(REGISTER);

        uriBuilder.queryParam(PARAM_SEARCH_TYPE, SEARCH_TYPE_NUMBERED)
                .queryParam(PARAM_SEARCH_RESULT_SIZE, pageSize)
                .queryParam(PARAM_RETURN_FIELDS, returnFields)
                .queryParam(PARAM_SORT_FIELD, DEFAULT_SORT_FIELD); // default direction ASC; process smaller files

        return uriBuilder.build();
    }

    public static URI buildDownloadDocumentsUri(@NonNull String hostname, String projectId, String documentId) {
        return UriBuilder.fromPath(hostname)
                .path(API)
                .path(PROJECTS)
                .path(projectId)
                .path(REGISTER)
                .path(documentId)
                .path(MARKEDUP)
                .build();
    }

    @Deprecated
    public static String buildDocumentViewerUri(@NonNull String projectId, @NonNull String documentId) {
        return "https://app35.qa.acx/ViewDoc?docid=" + documentId + "&projectid=" + projectId;
    }

    public static String buildDocumentViewerUri(@NonNull String host, @NonNull String projectId, @NonNull String documentId) {
        return host + "/ViewDoc?cversion=1&tab=0&trackingid=" + documentId + "&projectid=" + projectId;
    }
}
