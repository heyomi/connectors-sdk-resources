package com.lucidworks.connector.plugins.feed.client.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lucidworks.connector.plugins.feed.client.rest.RestApiUriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AconexHttpClient {
    private static final Logger logger = LoggerFactory.getLogger(AconexHttpClient.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private final HttpClient httpClient;
    private final RestApiUriBuilder restApiUriBuilder;

    @Inject
    public AconexHttpClient(HttpClient httpClient, RestApiUriBuilder restApiUriBuilder) {
        this.httpClient = httpClient;
        this.restApiUriBuilder = restApiUriBuilder;
    }

    public void projects() {
        try {
            final URI uri = restApiUriBuilder.buildProjectsUri(null);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .setHeader("Accept", "application/json")
                    .build();

            HttpResponse<String> response = null;
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // print response headers
            HttpHeaders headers = response.headers();
            headers.map().forEach((k, v) -> System.out.println(k + ":" + v));

            // print status code
            System.out.println(response.statusCode());

            // print response body
            System.out.println(response.body());
        } catch (IOException | InterruptedException e) {
            logger.error("An error occurred while getting projects", e);
        }
    }
}
