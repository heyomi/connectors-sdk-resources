package com.lucidworks.connector.plugins.aconex.client.http;

import com.lucidworks.connector.plugins.aconex.config.*;
import com.lucidworks.connector.plugins.aconex.model.Constants;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class DocumentContentClientTest {
    @InjectMocks
    DocumentContentClient client;

    @Spy
    CloseableHttpClient httpClient = HttpClients.createDefault(); // Should be mocked

    @Mock
    AuthenticationConfig.BasicAuthenticationProperties authProps;

    @Mock
    ApiProperties apiProperties;

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
        when(apiProperties.host()).thenReturn("https://uk1.aconex.co.uk");
        when(apiProperties.apiKey()).thenReturn("0e906a26-836c-4ca5-943b-9af74a4f0159");
        when(authConfig.basic()).thenReturn(authProps);
        when(timeoutProps.connection()).thenReturn(30000);
        when(limitProperties.pageSize()).thenReturn(25);
        when(limitProperties.pageSize()).thenReturn(25);
        when(limitProperties.write()).thenReturn(-1);
        when(limitProperties.includeMetadata()).thenReturn(true);
        when(properties.auth()).thenReturn(authConfig);
        when(properties.timeout()).thenReturn(timeoutProps);
        when(properties.limit()).thenReturn(limitProperties);
        when(projectProperties.documentReturnFields()).thenReturn(Constants.DEFAULT_RETURN_FIELDS);
        when(properties.project()).thenReturn(projectProperties);
        when(properties.api()).thenReturn(apiProperties);
        when(config.properties()).thenReturn(properties);
    }

    @Test
    void shouldReturnDocumentContent() throws IOException {
        InputStream in = client.getDocumentContent("268447644", "1348828088686947792");

        assertNotNull(in);
    }

    @Test
    void shouldReturnDocumentContentWhenRequestInvalid() throws IOException {
        InputStream in = client.getDocumentContent("268447644", "0123456789");

        assertEquals(0, in.readAllBytes().length);
    }
}