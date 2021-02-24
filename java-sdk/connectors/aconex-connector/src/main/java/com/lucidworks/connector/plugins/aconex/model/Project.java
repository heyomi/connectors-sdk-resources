package com.lucidworks.connector.plugins.aconex.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Project {
    private final String id;
    private final String name;

    @JsonCreator
    public Project(@JsonProperty("projectID") String id, @JsonProperty("projectName") String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
