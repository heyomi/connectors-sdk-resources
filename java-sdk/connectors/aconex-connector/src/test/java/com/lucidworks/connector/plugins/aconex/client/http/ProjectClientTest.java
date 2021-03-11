package com.lucidworks.connector.plugins.aconex.client.http;

import com.lucidworks.connector.plugins.aconex.config.*;
import com.lucidworks.connector.plugins.aconex.model.Project;
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

class ProjectClientTest {
    @InjectMocks
    ProjectClient client;

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

        when(authConfig.basic()).thenReturn(authProps);
        when(timeoutProps.connection()).thenReturn(30000);
        when(limitProperties.pageSize()).thenReturn(25);
        when(limitProperties.pageSize()).thenReturn(25);
        when(limitProperties.write()).thenReturn(-1);
        when(limitProperties.includeMetadata()).thenReturn(true);
        when(properties.auth()).thenReturn(authConfig);
        when(properties.timeout()).thenReturn(timeoutProps);
        when(properties.limit()).thenReturn(limitProperties);
        when(properties.project()).thenReturn(projectProperties);
        when(properties.host()).thenReturn("https://uk1.aconex.co.uk");
        when(properties.apiKey()).thenReturn("0e906a26-836c-4ca5-943b-9af74a4f0159");
        when(config.properties()).thenReturn(properties);
    }

    @Test
    void shouldReturnProjects() throws IOException {
        when(authProps.username()).thenReturn("Omar McKenzie");
        when(authProps.password()).thenReturn("F$/K#;E@dB32*yt:");

        List<Project> projects = client.getProjects();

        assertNotNull(projects);
        assertFalse(projects.isEmpty());
    }

    @Test
    void shouldReturnNullWhenAuthIsInvalid() throws IOException {
        when(authProps.username()).thenReturn("FAKE");
        when(authProps.password()).thenReturn("PASSWORD");

        List<Project> projects = client.getProjects();

        assertNull(projects);
    }
}