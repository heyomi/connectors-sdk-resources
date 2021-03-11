package com.lucidworks.connector.plugins.aconex.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public class ProjectList {
    private List<Project> searchResults;

    @JsonCreator
    public ProjectList(@JsonProperty("searchResults") List<Project> searchResults) {
        this.searchResults = searchResults;
    }

    public List<Project> getSearchResults() {
        return searchResults;
    }
}
