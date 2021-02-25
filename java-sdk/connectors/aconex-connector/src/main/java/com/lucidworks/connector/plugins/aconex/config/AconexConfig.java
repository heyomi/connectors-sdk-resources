package com.lucidworks.connector.plugins.aconex.config;

import com.lucidworks.fusion.connector.plugin.api.config.ConnectorConfig;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorPluginProperties;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.RootSchema;

@RootSchema(
        title = "Aconex Connector",
        description = "A simple Aconex connector",
        category = "Aconex"
)
public interface AconexConfig extends ConnectorConfig<AconexConfig.Properties> {

    @Property(
            title = "Aconex properties",
            required = true
    )
    AconexConfig.Properties properties();

    interface Properties extends ConnectorPluginProperties,
            TimeoutProperties,
            AuthenticationProperties,
            AdditionalProperties {
    }
}
