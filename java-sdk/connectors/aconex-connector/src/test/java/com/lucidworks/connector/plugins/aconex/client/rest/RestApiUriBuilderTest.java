package com.lucidworks.connector.plugins.aconex.client.rest;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class RestApiUriBuilderTest {
    private final String apiRoot = "https://apidev.aconex.com/api";

    @Test
    void buildProjectsUri() {
        RestApiUriBuilder builder = new RestApiUriBuilder();
        URI uri = builder.buildProjectsUri(apiRoot);

        assertThat(uri.toASCIIString(), is("https://apidev.aconex.com/api/projects"));
    }

    @Test
    void buildDocumentsUri() {
        RestApiUriBuilder builder = new RestApiUriBuilder();
        URI uri = builder.buildDocumentsUri(apiRoot, "0123456789");

        assertThat(uri.toString(), is("https://apidev.aconex.com/api/projects/0123456789/register?search_type=PAGED&page_size=25&page_number=1&return_fields=title%2Cfilename%2Cauthor%2Cdoctype%2CfileSize%2CfileType%2Cconfidential%2Cstatusid"));
    }

    @Test
    void buildFetchDocumentsUri() {
        RestApiUriBuilder builder = new RestApiUriBuilder();
        URI uri = builder.buildDownloadDocumentsUri(apiRoot, "0123456789", "9876543210");

        assertThat(uri.toASCIIString(), is("https://apidev.aconex.com/api/projects/0123456789/register/9876543210/markedup"));
    }

}