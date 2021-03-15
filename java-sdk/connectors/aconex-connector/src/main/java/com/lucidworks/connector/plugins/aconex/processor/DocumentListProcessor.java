package com.lucidworks.connector.plugins.aconex.processor;

import com.lucidworks.connector.plugins.aconex.client.AconexClient;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.model.Document;
import com.lucidworks.connector.plugins.aconex.model.Project;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

import static com.lucidworks.connector.plugins.aconex.model.Constants.ENTRY_LAST_UPDATED;
import static com.lucidworks.connector.plugins.aconex.model.Constants.LAST_JOB_RUN_DATE_TIME;

@Slf4j
public class DocumentListProcessor {

    private final ContentFetcher.FetchContext context;
    private final AconexConfig config;
    private final AconexClient service;
    private final long lastJobRunDateTime;

    public DocumentListProcessor(ContentFetcher.FetchContext context, AconexConfig config, AconexClient service, long lastJobRunDateTime) {
        this.context = context;
        this.config = config;
        this.service = service;
        this.lastJobRunDateTime = lastJobRunDateTime;
    }

    public void createNewCandidates() {
        log.trace("Starting Job:{}", context.getJobRunInfo().getId());

        int totalPages = 1;
        int pageNumber = 1;
        long i = 0;
        int maxItems = config.properties().limit().maxItems();

        try {
            // get projects
            final List<Project> projects = service.getProjects();

            for (Project p : projects) {
                totalPages = p.getTotalPages();

                log.info("Starting on project:{}", p.getProjectID());

                while(pageNumber <= totalPages) {

                    log.info("On page:{} of {}", pageNumber, totalPages);

                    if (i >= maxItems) {
                        log.info("Max item limit reached");
                        break;
                    } else {
                        // get documents
                        List<Document> documents = service.getDocuments(p.getProjectID(), pageNumber);
                        documents.forEach(d -> {
                            // add document metadata
                            context.newCandidate(d.getId())
                                    .metadata(m -> {
                                        m.setString("title", d.getTitle());
                                        m.setString("category", d.getCategory());
                                        m.setString("discipline", d.getDiscipline());
                                        m.setString("document_status", d.getDocumentStatus());
                                        m.setString("document_type", d.getDocumentType());
                                        m.setString("file_name", d.getFilename());
                                        m.setString("file_type", d.getFileType());
                                        m.setInteger("file_size", d.getFileSize());
                                        m.setString("url", d.getUrl());
                                        m.setLong("dateModified", d.getDateModified().getTime());
                                        // add last time when entry was modified
                                        m.setLong(ENTRY_LAST_UPDATED, d.getLastUpdated());
                                        // add 'lastJobRunDateTime'.
                                        m.setLong(LAST_JOB_RUN_DATE_TIME, lastJobRunDateTime);
                                    })
                                    .emit();

                            log.info("Emitting candidate {}", d.getId());
                        });
                        pageNumber++;
                        i = i + documents.size();
                    }


                }

            }
        } catch (IOException e) {
            log.error("An error occurred", e);
        }
    }
}
