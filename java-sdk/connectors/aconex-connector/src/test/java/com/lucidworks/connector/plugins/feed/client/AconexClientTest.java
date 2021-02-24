package com.lucidworks.connector.plugins.feed.client;

import com.lucidworks.connector.plugins.aconex.client.AconexClient;
import com.lucidworks.connector.plugins.aconex.client.rest.RestApiUriBuilder;
import com.lucidworks.connector.plugins.aconex.config.AuthenticationProperties;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.mock;

class AconexClientTest {
    private static final Logger logger = LoggerFactory.getLogger(AconexClientTest.class);

    @Test
    void testOpen() {
        AconexClient client = new AconexClient(
                mock(AuthenticationProperties.class),
                mock(RestApiUriBuilder.class)
        );
    }
}