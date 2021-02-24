package com.lucidworks.connector.plugins.aconex.config;

import com.lucidworks.fusion.schema.Model;
import com.lucidworks.fusion.schema.SchemaAnnotations.NumberSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.ObjectSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;

public interface ConnectionTimeoutProperties extends Model {

    @Property
    Properties timeoutsProperties();

    @ObjectSchema(title = "Http client options", description = "A set of options for configuring the http client.")
    interface Properties extends Model {

        @Property(order = 1)
        @NumberSchema(defaultValue = 5 * 60 * 1000, minimum = 0, maximum = 10 * 60 * 1000)
        int readTimeoutMs();

        @Property(order = 2)
        @NumberSchema(defaultValue = 60 * 1000, minimum = 0, maximum = 5 * 60 * 1000)
        int connectTimeoutMs();
    }
}

