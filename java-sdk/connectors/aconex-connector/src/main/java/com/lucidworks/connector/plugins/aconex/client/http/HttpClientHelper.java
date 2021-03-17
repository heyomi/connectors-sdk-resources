package com.lucidworks.connector.plugins.aconex.client.http;

import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;

import javax.ws.rs.core.HttpHeaders;
import java.net.URI;
import java.util.Base64;

import static com.lucidworks.connector.plugins.aconex.model.Constants.HTTP_HEADER_APPLICATION_KEY;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class HttpClientHelper {
    private String hostname;
    private String apiKey;
    private String username;
    private String password;
    private int connectionTimeout;

    public static HttpGet createHttpRequest(@NonNull URI uri, @NonNull AconexConfig config) {
        HttpGet request = new HttpGet(uri);

        // add request headers
        request.addHeader(HTTP_HEADER_APPLICATION_KEY, config.properties().api().apiKey());
        request.addHeader(HttpHeaders.AUTHORIZATION, encodeBasicAuth(config.properties().auth().basic().username(), config.properties().auth().basic().password()));

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(config.properties().timeout().connection())
                .setConnectTimeout(config.properties().timeout().connection())
                .setSocketTimeout(config.properties().timeout().socket())
                .build();

        request.setConfig(requestConfig);

        return request;
    }

    private static String encodeBasicAuth(@NonNull String username, @NonNull String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }
}
