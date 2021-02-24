package com.lucidworks.connector.plugins.aconex.config;

import com.lucidworks.fusion.schema.Model;
import com.lucidworks.fusion.schema.SchemaAnnotations;

public interface IndexConfig extends Model {

    @SchemaAnnotations.Property
    ConnectionTimeoutProperties.Properties requestProperties();

    @SchemaAnnotations.ObjectSchema(
            title = "Http client options",
            description = "A set of options for configuring the http client."
    )
    interface Properties extends Model {

        @SchemaAnnotations.Property(title = "Project IDs")
        @SchemaAnnotations.StringSchema(minLength = 1)
        String projects();

        @SchemaAnnotations.Property(title = "Cache Projects")
        @SchemaAnnotations.BooleanSchema()
        boolean cacheProjects();
    }
}

