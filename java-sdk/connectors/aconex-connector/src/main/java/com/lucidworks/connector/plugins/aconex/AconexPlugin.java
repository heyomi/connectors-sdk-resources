package com.lucidworks.connector.plugins.aconex;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.lucidworks.connector.plugins.aconex.client.AconexClient;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.fetcher.AconexFetcher;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AconexPlugin implements ConnectorPluginProvider {

  private static final Logger logger = LoggerFactory.getLogger(AconexPlugin.class);

  @Override
  public ConnectorPlugin get() {
    logger.info("Creating ConnectorPlugin Instance");

    Module fetchModule = new AbstractModule() {
      @Override
      protected void configure() {
        bind(AconexClient.class).asEagerSingleton();
      }
    };

    return ConnectorPlugin.builder(AconexConfig.class)
        .withFetcher("content", AconexFetcher.class, fetchModule)
        .build();
  }
}