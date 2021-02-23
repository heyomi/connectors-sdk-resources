package com.lucidworks.connector.plugins.feed.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Project {
    private final String projectID;
    private final String projectName;

    @JsonCreator
    public Project(@JsonProperty("projectID") String projectID, @JsonProperty("projectName") String projectName) {
        this.projectID = projectID;
        this.projectName = projectName;
    }

    public String getProjectID() {
        return projectID;
    }

    public String getProjectName() {
        return projectName;
    }
}
