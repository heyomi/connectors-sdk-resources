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
import java.util.Map;

import static com.lucidworks.connector.plugins.aconex.model.Constants.LAST_JOB_RUN_DATE_TIME;

@Slf4j
public class DocumentProcessor {

    private final ContentFetcher.FetchContext context;
    private final AconexConfig config;
    private final AconexClient service;
    private long lastJobRunDateTime;
    private Map<String, Object> metaData;

    public DocumentProcessor(ContentFetcher.FetchContext context, AconexConfig config, AconexClient service, long lastJobRunDateTime) {
        this.context = context;
        this.config = config;
        this.service = service;
        this.lastJobRunDateTime = lastJobRunDateTime;
    }

    public void process() {
        log.trace("Starting Job:{}", context.getJobRunInfo().getId());

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
                while(pageNumber <= totalPages) {
                    if (maxItems >= 0 && i >= maxItems) { // SP-62: Create a better way to handle this.
                        log.info("Max item limit reached");
                        break;
                    } else {
                        // get documents
                        List<Document> documents = service.getDocuments(p.getProjectID(), pageNumber);
                        // add document metadata
                        for (Document d : documents) {

                            if (i >= maxItems) break; // SP-62: Create a better way to handle this.

                            /* context.newCandidate(d.getId())
                                    .metadata(m -> {
                                        m.setString("title", d.getTitle());
                                        // add last time when entry was modified
                                        m.setLong(ENTRY_LAST_UPDATED, Instant.now().toEpochMilli());
                                        // add 'lastJobRunDateTime'.
                                        m.setLong(LAST_JOB_RUN_DATE_TIME, Instant.now().toEpochMilli());
                                    }).emit(); */

                            context.newDocument(d.getUrl())
                                    .fields(f -> {
                                        // Create method that does this?
                                        f.setString("title", d.getTitle());
                                        f.setString("category", d.getCategory());
                                        f.setString("discipline", d.getDiscipline());
                                        f.setString("document_id", d.getId());
                                        f.setString("document_type", d.getDocumentType());
                                        f.setString("document_type", d.getDocumentType());
                                        f.setString("project_id", p.getProjectID());
                                        f.setString("project_name", p.getProjectName());
                                        f.setString("file_name", d.getFilename());
                                        f.setString("file_type", d.getFileType());
                                        f.setInteger("file_size", d.getFileSize());
                                        f.setString("url", d.getUrl());
                                        f.setLong("dateModified", d.getDateModified().getTime());
                                        f.setDate("dateModified", d.getDateModified());
                                        f.setString("select_list2", d.getSelect2());
                                        f.setString("select_list8", d.getSelect8());
                                        f.setLong(LAST_JOB_RUN_DATE_TIME, Instant.now().toEpochMilli());
                                    })
                                    .emit();
                            i++;
                        }
                        pageNumber++;
                    }
                    log.info("Processed page:{} of {} with {} new documents", pageNumber, totalPages, i);
                }

            }

            createNewContent(projects);
        } catch (IOException e) {
            log.error("An error occurred", e);
        }
    }

    private void createNewContent(List<Project> projects) {
        log.info("Starting New Content Phase in Job:{}", context.getJobRunInfo().getId());

        int totalPages;
        int pageNumber = 1;
        long i = 0;
        int maxItems = config.properties().limit().maxItems();

        try {
            for (Project p : projects) {
                log.info("Starting on project:{}", p.getProjectID());

                totalPages = p.getTotalPages();
                while(pageNumber <= totalPages) {
                    if (maxItems >= 0 && i >= maxItems) { // SP-62: Create a better way to handle this.
                        log.info("Max item limit reached");
                        break;
                    } else {
                        // get documents
                        List<Document> documents = service.getDocuments(p.getProjectID(), pageNumber);
                        // add document metadata
                        for (Document d : documents) {
                            if (i >= maxItems) break; // SP-62: Create a better way to handle this.
                            // d.setContent(service.getDocument(p.getProjectID(), d.getId()));

                            context.newContent(d.getUrl(), service.getDocument(p.getProjectID(), d.getId())).emit();
                            i++;
                        }
                        pageNumber++;
                    }
                    log.info("Processed page:{} of {} with {} new documents", pageNumber, totalPages, i);
                }

            }
        } catch (IOException e) {
            log.error("An error occurred", e);
        }
    }
}
