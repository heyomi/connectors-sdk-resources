package com.lucidworks.connector.plugins.aconex.config;

import com.lucidworks.fusion.connector.plugin.api.config.ConnectorConfig;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorPluginProperties;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.RootSchema;

@RootSchema(
        title = "Aconex Connector - DEV",
        description = "A simple Aconex connector",
        category = "Aconex"
)
public interface AconexConfig extends ConnectorConfig<AconexConfig.Properties> {

    @Property(
            title = "Properties",
            required = true,
            order = 1
    )
    Properties properties();

    /**
     * Connector specific settings
     */
    interface Properties extends ConnectorPluginProperties, AconexProperties {
    }
}
