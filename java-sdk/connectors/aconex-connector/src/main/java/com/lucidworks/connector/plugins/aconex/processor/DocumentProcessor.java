package com.lucidworks.connector.plugins.aconex.processor;

import com.lucidworks.connector.plugins.aconex.client.AconexClient;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.model.Document;
import com.lucidworks.connector.plugins.aconex.model.Project;
import com.lucidworks.connector.plugins.aconex.model.RegisterSearch;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.lucidworks.connector.plugins.aconex.model.Constants.*;

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

    @SuppressWarnings("unchecked")
    public void process() {
        long emitCounter = 0;
        int maxItems = config.properties().limit().maxItems();

        try {
            // get projects
            final List<Project> projects = service.getProjects();

            for (Project project : projects) {
                log.info("Starting on project:{}", project.getProjectID());

                // get documents on page: 1
                Map<String, Object> documentRegister = service.getDocumentRegister(project.getProjectID(), 1);
                List<Document> documents = (List<Document>) documentRegister.get(DOCUMENTS);
                RegisterSearch register = (RegisterSearch) documentRegister.get(REGISTER);
                int totalPages = register.getTotalPages();
                int pageNumber = 1;

                log.info("Project: {}, {}", project.getProjectID(), register.toString());

                // Loop through all pages. The API uses PAGED search type, meaning return results by "pages" of variable size.
                maxItems:
                while (pageNumber <= totalPages) {
                    if (pageNumber > 1) {
                        // Get documents on subsequent pages
                        documents = service.getDocuments(project.getProjectID(), pageNumber);
                    }

                    // add document content
                    for (Document document : documents) {
                        if (maxItems > -1 && emitCounter >= maxItems) break maxItems;
                        createDocument(project, document);
                        emitCounter++;
                    }
                    log.info("Processed page:{} of {} with {} new documents", pageNumber, totalPages, emitCounter);
                    pageNumber++;
                }
            }
        } catch(Exception e) {
            log.error("An error occurred", e);
            context.newError(context.getFetchInput().getId(), e.getMessage())
                    .emit();
        }
    }

    private void createDocument(Project project, Document document) throws IOException {
        context.newContent(document.getUrl(), service.getDocument(project.getProjectID(), document.getId()))
                .fields(f -> {
                    f.merge(document.toMetadata());
                    f.setString("project_id_t", project.getProjectID());
                    f.setString("project_name_t", project.getProjectName());
                    f.setLong(LAST_JOB_RUN_DATE_TIME, Instant.now().toEpochMilli());
                })
                .metadata(m -> m.setLong(LAST_JOB_RUN_DATE_TIME, Instant.now().toEpochMilli()))
                .emit();
    }
}
