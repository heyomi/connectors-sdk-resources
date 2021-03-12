package com.lucidworks.connector.plugins.aconex.config;

import com.lucidworks.fusion.schema.Model;
import com.lucidworks.fusion.schema.SchemaAnnotations.NumberSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;

public interface TimeoutProperties extends Model {
    @Property(title = "Read Timeout (ms)", order = 1)
    @NumberSchema(defaultValue = 5000, minimum = 0, maximum = 300000)
    int read();

    @Property(title = "Connection Timeout (ms)", order = 2)
    @NumberSchema(defaultValue = 5000, minimum = 0, maximum = 300000)
    int connection();

    @Property(title = "Socket Timeout (ms)", order = 3)
    @NumberSchema(defaultValue = 5000, minimum = 0, maximum = 300000)
    int socket();
}