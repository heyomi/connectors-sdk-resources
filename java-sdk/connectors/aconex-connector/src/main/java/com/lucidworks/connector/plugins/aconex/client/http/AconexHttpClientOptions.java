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
    private AuthType authType;
    private String username;
    private String password;
    private int connectionTimeout;

    public enum AuthType {
        BASIC,
        API
    }
}
