package com.lucidworks.connector.plugins.feed.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class ProjectList {
    private final Map<String, Project> searchResults;

    @JsonCreator
    public ProjectList(@JsonProperty("entries") Map<String, Project> searchResults) {
        this.searchResults = searchResults;
    }

    public Map<String, Project> getSearchResults() {
        return searchResults;
    }
}
