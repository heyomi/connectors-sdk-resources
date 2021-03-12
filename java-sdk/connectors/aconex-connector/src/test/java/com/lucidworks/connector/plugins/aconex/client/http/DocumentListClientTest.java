package com.lucidworks.connector.plugins.aconex.client.http;

import com.lucidworks.connector.plugins.aconex.config.*;
import com.lucidworks.connector.plugins.aconex.model.Constants;
import com.lucidworks.connector.plugins.aconex.model.Document;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class DocumentListClientTest {
    @InjectMocks
    DocumentListClient client;

    @Spy
    CloseableHttpClient httpClient = HttpClients.createDefault(); // Should be mocked

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
    void setup() {
        MockitoAnnotations.initMocks(this);

        when(authProps.username()).thenReturn("Omar McKenzie");
        when(authProps.password()).thenReturn("F$/K#;E@dB32*yt:");
        when(authConfig.basic()).thenReturn(authProps);
        when(timeoutProps.connection()).thenReturn(30000);
        when(limitProperties.maxItems()).thenReturn(25);
        when(limitProperties.pageSize()).thenReturn(25);
        when(limitProperties.write()).thenReturn(-1);
        when(limitProperties.includeMetadata()).thenReturn(true);
        when(properties.auth()).thenReturn(authConfig);
        when(properties.timeout()).thenReturn(timeoutProps);
        when(properties.limit()).thenReturn(limitProperties);
        when(projectProperties.documentReturnFields()).thenReturn(Constants.DEFAULT_RETURN_FIELDS);
        when(properties.project()).thenReturn(projectProperties);
        when(properties.host()).thenReturn("https://uk1.aconex.co.uk");
        when(properties.apiKey()).thenReturn("0e906a26-836c-4ca5-943b-9af74a4f0159");
        when(config.properties()).thenReturn(properties);
    }

    @Test
    void shouldReturnDocuments() throws IOException {
        List<Document> documents = client.getDocuments("268447644", 1);

        assertNotNull(documents);
        assertFalse(documents.isEmpty());
    }

    @Test
    void shouldReturnDocumentsWhenMaxFileSize() throws IOException {
        when(limitProperties.maxSizeBytes()).thenReturn(300000);

        List<Document> documents = client.getDocuments("268447644", 1);

        assertNotNull(documents);
        assertTrue(documents.size() < 25);
    }

    @Test
    void shouldReturnDocumentsWhenMinFileSize() throws IOException {
        when(limitProperties.maxSizeBytes()).thenReturn(800000);

        List<Document> documents = client.getDocuments("268447644", 1);

        assertNotNull(documents);
        assertTrue(documents.size() < 25);
    }

    @Test
    void shouldReturnEmptyResponseWhenProjectIsInvalid() throws IOException {
        List<Document> documents = client.getDocuments("fakeproject007", 1);

        assertNotNull(documents);
        assertTrue(documents.isEmpty());
    }
}