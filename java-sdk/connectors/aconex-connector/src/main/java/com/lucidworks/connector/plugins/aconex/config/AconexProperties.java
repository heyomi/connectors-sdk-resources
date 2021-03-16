package com.lucidworks.connector.plugins.aconex.config;

import com.lucidworks.fusion.schema.Model;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.UIHints;

public interface AconexProperties extends Model {

    @Property(
            title = "Aconex Instance Details",
            // description = "Options for configuring project retrieval.",
            order = 1,
            required = true
    )
    ApiProperties api();

    @Property(
            title = "Authentication settings",
            order = 2,
            required = true
    )
    AuthenticationConfig auth();

    @Property(
            title = "Aconex Projects Options",
            description = "Options for configuring project retrieval.",
            order = 3,
            hints = { UIHints.ADVANCED }
    )
    ProjectProperties project();

    @Property(
            title = "HTTP Timeout Options",
            description = "A set of options for configuring the HTTP client timeout in milliseconds.",
            order = 4,
            hints = { UIHints.ADVANCED }
    )
    TimeoutProperties timeout();

    @Property(
            title = "Limits",
            description = "Options for including or excluding documents.",
            order = 5,
            hints = { UIHints.ADVANCED }
    )
    LimitProperties limit();

    @Property(
            title = "Performance",
            description = "These API request limits when calling the Aconex web services.",
            order = 6,
            hints = { UIHints.ADVANCED }
    )
    PerformanceProperties performance();
}
