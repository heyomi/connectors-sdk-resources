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
import java.util.List;

@Slf4j
public class AconexFetcher implements ContentFetcher {
    private static final String ERROR_MSG = "Item=%s failed with error=%s";

    private final AconexClient service;
    private final AconexConfig config;
    private List<String> projects;

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
        // Map<String, Object> metaData = input.getMetadata();
        log.trace("Fetching input={}", input);

        try {
            /*if (!input.hasId() || input.getId().startsWith(CHECKPOINT_PREFIX)) {
                long currentJobRunDateTime = Instant.now().toEpochMilli();
                long lastJobRunDateTime = 0;
                if (metaData.containsKey(LAST_JOB_RUN_DATE_TIME)) {
                    // extract the lastJobRunDateTime from the checkpoint coming from crawlDb
                    // it represents the last time a job was run (previous to this current crawl)
                    lastJobRunDateTime = (Long) metaData.get(LAST_JOB_RUN_DATE_TIME);
                }

                DocumentListProcessor processor = new DocumentListProcessor(context, config, service, lastJobRunDateTime);
                processor.createNewDocuments();

                // add/update the checkpoint
                emitCheckpoint(
                        context,
                        currentJobRunDateTime,
                        getEntryIndexStart(input),
                        getEntryIndexEnd(input)
                );
            } else {
                DocumentListProcessor processor = new DocumentListProcessor(context, config, service, metaData);
                processor.createNewDocuments();
            }*/

            DocumentProcessor processor = new DocumentProcessor(context, config, service, 0);
            processor.process();
        } catch (Exception e) {
            context.newError(input.getId(), String.format(ERROR_MSG, input.getId(), e.getMessage())).emit();
        }
        return context.newResult();
    }
}
