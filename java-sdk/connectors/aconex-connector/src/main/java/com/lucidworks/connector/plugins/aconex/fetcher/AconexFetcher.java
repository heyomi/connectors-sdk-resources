package com.lucidworks.connector.plugins.aconex.fetcher;

import com.lucidworks.connector.plugins.aconex.client.AconexClient;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.processor.DocumentProcessor;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.StartResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.StopResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class AconexFetcher implements ContentFetcher {
    private static final String ERROR_MSG = "Item=%s failed with error=%s";

    private final AconexClient service;
    private final AconexConfig config;

    @Inject
    public AconexFetcher(AconexClient service, AconexConfig config) {
        this.service = service;
        this.config = config;
    }

    @Override
    public StartResult start(StartContext context) {
        log.trace("Starting Job:{}", context.getJobRunInfo().getId());
        return context.newResult();
    }

    @Override
    public StopResult stop(StopContext context) {
        log.trace("Stopping Job:{}", context.getJobRunInfo().getId());
        return context.newResult();
    }

    @Override
    public FetchResult fetch(FetchContext context) {
        FetchInput input = context.getFetchInput();
        log.trace("Fetching input={}", input);

        try {
            new DocumentProcessor(context, config, service).process();
        } catch (Exception e) {
            context.newError(input.getId(), String.format(ERROR_MSG, input.getId(), e.getMessage())).emit();
        }
        return context.newResult();
    }
}
