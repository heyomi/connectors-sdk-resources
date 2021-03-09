package com.lucidworks.connector.plugins.aconex.config;

import com.lucidworks.fusion.connector.plugin.api.config.ConnectorPluginProperties;
import com.lucidworks.fusion.schema.Model;
import com.lucidworks.fusion.schema.SchemaAnnotations.ArraySchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.StringSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.NumberSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;

import java.util.Set;

import static com.lucidworks.connector.plugins.aconex.model.Constants.DEFAULT_PAGE_SIZE;

/**
 * An extension interface for any {@link ConnectorPluginProperties} implementation.
 * Any Connector which requires item filtering based on size restrictions can use this extension.
 * Example: {@code interface MyFetcherProps extends ConnectorPluginProperties, SizeLimitConfig {}}
 */
public interface ItemLimitProperties extends Model {

    @Property(
            title = "Maximum Items",
            description = "Maximum number of documents to fetch. The default (-1) means no limit.",
            order = 1)
    @NumberSchema(defaultValue = -1)
    Integer maxItems();

    @Property(
            title = "Maximum File Size",
            description = "Used for excluding items when the item size is larger than the configured value. The default (-1) means no limit.",
            order = 3)
    @NumberSchema(defaultValue = -1)
    Integer maxSizeBytes();

    @Property(
            title = "Minimum File Size",
            description = "Used for excluding items when the item size is smaller than the configured value. The default (-1) means no limit.",
            order = 2)
    @NumberSchema(defaultValue = 1)
    Integer minSizeBytes();

    @Property(
            title = "Request Page Size",
            description = "Total number of docs to generate from the second and subsequent crawls.",
            order = 4)
    @NumberSchema(defaultValue = DEFAULT_PAGE_SIZE)
    int pageSize();

    @Property(
            title = "Included file extensions",
            description = "Set of file extensions to be fetched. If specified, all non-matching files will be skipped.")
    @ArraySchema(defaultValue = "[]")
    @StringSchema(minLength = 5)
    Set<String> includedFileExtensions();

    @Property(
            title = "Excluded file extensions",
            description = "A set of all file extensions to be skipped from the fetch.")
    @ArraySchema(defaultValue = "[jpg, jpeg, png, gif, zip]")
    @StringSchema(minLength = 6)
    Set<String> excludedFileExtensions();
}