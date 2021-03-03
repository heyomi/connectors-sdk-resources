package com.lucidworks.connector.plugins.aconex.service;

import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.config.AconexProperties;
import com.lucidworks.connector.plugins.aconex.config.AuthenticationConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AconexServiceTest {
    AconexService service;

    @Mock
    AuthenticationConfig.BasicAuthenticationProperties authProps;

    @Mock
    AconexProperties.TimeoutProperties timeoutProps;

    @Mock
    AconexConfig.Properties properties;

    @Mock
    AuthenticationConfig authConfig;

    @Mock
    AconexConfig config;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(properties.auth()).thenReturn(authConfig);
        when(timeoutProps.connectTimeoutMs()).thenReturn(30000);
        when(properties.auth().basic()).thenReturn(authProps);
        when(properties.timeout()).thenReturn(timeoutProps);
        when(config.properties()).thenReturn(properties);
        when(config.properties().host()).thenReturn("https://apidev.aconex.com");
        service = new AconexService(config);
    }

    @Test
    void getContent() {
        when(properties.projects()).thenReturn(Arrays.asList("1879048199", "1879048279"));
        when(authProps.username()).thenReturn("poleary");
        when(authProps.password()).thenReturn("Auth3nt1c");

        service = new AconexService(config);
        Map<String, Map<String, Object>> content = service.getDocuments();

        assertNotNull(content);
        assertTrue(content.size() > 0);
    }

    void getPagedContent() {
        when(authProps.username()).thenReturn("poleary");
        when(authProps.password()).thenReturn("Auth3nt1c");

        service = new AconexService(config);
        Map<String, Map<String, Object>> content = service.getDocuments();

        assertNotNull(content);
        assertTrue(content.size() > 0);
    }

    @Test
    void getContent_error403() {
        when(properties.projects()).thenReturn(Collections.singletonList("1879048407"));
        when(authProps.username()).thenReturn("poleary");
        when(authProps.password()).thenReturn("Auth3nt1c");

        service = new AconexService(config);
        Map<String, Map<String, Object>> content = service.getDocuments();

        assertNotNull(content);
        assertEquals(content.size(), 0);
    }
}
