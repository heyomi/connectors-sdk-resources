package com.lucidworks.connector.plugins.aconex.fetcher;

import com.lucidworks.connector.plugins.aconex.client.AconexService;
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
  private final AconexService service;

  @Inject
  public AconexFetcher(AconexConfig config) {
    this.config = config;
    this.service = new AconexService(config.properties().auth(), config.properties().timeout(), config.properties().additional());
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

    try {
      service.getContent();

    } catch (Exception e) {
      String message = "Failed to parse content from Aconex!";
      logger.error(message, e);
      context.newError(context.getFetchInput().getId())
              .withError(message)
              .emit();
    }

    return context.newResult();

    // return processorRunner.process(ctx, input);
  }

}
