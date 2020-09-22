package com.autoretouch.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Organization {
    @JsonProperty("id") private String id;
    @JsonProperty("name") private String name;
    @JsonProperty("version") private String version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}

