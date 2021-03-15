package com.lucidworks.connector.plugins.aconex.fetcher;

import com.lucidworks.connector.plugins.aconex.client.AconexClient;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.processor.DocumentListProcessor;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.StartResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.StopResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static com.lucidworks.connector.plugins.aconex.model.Constants.*;

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
        Map<String, Object> metaData = input.getMetadata();
        log.trace("Fetching input={}", input);

        try {
            if (!input.hasId() || input.getId().startsWith(CHECKPOINT_PREFIX)) {
                long currentJobRunDateTime = Instant.now().toEpochMilli();
                long lastJobRunDateTime = 0;
                if (metaData.containsKey(LAST_JOB_RUN_DATE_TIME)) {
                    // extract the lastJobRunDateTime from the checkpoint coming from crawlDb
                    // it represents the last time a job was run (previous to this current crawl)
                    lastJobRunDateTime = (Long) metaData.get(LAST_JOB_RUN_DATE_TIME);
                }

                DocumentListProcessor processor = new DocumentListProcessor(context, config, service, lastJobRunDateTime);
                processor.createNewCandidates();

                // add/update the checkpoint
                /* emitCheckpoint(
                        context,
                        currentJobRunDateTime,
                        getEntryIndexStart(input),
                        getEntryIndexEnd(input)
                ); */
            } else {
                // processFeedEntry(fetchContext, input, metaData);
            }
        } catch (Exception e) {
            context.newError(input.getId(), String.format(ERROR_MSG, input.getId(), e.getMessage())).emit();
        }
        return context.newResult();
    }

    private void createNewDocuments(FetchContext context, Map<String, Map<String, Object>> content) {
        content.keySet().forEach(key -> {
            Map<String, Object> data = content.get(key);
            log.info("creating doc key:{}", key);
            context.newDocument(key)
                    .fields(f -> {
                        f.setLong("timestamp", ZonedDateTime.now().toEpochSecond());
                        f.merge(data);
                    })
                    .emit();
        });
    }

    private void emitCheckpoint(FetchContext context, int pageNumber, int totalResults, int totalPages, int totalOnPage) {
        log.info("Emit Checkpoint");
        context.newCheckpoint(CHECKPOINT_PREFIX)
                .metadata(m -> {
                    // m.setInteger(TOTAL_INDEXED, totalIndexed);
                    m.setInteger(PAGE_NUMBER, pageNumber);
                    m.setInteger(PAGE_SIZE, config.properties().limit().pageSize());
                    m.setInteger(TOTAL_RESULTS, totalResults);
                    m.setInteger(TOTAL_PAGES, totalPages);
                    m.setInteger(TOTAL_ON_PAGE, totalOnPage);
                    m.setLong("lastJobRunDateTime", ZonedDateTime.now().toEpochSecond());
                })
                .emit();
    }
}
