package com.lucidworks.connector.plugins.aconex.config;

import com.lucidworks.fusion.schema.Model;
import com.lucidworks.fusion.schema.SchemaAnnotations.StringSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;

public interface ApiProperties extends Model {
    @Property(title = "Host", required = true, description = "The Aconex hostname. E.g. https://host.aconex.com", order = 1)
    @StringSchema(minLength = 1)
    String host();

    @Property(title = "API Key", required = true, description = "The Aconex API Application Key.", order = 2)
    @StringSchema(minLength = 1)
    String apiKey();
}
