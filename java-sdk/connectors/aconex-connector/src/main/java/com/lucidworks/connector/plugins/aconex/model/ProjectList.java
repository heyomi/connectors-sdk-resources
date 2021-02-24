package com.lucidworks.connector.plugins.aconex.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ProjectList {
    private final List<Project> searchResults;

    @JsonCreator
    public ProjectList(@JsonProperty("searchResults") List<Project> searchResults) {
        this.searchResults = searchResults;
    }

    public List<Project> getSearchResults() {
        return searchResults;
    }
}
