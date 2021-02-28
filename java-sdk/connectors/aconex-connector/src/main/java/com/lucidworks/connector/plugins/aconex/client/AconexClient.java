package com.lucidworks.connector.plugins.aconex.client;

import java.util.Map;

public interface AconexClient {
    Map<String, Map<String, Object>> getDocuments();
}
