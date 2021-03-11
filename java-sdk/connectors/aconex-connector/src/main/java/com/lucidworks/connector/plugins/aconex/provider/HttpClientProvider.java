package com.lucidworks.connector.plugins.aconex.provider;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.inject.Inject;
import javax.inject.Provider;

public class HttpClientProvider implements Provider<CloseableHttpClient> {

    @Inject
    public HttpClientProvider() {}

    @Override
    public CloseableHttpClient get() {
        return HttpClients.createDefault();
    }
}
