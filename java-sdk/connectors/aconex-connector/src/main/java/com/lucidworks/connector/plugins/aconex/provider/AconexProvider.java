package com.lucidworks.connector.plugins.aconex.provider;

import com.lucidworks.connector.plugins.aconex.client.AconexClient;
import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.service.AconexService;

import javax.inject.Inject;
import javax.inject.Provider;

public class AconexProvider implements Provider<AconexClient> {
    private final AconexConfig config;

    @Inject
    AconexProvider(AconexConfig config) {
        this.config = config;
    }

    @Override
    public AconexClient get() {
        return new AconexService(config);
    }
}
