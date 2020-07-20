package com.autoretouch.client;

import com.autoretouch.client.auth.AuthResponse;
import com.autoretouch.client.auth.DeviceAuthorization;
import com.autoretouch.client.auth.GetDeviceCodeResponse;
import com.autoretouch.client.model.Page;
import com.autoretouch.client.model.Workflow;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

class AutoRetouchClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private String apiServer = "https://api.autoretouch.com/";
    private String authServer = "https://auth.autoretouch.com/";
    private String clientId = "V8EkfbxtBi93cAySTVWAecEum4d6pt4J";
    private String audience = "https://api.autoretouch.com";
    private String authType = "Bearer";
    private String scope = "offline_access openid";
    private String accessToken = null;
    private String refreshToken = null;
    private String idToken = null;
    private String deviceCode = null;
    private boolean commandLineOnly = false;
    public int deviceCodeExpiresIn = -1;
    public int refreshInterval = -1;

    public AutoRetouchClient() {
    }

    public AutoRetouchClient requestDeviceAuth() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        map.add("client_id", clientId);
        map.add("scope", scope);
        map.add("audience", audience);
        GetDeviceCodeResponse response = Objects.requireNonNull(restTemplate.exchange(authServer + "oauth/device/code", HttpMethod.POST, request, GetDeviceCodeResponse.class).getBody());
        String verificationUri = response.getVerificationUriComplete();
        deviceCodeExpiresIn = response.getExpiresIn();
        refreshInterval = response.getInterval();
        deviceCode = response.getDeviceCode();
        if (!commandLineOnly && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(verificationUri));
            } catch (IOException | URISyntaxException ignored) {
            } finally {
                System.out.println(verificationUri);
            }
        } else {
            System.out.println(verificationUri);
        }
        return this;
    }


    public boolean requestAuthToken() {
        if (this.accessToken == null && this.deviceCode != null) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
                map.add("grant_type", "urn:ietf:params:oauth:grant-type:device_code");
                map.add("device_code", deviceCode);
                map.add("client_id", clientId);

                AuthResponse response = Objects.requireNonNull(restTemplate.exchange(authServer + "oauth/token", HttpMethod.POST, request, AuthResponse.class).getBody());
                this.accessToken = response.getAccessToken();
                this.refreshToken = response.getRefreshToken();
                this.idToken = response.getIdToken();
            } catch (HttpClientErrorException ignored) {
            }
        }

        return accessToken != null;
    }

    public AutoRetouchClient logIn() {
        if (this.accessToken == null && refreshToken == null) {
            return requestDeviceAuth();
        } else if (this.accessToken == null) {
            return refreshAccessToken();
        }
        return this;
    }

    public List<Workflow> getWorkflows() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        Page<Workflow> workflows = Objects.requireNonNull(restTemplate.exchange(apiServer + "workflow/", HttpMethod.GET,request, new ParameterizedTypeReference<Page<Workflow>>() {}).getBody());
        return workflows.getEntries();
    }

    public AutoRetouchClient refreshAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        map.add("grant_type", "refresh_token");
        map.add("client_id", clientId);
        map.add("refresh_token", refreshToken);

        AuthResponse response = Objects.requireNonNull(restTemplate.exchange(authServer + "oauth/token", HttpMethod.POST, request, AuthResponse.class).getBody());
        this.accessToken = response.getAccessToken();
        this.refreshToken = response.getRefreshToken();
        this.idToken = response.getIdToken();
        return this;
    }

    public DeviceAuthorization getDeviceAuthorization() {
        DeviceAuthorization result = new DeviceAuthorization();
        result.setRefreshToken(refreshToken);
        result.setType(authType);
        result.setClientId(clientId);
        return result;
    }

    public AutoRetouchClient withDeviceAuth(DeviceAuthorization authorization) {
        this.refreshToken = authorization.getRefreshToken();
        this.clientId = authorization.getClientId();
        this.authType = authorization.getType();
        return refreshAccessToken();
    }

    protected AutoRetouchClient api(String apiServer) {
        this.apiServer = apiServer;
        return this;
    }

    protected AutoRetouchClient authServer(String authServer) {
        this.authServer = authServer;
        return this;
    }

    protected AutoRetouchClient id(String id) {
        clientId = id;
        return this;
    }

    protected AutoRetouchClient audience(String audience) {
        this.audience = audience;
        return this;
    }

    protected AutoRetouchClient scope(String scope) {
        this.scope = scope;
        return this;
    }

    protected AutoRetouchClient useCommandLineOnly() {
        commandLineOnly = true;
        return this;
    }
}
