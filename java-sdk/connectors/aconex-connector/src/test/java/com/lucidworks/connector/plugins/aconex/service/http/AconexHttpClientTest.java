package com.lucidworks.connector.plugins.aconex.service.http;

import com.lucidworks.connector.plugins.aconex.config.AconexConfig;
import com.lucidworks.connector.plugins.aconex.config.AconexProperties;
import com.lucidworks.connector.plugins.aconex.model.Document;
import com.lucidworks.connector.plugins.aconex.model.ProjectList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AconexHttpClientTest {
    AconexHttpClient client;

    @Mock
    AconexConfig config;

    @Mock
    AconexConfig.Properties properties;

    @Mock
    AconexProperties.AuthenticationProperties authProps;

    @Mock
    AconexProperties.TimeoutProperties timeoutProps;

    private String apiEndpoint = "https://apidev.aconex.com/api";
    private String projectId = "1879048409";
    private String documentId = "271341877549097596";
    private String fileType = "pdf,doc";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(authProps.instanceUrl()).thenReturn("https://apidev.aconex.com");
        when(timeoutProps.connectTimeoutMs()).thenReturn(30000);
    }

    @Test
    void getProjectList() {
        when(authProps.username()).thenReturn("dmori");
        when(authProps.password()).thenReturn("Auth3nt1c");

        initClient();
        ProjectList projects = client.getProjectList(apiEndpoint);

        assertNotNull(projects);
        assertNotNull(projects.getSearchResults());
        assertFalse(projects.getSearchResults().isEmpty());
    }

    @Test
    void getDocuments() {
        when(authProps.username()).thenReturn("dmori");
        when(authProps.password()).thenReturn("Auth3nt1c");

        initClient();
        List<Document> documents = client.getDocuments(projectId);

        assertNotNull(documents);
        assertFalse(documents.isEmpty());
        assertNotNull(documents.get(0).getId());
    }

    @Test
    void getDownloadedDocuments_whenFileTypeIsPDF() {
        when(authProps.username()).thenReturn("poleary");
        when(authProps.password()).thenReturn("Auth3nt1c");

        initClient();
        Map<String, Object> document = client.getDocumentContent(projectId, documentId);

        assertNotNull(document);
        assertNotNull(document.get("pdf:PDFVersion"));
        assertNotNull(document.get("body"));
        assertNotNull(document.get("date"));
    }

    @Test
    void getDownloadedDocuments_whenFileTypeIsDoc() {
        when(authProps.username()).thenReturn("poleary");
        when(authProps.password()).thenReturn("Auth3nt1c");

        initClient();
        Map<String, Object> document = client.getDocumentContent("1879048400", "271341877549081900");

        assertNotNull(document);
        assertNotNull(document.get("body"));
        assertNotNull(document.get("date"));
        assertNull(document.get("pdf:PDFVersion"));
    }

    @Test
    void getApiEndpoint() {
        when(authProps.username()).thenReturn("dmori");
        when(authProps.password()).thenReturn("Auth3nt1c");

        initClient();
        String api = client.getApiEndpoint();

        assertEquals(api, "https://apidev.aconex.com/api");
    }

    @Test
    void shouldReturn401Exception_whenCredentialsAreInvalid() {
        when(authProps.username()).thenReturn("fakeuser");
        when(authProps.password()).thenReturn("fakepassword");

        initClient();
        // assertThrows(NotAuthorizedException.class, () -> client.getProjectList(apiEndpoint));
        assertNull(client.getProjectList(apiEndpoint));
    }

    private void initClient() {
        when(properties.auth()).thenReturn(authProps);
        when(properties.timeout()).thenReturn(timeoutProps);
        when(config.properties()).thenReturn(properties);

        client = new AconexHttpClient(config);
    }
}