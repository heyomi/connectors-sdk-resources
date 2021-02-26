package com.lucidworks.connector.plugins.aconex.config;

import com.lucidworks.fusion.schema.Model;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.StringSchema;

public interface AdditionalProperties extends Model {

    @Property(required = true, title = "Index Properties")
    Properties additional();

    interface Properties extends Model {
        @Property(title = "File Type")
        @StringSchema(minLength = 1)
        String fileType();
    }
}

