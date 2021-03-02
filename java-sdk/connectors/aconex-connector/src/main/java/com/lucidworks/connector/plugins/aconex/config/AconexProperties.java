package com.lucidworks.connector.plugins.aconex.config;

import com.lucidworks.fusion.schema.Model;
import com.lucidworks.fusion.schema.SchemaAnnotations;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.StringSchema;

import java.util.List;

import static com.lucidworks.connector.plugins.aconex.model.Constants.DEFAULT_PAGE_SIZE;

public interface AconexProperties extends Model {
    @Property(
            title = "Authentication Properties",
            description = "Aconex Authentication Properties",
            required = true,
            order = 1
    )
    AuthenticationProperties auth();

    @Property(
            title = "Timeout Properties",
            description = "Timeout Properties",
            required = true,
            order = 2
    )
    TimeoutProperties timeout();

    @Property(title = "Incremental Total", description = "Total number of docs to generate from the second and subsequent crawls.")
    @SchemaAnnotations.NumberSchema(defaultValue = DEFAULT_PAGE_SIZE)
    Integer documentsPerPage();

    @Property(title = "Inclusive File Types", description = "Document types that should be crawled.")
    @StringSchema(minLength = 1)
    List<String> fileTypes();

    @Property(
            title = "Aconex Projects",
            description = "List of Aconex Project IDs to crawl.",
            order = 2
    )
    @SchemaAnnotations.ArraySchema(minItems = 1)
    List<String> projects();

    interface AuthenticationProperties extends Model {
        @Property(title = "Instance URL", required = true, description = "URL of your Aconex instance")
        @StringSchema(minLength = 1)
        String instanceUrl();

        @Property(title = "Username", required = true)
        @StringSchema(minLength = 1)
        String username();

        @Property(title = "Password", required = true)
        @StringSchema(encrypted = true)
        String password();
    }

    interface TimeoutProperties extends Model {
        @Property(order = 1)
        @SchemaAnnotations.NumberSchema(defaultValue = 5 * 60 * 1000, minimum = 0, maximum = 10 * 60 * 1000)
        int readTimeoutMs();

        @Property(order = 2)
        @SchemaAnnotations.NumberSchema(defaultValue = 60 * 1000, minimum = 0, maximum = 5 * 60 * 1000)
        int connectTimeoutMs();
    }
}
