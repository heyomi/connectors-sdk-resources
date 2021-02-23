package com.lucidworks.connector.plugins.feed.fetcher;

import com.lucidworks.connector.plugins.feed.AconexClient;
import com.lucidworks.connector.plugins.feed.config.AconexConfig;
import com.lucidworks.connectors.components.processor.ProcessorRunner;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.StartResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.StopResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

public class AconexFetcher implements ContentFetcher {

  private static final Logger logger = LoggerFactory.getLogger(AconexFetcher.class);

  // private final ProcessorRunner processorRunner;

  private final AconexConfig config;
  private final AconexClient client;

  @Inject
  public AconexFetcher(
          AconexConfig config,
          AconexClient client) {
    this.config = config;
    this.client = client;
  }

  @Override
  public StartResult start(StartContext context) {
    try {
      client.open();
    } catch (IOException e) {
      logger.warn("Problem opening couchbase client", e);
    }
    return context.newResult();
  }

  @Override
  public StopResult stop(StopContext context) {
    if (client != null) {
      try {
        client.close();
      } catch (Exception e) {
        logger.warn("Problem closing", e);
      }
    }
    return context.newResult();
  }

  @Override
  public FetchResult fetch(FetchContext ctx) {
    FetchInput input = ctx.getFetchInput();
    logger.trace("Fetching input={}", input);
    return processorRunner.process(ctx, input);
  }

}
