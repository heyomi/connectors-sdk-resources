package com.lucidworks.connector.plugins.aconex.config;

import com.lucidworks.fusion.schema.Model;
import com.lucidworks.fusion.schema.SchemaAnnotations.ArraySchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.StringSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.NumberSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.UIHints;

import java.util.List;

import static com.lucidworks.connector.plugins.aconex.model.Constants.DEFAULT_PAGE_SIZE;

public interface AconexProperties extends Model {

    @Property(title = "Host", required = true, description = "The Aconex hostname. E.g. https://host.aconex.com", order = 1)
    @StringSchema(minLength = 1)
    String host();

    @Property(
            title = "Authentication settings",
            description = "Select only one option",
            order = 2,
            required = true
    )
    AuthenticationConfig auth();

    @Property(
            title = "HTTP Timeout Options",
            description = "A set of options for configuring the HTTP client timeout in milliseconds.",
            order = 4,
            hints = { UIHints.ADVANCED }
    )
    TimeoutProperties timeout();

    @Property(title = "Default Request Page Size", description = "Total number of docs to generate from the second and subsequent crawls.", hints = { UIHints.ADVANCED })
    @NumberSchema(defaultValue = DEFAULT_PAGE_SIZE)
    int documentsPerPage();

    @Property(title = "Include Documents by File Types", description = "This will limit this datasource to only these file types.", hints = { UIHints.ADVANCED })
    @ArraySchema(minItems = 1)
    List<String> fileTypes();

    @Property(
            title = "Aconex Projects",
            description = "List of Aconex Project IDs to crawl.",
            order = 3
    )
    @ArraySchema(minItems = 1)
    List<String> projects();

    interface TimeoutProperties extends Model {
        @Property(title = "Read Timeout", order = 1)
        @NumberSchema(defaultValue = 5 * 60 * 1000, minimum = 0, maximum = 10 * 60 * 1000)
        int readTimeoutMs();

        @Property(title = "Connection Timeout", order = 2)
        @NumberSchema(defaultValue = 60 * 1000, minimum = 0, maximum = 5 * 60 * 1000)
        int connectTimeoutMs();
    }
}
