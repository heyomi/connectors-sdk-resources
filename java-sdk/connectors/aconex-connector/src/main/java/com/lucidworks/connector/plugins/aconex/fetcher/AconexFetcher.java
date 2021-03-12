package com.lucidworks.connector.plugins.aconex.fetcher;

import com.lucidworks.connector.plugins.aconex.client.AconexClient;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.processor.DocumentListProcessor;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.PreFetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.StartResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.StopResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static com.lucidworks.connector.plugins.aconex.model.Constants.*;

public class AconexFetcher implements ContentFetcher {

    private static final Logger logger = LoggerFactory.getLogger(AconexFetcher.class);
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
        logger.trace("Starting Job:{}", context.getJobRunInfo().getId());
        return context.newResult();
    }

    @Override
    public PreFetchResult preFetch(PreFetchContext context) {
        logger.trace("Starting Job:{}", context.getJobRunInfo().getId());
        DocumentListProcessor processor = new DocumentListProcessor(config, service);

        return processor.process(context);
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
        } catch (Exception e) {
            context.newError(input.getId(), String.format(ERROR_MSG, input.getId(), e.getMessage())).emit();
        }
        return context.newResult();
    }

    private void handleProject(FetchContext context, String project) {
        logger.info("emitting project: {}", project);
        context.newCandidate(project)
                .withIsLeafNode(false)
                .withTransient(true)
                .metadata(m -> {
                            m.setString(TYPE_FIELD, "project");
                            m.setString(PROJECT_ID_FIELD, project);
                        }
                )
                .emit();
    }

    private void createNewDocuments(FetchContext context, Map<String, Map<String, Object>> content) {
        content.keySet().forEach(key -> {
            Map<String, Object> data = content.get(key);
            logger.info("creating doc key:{}", key);
            context.newDocument(key)
                    .fields(f -> {
                        f.setLong("timestamp", ZonedDateTime.now().toEpochSecond());
                        f.merge(data);
                    })
                    .emit();
        });
    }

    private void emitCheckpoint(FetchContext context, int pageNumber, int totalResults, int totalPages, int totalOnPage) {
        logger.info("Emit Checkpoint");
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
