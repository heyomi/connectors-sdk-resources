package com.lucidworks.connector.plugins.aconex.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class ProjectList {
    private List<Project> searchResults;

    public List<Project> getSearchResults() {
        return searchResults;
    }
}
