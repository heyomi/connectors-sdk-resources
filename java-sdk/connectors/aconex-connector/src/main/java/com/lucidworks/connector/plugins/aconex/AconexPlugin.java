package com.lucidworks.connector.plugins.aconex;

import com.google.inject.AbstractModule;
import com.lucidworks.connector.plugins.aconex.client.AconexClient;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.fetcher.AconexFetcher;
import com.lucidworks.connector.plugins.aconex.provider.AconexProvider;
import com.lucidworks.connector.plugins.aconex.provider.HttpClientProvider;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

public class AconexPlugin implements ConnectorPluginProvider {

    private static final Logger logger = LoggerFactory.getLogger(AconexPlugin.class);

    @Override
    public ConnectorPlugin get() {
        logger.info("Creating ConnectorPlugin Instance");

        AbstractModule nonGenModule = new AbstractModule() {
            @Override
            protected void configure() {
                bind(AconexClient.class).toProvider(AconexProvider.class).in(Singleton.class);
                bind(CloseableHttpClient.class).toProvider(HttpClientProvider.class).in(Singleton.class);
            }
        };

        return ConnectorPlugin.builder(AconexConfig.class)
                .withFetcher("content", AconexFetcher.class, nonGenModule)
                .build();
    }
}