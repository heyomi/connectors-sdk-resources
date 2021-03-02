package com.lucidworks.connector.plugins.aconex.service;

import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.config.AconexProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class AconexServiceTest {
    AconexService client;

    @Mock
    AconexProperties.AuthenticationProperties authProps;

    @Mock
    AconexProperties.TimeoutProperties timeoutProps;

    @Mock
    AconexConfig.Properties properties;

    @Mock
    AconexConfig config;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(authProps.instanceUrl()).thenReturn("https://apidev.aconex.com");
        when(timeoutProps.connectTimeoutMs()).thenReturn(30000);
        when(properties.auth()).thenReturn(authProps);
        when(properties.timeout()).thenReturn(timeoutProps);
        when(config.properties()).thenReturn(properties);
    }

    @Test
    void getContent() {
        when(authProps.username()).thenReturn("dmori");
        when(authProps.password()).thenReturn("Auth3nt1c");

        client = new AconexService(config);
        Object content = client.getDocuments();

        assertNotNull(content);
    }
}
