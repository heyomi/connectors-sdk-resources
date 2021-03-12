package com.lucidworks.connector.plugins.aconex.config;

import com.lucidworks.fusion.schema.Model;
import com.lucidworks.fusion.schema.SchemaAnnotations;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.UIHints;

import java.util.List;

import static com.lucidworks.connector.plugins.aconex.model.Constants.DEFAULT_RETURN_FIELDS;

public interface ProjectProperties extends Model {
    @Property(
            title = "Aconex Projects",
            description = "List of Aconex Projects to crawl.",
            order = 1,
            hints = { UIHints.ADVANCED }
    )
    @SchemaAnnotations.ArraySchema(defaultValue = "[]")
    @SchemaAnnotations.StringSchema(minLength = 1)
    List<String> projects();

    @Property(
            title = "Document Return Fields",
            description = "The search API service allows you to request specific data for each search result if required." +
                    "Default: " + DEFAULT_RETURN_FIELDS,
            order = 2,
            hints = { UIHints.ADVANCED }
    )
    @SchemaAnnotations.StringSchema(minLength = 1, defaultValue = DEFAULT_RETURN_FIELDS)
    String documentReturnFields();
}
