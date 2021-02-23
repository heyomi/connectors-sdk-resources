package com.lucidworks.connector.plugins.feed.config;

import com.lucidworks.fusion.schema.Model;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.StringSchema;

public interface AuthenticationConfig extends Model {

    @Property(required = true, title = "Aconex Authentication Properties")
    Properties authentication();

    interface Properties extends Model {

        @Property(title = "Instance URL", required = true, description = "URL of your Aconex instance")
        @StringSchema(minLength = 1)
        String instanceUrl();

        @Property(title = "Username", required = true)
        @StringSchema(minLength = 1)
        String username();

        @Property(title = "Password", required = true)
        @StringSchema(encrypted = true)
        String password();
    }
}
