package com.lucidworks.connector.plugins.aconex.config;

import com.lucidworks.fusion.connector.plugin.api.config.ConnectorPluginProperties;
import com.lucidworks.fusion.schema.Model;
import com.lucidworks.fusion.schema.SchemaAnnotations.ArraySchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.BooleanSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.StringSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.NumberSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.UIHints;

import java.util.Set;

import static com.lucidworks.connector.plugins.aconex.model.Constants.DEFAULT_PAGE_SIZE;

/**
 * An extension interface for any {@link ConnectorPluginProperties} implementation.
 * Any Connector which requires item filtering based on size restrictions can use this extension.
 * Example: {@code interface MyFetcherProps extends ConnectorPluginProperties, SizeLimitConfig {}}
 */
public interface LimitProperties extends Model {

    @Property(
            title = "Maximum Items",
            description = "Maximum number of documents to fetch. The default (-1) means no limit.",
            order = 1)
    @NumberSchema(defaultValue = -1, minimum = -1)
    Integer maxItems();

    @Property(
            title = "Minimum File Size (bytes)",
            description = "Used for excluding items when the item size is smaller than the configured value. The default (-1) means no limit.",
            order = 2)
    @NumberSchema(defaultValue = -1, minimum = -1)
    Integer minSizeBytes();

    @Property(
            title = "Maximum File Size (bytes)",
            description = "Used for excluding items when the item size is larger than the configured value. The default (-1) means no limit.",
            order = 3)
    @NumberSchema(defaultValue = -1, minimum = -1)
    Integer maxSizeBytes();

    @Property(
            title = "Request Page Size",
            description = "If specified, the value must be a number that is divisible by 25." +
                    "Without this parameter specified, this defaults to 25 Note: this parameter has a maximum value of 500",
            order = 4)
    @NumberSchema(defaultValue = DEFAULT_PAGE_SIZE, minimum = DEFAULT_PAGE_SIZE, maximum = 500)
    int pageSize();

    @Property(
            title = "Document Character Limit",
            description = "To receive the full text of the document use -1",
            order = 5,
            hints = { UIHints.ADVANCED }
    )
    @NumberSchema(defaultValue = -1, minimum = -1)
    int write();

    @Property(
            title = "Index Document Metadata",
            description = "Index the document's metadata for those files/attachments that meet the maximum/minimum size limits.",
            order = 6)
    @BooleanSchema(defaultValue = true)
    boolean includeMetadata();

    @Property(
            title = "Exclude Empty Documents",
            description = "Prevents 'CANNOT_DOWNLOAD_EMPTY_DOCUMENT' error. Cannot download a registered document which does not have a backing file",
            order = 6,
            hints = { UIHints.ADVANCED }
    )
    @BooleanSchema(defaultValue = true)
    boolean excludeEmptyDocuments();

    @Property(
            title = "Included file extensions",
            description = "Set of file extensions to be fetched. If specified, all non-matching files will be skipped.",
            order = 7)
    @ArraySchema(defaultValue = "[]")
    @StringSchema(minLength = 1)
    Set<String> includedFileExtensions();

    @Property(
            title = "Excluded file extensions",
            description = "A set of all file extensions to be skipped from the fetch.",
            order = 8)
    @ArraySchema(defaultValue = "[]")
    @StringSchema(minLength = 1)
    Set<String> excludedFileExtensions();
}