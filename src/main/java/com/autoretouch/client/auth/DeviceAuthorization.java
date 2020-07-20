package com.autoretouch.client.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeviceAuthorization {
    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("type")
    private String type;

    @JsonProperty("client_id")
    private String clientId;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
