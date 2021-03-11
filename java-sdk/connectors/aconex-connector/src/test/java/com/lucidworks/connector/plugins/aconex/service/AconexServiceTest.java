package com.lucidworks.connector.plugins.aconex.service;

import com.lucidworks.connector.plugins.aconex.config.*;
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
    LimitProperties limitProperties;

    @Mock
    TimeoutProperties timeoutProps;

    @Mock
    ProjectProperties projectProperties;

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
        when(timeoutProps.connection()).thenReturn(30000);
        when(limitProperties.pageSize()).thenReturn(25);
        when(limitProperties.pageSize()).thenReturn(25);
        when(limitProperties.write()).thenReturn(-1);
        when(limitProperties.includeMetadata()).thenReturn(true);
        when(properties.auth().basic()).thenReturn(authProps);
        when(properties.timeout()).thenReturn(timeoutProps);
        when(config.properties()).thenReturn(properties);
        when(config.properties().host()).thenReturn("https://uk1.aconex.co.uk");
        when(config.properties().apiKey()).thenReturn("0e906a26-836c-4ca5-943b-9af74a4f0159");
        when(config.properties().limit()).thenReturn(limitProperties);
        when(config.properties().project()).thenReturn(projectProperties);
    }

    @Test
    void shouldReturnDocumentContent() {
        // when(properties.projects()).thenReturn(Arrays.asList("1879048199", "1879048279"));
        when(authProps.username()).thenReturn("Omar McKenzie");
        when(authProps.password()).thenReturn("F$/K#;E@dB32*yt:");

        service = new AconexService(config);
        Map<String, Map<String, Object>> content = service.getDocuments();

        assertNotNull(content);
        assertTrue(content.size() > 0);
    }

    @Test
    void shouldReturnEmptyResponseWhenProjectIsInvalid() {
        when(projectProperties.projects()).thenReturn(Arrays.asList("FAKE", "PROJECT"));
        when(authProps.username()).thenReturn("Omar McKenzie");
        when(authProps.password()).thenReturn("F$/K#;E@dB32*yt:");

        service = new AconexService(config);
        Map<String, Map<String, Object>> content = service.getDocuments();

        assertNotNull(content);
        assertTrue(content.isEmpty());
    }

    // @Test
    void getPagedContent() {
        when(authProps.username()).thenReturn("Omar McKenzie");
        when(authProps.password()).thenReturn("F$/K#;E@dB32*yt:");

        service = new AconexService(config);
        Map<String, Map<String, Object>> content = service.getPagedDoc();

        assertNotNull(content);
        assertEquals(content.size(), 25);
    }

    @Test
    void shouldReturnDocumentsWhenMaxFileSize() {
        when(authProps.username()).thenReturn("Omar McKenzie");
        when(authProps.password()).thenReturn("F$/K#;E@dB32*yt:");
        when(limitProperties.maxSizeBytes()).thenReturn(300000);

        service = new AconexService(config);
        Map<String, Map<String, Object>> content = service.getDocuments();

        assertNotNull(content);
        assertTrue(content.size() < 25);
    }

    @Test
    void shouldReturnDocumentsWhenMinFileSize() {
        when(authProps.username()).thenReturn("Omar McKenzie");
        when(authProps.password()).thenReturn("F$/K#;E@dB32*yt:");
        when(limitProperties.maxSizeBytes()).thenReturn(800000);

        service = new AconexService(config);
        Map<String, Map<String, Object>> content = service.getDocuments();

        assertNotNull(content);
        assertTrue(content.size() < 25);
    }

    @Test
    void shouldReturnDocumentsWithoutMetadata() {
        when(authProps.username()).thenReturn("Omar McKenzie");
        when(authProps.password()).thenReturn("F$/K#;E@dB32*yt:");
        when(limitProperties.includeMetadata()).thenReturn(false);

        service = new AconexService(config);
        Map<String, Map<String, Object>> content = service.getDocuments();

        assertNotNull(content);
        assertFalse(content.isEmpty());
        assertEquals(6, content.get("268447644:1348828088686947792").size());
    }

    @Test
    void shouldReturnEmptyResponseWhenCredentialsAreInvalid() {
        when(projectProperties.projects()).thenReturn(Collections.singletonList("1879048407"));
        when(authProps.username()).thenReturn("poleary");
        when(authProps.password()).thenReturn("Auth3nt1c");

        service = new AconexService(config);
        Map<String, Map<String, Object>> content = service.getDocuments();

        assertNotNull(content);
        assertEquals(content.size(), 0);
    }
}
