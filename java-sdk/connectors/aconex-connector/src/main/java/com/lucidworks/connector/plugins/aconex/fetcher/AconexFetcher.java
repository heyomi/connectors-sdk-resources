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

    private static final String TYPE_FIELD = "_aconex_type";
    private static final String PROJECT_ID_FIELD = "_aconex_project_id";
    private static final String DOCUMENT_ID_FIELD = "_aconex_document_id";
    private static final String CANDIDATE_ID = "%s-%s";
    private static final String ERROR_MSG = "Item=%s failed with error=%s";

    // private final ProcessorRunner processorRunner;
    private final AconexClient client;
    private final AconexConfig config;
    private final List<String> projects;

    @Inject
    public AconexFetcher(AconexClient client, AconexConfig config) {
        // this.client = new AconexService(config.properties().auth(), config.properties().timeout(), config.properties().additional());
        this.client = client;
        this.config = config;
        this.projects = config.properties().projects();
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
            handleDocuments(context, input);
        } catch (Exception e) {
            context.newError(input.getId(), String.format(ERROR_MSG, input.getId(), e.getMessage())).emit();
        }

        return context.newResult();

        // return processorRunner.process(ctx, input);
    }

    private void emitCheckpoint(FetchContext context, int pageNumber, int totalResults, int totalPages, int totalOnPage) {
        logger.info("Emit Checkpoint");
        context.newCheckpoint(CHECKPOINT_PREFIX)
                .metadata(m -> {
                    // m.setInteger(TOTAL_INDEXED, totalIndexed);
                    m.setInteger(PAGE_NUMBER, pageNumber);
                    m.setInteger(PAGE_SIZE, config.properties().documentsPerPage());
                    m.setInteger(TOTAL_RESULTS, totalResults);
                    m.setInteger(TOTAL_PAGES, totalPages);
                    m.setInteger(TOTAL_ON_PAGE, totalOnPage);
                    m.setLong("lastJobRunDateTime", ZonedDateTime.now().toEpochSecond());
                })
                .emit();
    }

    private void handleDocuments(FetchContext context, FetchInput input) {
        Map<String, Map<String, Object>> content = client.getDocuments();
        SearchResultsStats stats = client.getSearchResultsStats();
        logger.debug(stats.toString());

        if (content != null && !content.isEmpty()) {
            for (String key : content.keySet()) {
                Map<String, Object> data = content.get(key);
                context.newDocument(key)
                        .fields(f -> {
                            f.setString("url", key); // TODO: Figure out how to get document URL from Aconex]
                            f.setLong("timestamp", ZonedDateTime.now().toEpochSecond());
                            f.setString(TYPE_FIELD, "doc");
                            f.setString(PROJECT_ID_FIELD, data.get("project:id").toString());
                            f.setString(DOCUMENT_ID_FIELD, data.get("document:id").toString());
                            f.merge(data);
                        })
                        .emit();
            }
        } else {
            context.newError(input.getId(), "Failed to store all Aconex Content.").emit();
        }

        // Checkpoint
        // emitCheckpoint(context, stats.getCurrentPage(), stats.getTotalResults(), stats.getTotalPages(), stats.getTotalResultsOnPage());
    }
}
