package com.lucidworks.connector.plugins.aconex.service.rest;

import com.lucidworks.connector.plugins.aconex.client.rest.RestApiUriBuilder;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static com.lucidworks.connector.plugins.aconex.model.Constants.DEFAULT_PAGE_SIZE;
import static com.lucidworks.connector.plugins.aconex.model.Constants.RETURN_FIELDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class RestApiUriBuilderTest {
    private final String apiRoot = "https://apidev.aconex.com";

    @Test
    void shouldReturnProjectsUri() {
        URI uri = RestApiUriBuilder.buildProjectsUri(apiRoot);

        assertThat(uri.toString(), is("https://apidev.aconex.com/api/projects"));
    }

    @Test
    void shouldReturnDocumentsUri() {
        URI uri = RestApiUriBuilder.buildDocumentsUri(apiRoot, "0123456789");

        assertThat(uri.toString(), is("https://apidev.aconex.com/api/projects/0123456789/register?search_type=PAGED&page_size="
                + DEFAULT_PAGE_SIZE + "&page_number=1&return_fields=" + RETURN_FIELDS.replace(",", "%2C")));
    }

    @Test
    void shouldReturnDocumentsUriWithParams() {
        URI uri = RestApiUriBuilder.buildDocumentsUri(apiRoot, "0123456789", 2, 50, "title");

        assertThat(uri.toString(), is("https://apidev.aconex.com/api/projects/0123456789/register?search_type=PAGED&page_size=50&page_number=2&return_fields=title&sort_field=fileSize"));
    }

    @Test
    void shouldReturnDocumentsUriWithInvalidPageSize() {
        URI uri = RestApiUriBuilder.buildDocumentsUri(apiRoot, "0123456789", 1, 39, "title");

        assertThat(uri.toString(), is("https://apidev.aconex.com/api/projects/0123456789/register?search_type=PAGED&page_size="
                + DEFAULT_PAGE_SIZE + "&page_number=1&return_fields=title&sort_field=fileSize"));
    }

    @Test
    void shouldReturnDocumentsUriWithInvalidParams() {
        URI uri = RestApiUriBuilder.buildDocumentsUri(apiRoot, "0123456789", 0, -1, null);

        assertThat(uri.toString(), is("https://apidev.aconex.com/api/projects/0123456789/register?search_type=PAGED&page_size="
                + DEFAULT_PAGE_SIZE + "&page_number=1&return_fields=" + RETURN_FIELDS.replace(",", "%2C")));
    }

    @Test
    void shouldReturnFetchDocumentsUri() {
        URI uri = RestApiUriBuilder.buildDownloadDocumentsUri(apiRoot, "0123456789", "9876543210");

        assertThat(uri.toString(), is("https://apidev.aconex.com/api/projects/0123456789/register/9876543210/markedup"));
    }

    @Test
    void shouldReturnDocumentViewerUri() {
        String uri = RestApiUriBuilder.buildDocumentViewerUri(apiRoot, "0123456789", "9876543210");

        assertThat(uri.toString(), is("https://apidev.aconex.com/ViewDoc?cversion=1&tab=0&trackingid=9876543210&projectid=0123456789"));
    }

}