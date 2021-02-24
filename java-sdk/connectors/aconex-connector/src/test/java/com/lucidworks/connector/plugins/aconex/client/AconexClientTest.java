package com.lucidworks.connector.plugins.aconex.client;

import com.lucidworks.connector.plugins.aconex.config.AuthenticationProperties;
import com.lucidworks.connector.plugins.aconex.config.TimeoutProperties;
import com.lucidworks.connector.plugins.aconex.model.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.NotAuthorizedException;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class AconexClientTest {
    // @Mock
    AconexClient client;

    @Mock
    AuthenticationProperties authenticationProperties;

    @Mock
    TimeoutProperties timeoutProperties;

    @Mock
    AuthenticationProperties.Properties authProps;

    @Mock
    TimeoutProperties.Properties timeoutProps;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(authProps.instanceUrl()).thenReturn("https://apidev.aconex.com");
        when(timeoutProps.connectTimeoutMs()).thenReturn(10000);
        when(authenticationProperties.authentication()).thenReturn(authProps);
        when(timeoutProperties.properties()).thenReturn(timeoutProps);
    }

    @Test
    void shouldReturnProjects() {
        when(authProps.username()).thenReturn("");
        when(authProps.password()).thenReturn("");
        initClient();

        List<Project> projects = client.getProjects();

        assertNotNull(projects);
    }

    @Test
    void shouldReturn401Exception_whenCredentialsAreInvalid() {
        when(authProps.username()).thenReturn("fakeuser");
        when(authProps.password()).thenReturn("fakepassword");
        when(authenticationProperties.authentication()).thenReturn(authProps);
        initClient();

        assertThrows(NotAuthorizedException.class, () -> client.getProjects());
    }

    void getDocumentIds() {
    }

    private void initClient() {
        client = new AconexClient(authenticationProperties, timeoutProperties);
    }
}