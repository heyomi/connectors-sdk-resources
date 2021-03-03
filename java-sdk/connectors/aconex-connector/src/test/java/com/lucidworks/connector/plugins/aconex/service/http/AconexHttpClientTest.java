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

    private final String apiEndpoint = "https://apidev.aconex.com/api";
    private final String projectId = "1879048409";
    private final String documentId = "271341877549097596";
    private final String fileType = "pdf,doc";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(options.getUsername()).thenReturn("poleary");
        when(options.getPassword()).thenReturn("Auth3nt1c");
        when(options.getHostname()).thenReturn("https://apidev.aconex.com");
        when(options.getConnectionTimeout()).thenReturn(30000);

        client = new AconexHttpClient(options);
    }

    @Test
    void getProjectList() {
        ProjectList projects = client.getProjectList(apiEndpoint);

        assertNotNull(projects);
        assertNotNull(projects.getSearchResults());
        assertFalse(projects.getSearchResults().isEmpty());
    }

    @Test
    void getDocuments() {
        String documents = client.getDocumentsByProject(projectId);

        assertNotNull(documents);
    }

    @Test
    void getDocument() {
        byte[] document = client.getDocument(projectId, documentId);

        assertNotNull(document);
    }

    @Test
    void getApiEndpoint() {
        String api = client.getApiEndpoint();

        assertEquals(api, "https://apidev.aconex.com/api");
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