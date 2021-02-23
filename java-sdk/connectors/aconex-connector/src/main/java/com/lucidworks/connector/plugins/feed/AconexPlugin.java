package com.lucidworks.connector.plugins.feed;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.lucidworks.connector.plugins.feed.config.AconexConfig;
import com.lucidworks.connector.plugins.feed.feed.DefaultFeedGenerator;
import com.lucidworks.connector.plugins.feed.feed.FeedGenerator;
import com.lucidworks.connector.plugins.feed.fetcher.AconexFetcher;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider;

public class AconexPlugin implements ConnectorPluginProvider {

  @Override
  public ConnectorPlugin get() {
    Module fetchModule = new AbstractModule() {
      @Override
      protected void configure() {
        bind(FeedGenerator.class)
            .to(DefaultFeedGenerator.class)
            .asEagerSingleton();
      }
    };

    return ConnectorPlugin.builder(AconexConfig.class)
        .withFetcher("content", AconexFetcher.class, fetchModule)
        .build();
  }
}