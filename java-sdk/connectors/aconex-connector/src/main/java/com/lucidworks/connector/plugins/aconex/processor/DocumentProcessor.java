package com.lucidworks.connector.plugins.aconex.processor;

import com.lucidworks.connector.plugins.aconex.client.AconexClient;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.model.Document;
import com.lucidworks.connector.plugins.aconex.model.Project;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static com.lucidworks.connector.plugins.aconex.model.Constants.LAST_JOB_RUN_DATE_TIME;

@Slf4j
public class DocumentProcessor {
    private final ContentFetcher.FetchContext context;
    private final AconexConfig config;
    private final AconexClient service;

    public DocumentProcessor(ContentFetcher.FetchContext context, AconexConfig config, AconexClient service) {
        this.context = context;
        this.config = config;
        this.service = service;
    }

    public void process() {
        log.info("Starting New Content Phase in Job:{}", context.getJobRunInfo().getId());

        int totalPages;
        int pageNumber = 1;
        long i = 0;
        int maxItems = config.properties().limit().maxItems();

        try {
            // get projects
            final List<Project> projects = service.getProjects();

            for (Project p : projects) {
                log.info("Starting on project:{}", p.getProjectID());

                totalPages = p.getTotalPages();
                while (pageNumber <= totalPages) {
                    // get documents
                    List<Document> documents = service.getDocuments(p.getProjectID(), pageNumber);

                    // add document content
                    for (Document d : documents) {
                        if (maxItems > -1 && i >= maxItems) break; // SP-62: Create a better way to handle this.
                        context.newContent(d.getUrl(), service.getDocument(p.getProjectID(), d.getId()))
                                .fields(f -> {
                                    f.merge(d.toMetadata());
                                    f.setString("project_id_t", p.getProjectID());
                                    f.setString("project_name_t", p.getProjectName());
                                    f.setLong(LAST_JOB_RUN_DATE_TIME, Instant.now().toEpochMilli());
                                })
                                .metadata(m -> m.setLong(LAST_JOB_RUN_DATE_TIME, Instant.now().toEpochMilli()))
                                .emit();
                        i++;
                    }
                    pageNumber++;
                }
                log.info("Processed page:{} of {} with {} new documents", pageNumber, totalPages, i);
            }
        } catch (IOException e) {
            log.error("An error occurred", e);
        }
    }
}
