package com.lucidworks.connector.plugins.aconex.fetcher;

import com.lucidworks.connector.plugins.aconex.client.AconexClient;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.StartResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.StopResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

// import com.lucidworks.connectors.components.processor.ProcessorRunner;

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
    logger.trace("Starting Job:{}", context.getJobRunInfo().getId());
    return context.newResult();
  }

  @Override
  public StopResult stop(StopContext context) {
    logger.trace("Stopping Job:{}", context.getJobRunInfo().getId());
    return context.newResult();
  }

  @Override
  public FetchResult fetch(FetchContext context) {
    FetchInput input = context.getFetchInput();
    logger.trace("Fetching input={}", input);
    return null;

    // return processorRunner.process(ctx, input);
  }

}
