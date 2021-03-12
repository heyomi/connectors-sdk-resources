package com.lucidworks.connector.plugins.aconex.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Project {
    private final String projectID;
    private final String projectName;
    private int totalPages;
    private int totalResults;

    @JsonCreator
    public Project(@JsonProperty("projectID") String projectID, @JsonProperty("projectName") String projectName) {
        this.projectID = projectID;
        this.projectName = projectName;
    }
}
