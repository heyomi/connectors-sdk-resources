package com.lucidworks.connector.plugins.aconex.config;

import com.lucidworks.fusion.schema.Model;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.StringSchema;
import com.lucidworks.fusion.schema.UIHints;

public interface AuthenticationConfig extends Model {
    @Property(
            // title = "Basic Authentication",
            // description = "Settings for the Basic Auth.",
            required = true,
            order = 2
    )
    BasicAuthenticationProperties basic();

    interface BasicAuthenticationProperties extends Model {
        @Property(title = "Username", required = true)
        @StringSchema(minLength = 1)
        String username();

        @Property(title = "Password", required = true, hints = {UIHints.SECRET})
        @StringSchema(encrypted = true)
        String password();
    }
}