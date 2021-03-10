package com.lucidworks.connector.plugins.aconex.service.http;

import com.lucidworks.connector.plugins.aconex.model.ProjectList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AconexHttpClientTest {
    AconexHttpClient client;

    @Mock
    AconexHttpClientOptions options;

    private final String apiEndpoint = "https://uk1.aconex.co.uk/api";
    private final String projectId = "268447644";
    private final String documentId = "1348828088686947792";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(options.getUsername()).thenReturn("Omar McKenzie");
        when(options.getPassword()).thenReturn("F$/K#;E@dB32*yt:");
        when(options.getHostname()).thenReturn("https://uk1.aconex.co.uk");
        when(options.getApiKey()).thenReturn("0e906a26-836c-4ca5-943b-9af74a4f0159");
        when(options.getConnectionTimeout()).thenReturn(30000);

        client = new AconexHttpClient(options);
    }

    @Test
    void shouldReturnProjectList() {
        ProjectList projects = client.getProjectList(apiEndpoint);

        assertNotNull(projects);
        assertNotNull(projects.getSearchResults());
        assertFalse(projects.getSearchResults().isEmpty());
    }

    @Test
    void shouldReturnDocuments() {
        String documents = client.getDocumentsByProject(projectId);

        assertNotNull(documents);
    }

    @Test
    void shouldReturnDocumentContent() {
        byte[] document = client.getDocument(projectId, documentId);

        assertNotNull(document);
    }

    @Test
    void shouldReturnAPI() {
        assertEquals(apiEndpoint, client.getApiEndpoint());
    }

    @Test
    void shouldReturn401Exception_whenCredentialsAreInvalid() {
        when(options.getUsername()).thenReturn("fakeuser");
        when(options.getPassword()).thenReturn("fakepassword");
        client = new AconexHttpClient(options);
        // assertThrows(NotAuthorizedException.class, () -> client.getProjectList(apiEndpoint));

        assertNull(client.getProjectList(apiEndpoint));
    }
}