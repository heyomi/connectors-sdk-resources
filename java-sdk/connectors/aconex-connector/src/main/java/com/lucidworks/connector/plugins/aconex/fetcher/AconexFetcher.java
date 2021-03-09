package com.lucidworks.connector.plugins.aconex.fetcher;

import com.lucidworks.connector.plugins.aconex.client.AconexClient;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.model.SearchResultsStats;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
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

// import com.lucidworks.connectors.components.processor.ProcessorRunner;

public class AconexFetcher implements ContentFetcher {

    private static final Logger logger = LoggerFactory.getLogger(AconexFetcher.class);
    private static final String ERROR_MSG = "Item=%s failed with error=%s";

    // private final ProcessorRunner processorRunner;
    private final AconexClient client;
    private final AconexConfig config;
    private List<String> projects;

    @Inject
    public AconexFetcher(AconexClient client, AconexConfig config) {
        this.client = client;
        this.config = config;
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
            /*if (!input.hasId()) {
                logger.info("FetchInput is null");

                for (String project : projects) {
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

                return context.newResult();
            }*/

            process(context);
        } catch (Exception e) {
            context.newError(input.getId(), String.format(ERROR_MSG, input.getId(), e.getMessage())).emit();
        }
        return context.newResult();
    }

    private void process(FetchContext context){
        int pageNumber = 1;

        for (String id : client.getProjectIds()) {
            handleProject(context, id);
            int pageSize = config.properties().item().pageSize();
            Map<String, Map<String, Object>> content = client.getDocumentsByProject(id, pageNumber, pageSize);
            SearchResultsStats stats = client.getSearchResultsStats();
            createNewDocuments(context, content);

            while (stats.getTotalPages() > stats.getCurrentPage()) {
                logger.info("stats:{}", stats);

                content = client.getDocumentsByProject(id, ++pageNumber, pageSize);
                stats = client.getSearchResultsStats();
                createNewDocuments(context, content);
            }
        }
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
                    m.setInteger(PAGE_SIZE, config.properties().item().pageSize());
                    m.setInteger(TOTAL_RESULTS, totalResults);
                    m.setInteger(TOTAL_PAGES, totalPages);
                    m.setInteger(TOTAL_ON_PAGE, totalOnPage);
                    m.setLong("lastJobRunDateTime", ZonedDateTime.now().toEpochSecond());
                })
                .emit();
    }
}
