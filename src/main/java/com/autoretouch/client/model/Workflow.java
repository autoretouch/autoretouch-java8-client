package com.autoretouch.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class Workflow {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @JsonProperty("id") private String id;
    @JsonProperty("version") private String version;
    @JsonProperty("name") private String name;
    @JsonProperty("date") private String creationDate;

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
}
