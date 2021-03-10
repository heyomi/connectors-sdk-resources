package com.lucidworks.connector.plugins.aconex.config;

import com.lucidworks.fusion.schema.Model;
import com.lucidworks.fusion.schema.SchemaAnnotations.NumberSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;

public interface PerformanceProperties  extends Model {
    @Property(
            title = "Max Frequency Throttle",
            description = "Aconex Web Services has implemented a limit of maximum 5 requests per second." +
                    "This limitation is applied per organization the used account belongs to." +
                    "If this limit is passed, instead of getting the requested data, an error message is returned",
            order = 1)
    @NumberSchema(defaultValue = 1, minimum = 1, maximum = 5)
    int maxFrequency();

    @Property(
            title = "Max Concurrency Throttle",
            description = "Aconex Web Services has implemented a limit of maximum 10 concurrent active requests." +
                    "This limitation is applied per organization the used account belongs to." +
                    "If this limit is passed, instead of getting the requested data, an error message is returned",
            order = 2)
    @NumberSchema(defaultValue = 5, minimum = 1, maximum = 10)
    int maxConcurrency();
}