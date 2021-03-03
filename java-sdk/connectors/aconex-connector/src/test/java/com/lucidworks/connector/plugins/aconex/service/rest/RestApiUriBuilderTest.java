package com.lucidworks.connector.plugins.aconex.service.rest;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static com.lucidworks.connector.plugins.aconex.model.Constants.DEFAULT_PAGE_SIZE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class RestApiUriBuilderTest {
    private final String apiRoot = "https://apidev.aconex.com/api";

    @Test
    void buildProjectsUri() {
        URI uri = RestApiUriBuilder.buildProjectsUri(apiRoot);

        assertThat(uri.toASCIIString(), is("https://apidev.aconex.com/api/projects"));
    }

    @Test
    void buildDocumentsUri() {
        URI uri = RestApiUriBuilder.buildDocumentsUri(apiRoot, "0123456789");

        assertThat(uri.toASCIIString(), is("https://apidev.aconex.com/api/projects/0123456789/register?search_type=PAGED&page_size="
                + DEFAULT_PAGE_SIZE + "&page_number=1&return_fields=title,filename,author,doctype,fileSize,fileType,confidential,statusid"));
    }

    @Test
    void buildFetchDocumentsUri() {
        URI uri = RestApiUriBuilder.buildDownloadDocumentsUri(apiRoot, "0123456789", "9876543210");

        assertThat(uri.toASCIIString(), is("https://apidev.aconex.com/api/projects/0123456789/register/9876543210/markedup"));
    }

}