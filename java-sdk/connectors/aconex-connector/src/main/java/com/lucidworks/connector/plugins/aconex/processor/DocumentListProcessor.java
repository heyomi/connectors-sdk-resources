package com.lucidworks.connector.plugins.aconex.processor;

import com.lucidworks.connector.plugins.aconex.client.AconexClient;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.model.Document;
import com.lucidworks.connector.plugins.aconex.model.Project;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.PreFetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public class DocumentListProcessor {

    private final AconexConfig config;
    private final AconexClient service;

    public DocumentListProcessor(AconexConfig config, AconexClient service) {
        this.config = config;
        this.service = service;
    }

    public PreFetchResult process(ContentFetcher.PreFetchContext context) {
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

                log.info("Emitting candidate -> {}:{}", p.getProjectID(), totalPages);

                while(pageNumber <= totalPages) {
                    if (i >= maxItems) {
                        log.info("Max item limit reached");
                        break;
                    } else {
                        // get documents
                        List<Document> documents = service.getDocuments(p.getProjectID(), pageNumber);
                        documents.forEach(d -> {
                            log.info("Creating candidate {}", d.getId());

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
                                    })
                                    .emit();
                        });
                        pageNumber++;
                        i = i + documents.size();
                    }
                }

            }
        } catch (IOException e) {
            log.error("An error occurred", e);
        }

        return context.newResult();
    }
}
