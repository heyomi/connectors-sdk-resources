package com.lucidworks.connector.plugins.aconex.config;

import com.lucidworks.fusion.schema.Model;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.StringSchema;
import com.lucidworks.fusion.schema.UIHints;

public interface AconexProperties extends Model {

    @Property(title = "Host", required = true, description = "The Aconex hostname. E.g. https://host.aconex.com", order = 1)
    @StringSchema(minLength = 1)
    String host();

    @Property(title = "API Key", required = true, description = "The Aconex API Application Key.", order = 2)
    @StringSchema(minLength = 1)
    String apiKey();

    @Property(
            title = "Authentication settings",
            description = "Select only one option",
            order = 3,
            required = true
    )
    AuthenticationConfig auth();

    @Property(
            title = "Aconex Projects Options",
            description = "Options for configuring project retrieval.",
            order = 4,
            hints = { UIHints.ADVANCED }
    )
    ProjectProperties project();

    @Property(
            title = "HTTP Timeout Options",
            description = "A set of options for configuring the HTTP client timeout in milliseconds.",
            order = 5,
            hints = { UIHints.ADVANCED }
    )
    TimeoutProperties timeout();

    @Property(
            title = "Limits",
            description = "Options for including or excluding documents.",
            order = 6,
            hints = { UIHints.ADVANCED }
    )
    LimitProperties limit();

    @Property(
            title = "Performance",
            description = "These API request limits when calling the Aconex web services.",
            order = 7,
            hints = { UIHints.ADVANCED }
    )
    PerformanceProperties performance();
}
