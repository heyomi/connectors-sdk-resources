package com.lucidworks.connector.plugins.aconex.client;

import com.lucidworks.connector.plugins.aconex.model.SearchResultsStats;

import java.util.Map;

public interface AconexClient {
    Map<String, Map<String, Object>> getDocuments();
    Map<String, Map<String, Object>> getDocuments(int pageNumber);
    SearchResultsStats getSearchResultsStats();
}
