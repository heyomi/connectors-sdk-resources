package com.lucidworks.connector.plugins.aconex.client.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AconexHttpClientOptions {
    private String hostname;
    private String apiKey;
    private String username;
    private String password;
    private int connectionTimeout;
}
