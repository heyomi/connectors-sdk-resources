package com.lucidworks.connector.plugins.aconex.client;

import com.lucidworks.connector.plugins.aconex.config.AdditionalProperties;
import com.lucidworks.connector.plugins.aconex.config.AuthenticationProperties;
import com.lucidworks.connector.plugins.aconex.config.TimeoutProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class AconexClientTest {
    AconexClient client;

    @Mock
    AuthenticationProperties authenticationProperties;

    @Mock
    TimeoutProperties timeoutProperties;

    @Mock
    AdditionalProperties additionalProperties;

    @Mock
    AuthenticationProperties.Properties authProps;

    @Mock
    TimeoutProperties.Properties timeoutProps;

    @Mock
    AdditionalProperties.Properties addProps;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(authProps.instanceUrl()).thenReturn("https://apidev.aconex.com");
        when(timeoutProps.connectTimeoutMs()).thenReturn(30000);
        when(authenticationProperties.auth()).thenReturn(authProps);
        when(timeoutProperties.timeout()).thenReturn(timeoutProps);
        when(additionalProperties.properties()).thenReturn(addProps);
    }

    @Test
    void getContent() {
        when(authProps.username()).thenReturn("poleary");
        when(authProps.password()).thenReturn("Auth3nt1c");

        client = new AconexClient(authenticationProperties, timeoutProperties, additionalProperties);
        Object content = client.getContent();

        assertNotNull(content);
    }
}
