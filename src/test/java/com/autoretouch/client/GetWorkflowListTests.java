package com.autoretouch.client;

import com.autoretouch.client.auth.AuthResponse;
import com.autoretouch.client.auth.DeviceAuthorization;
import com.autoretouch.client.auth.GetDeviceCodeResponse;
import com.autoretouch.client.model.Page;
import com.autoretouch.client.model.Workflow;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
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
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class GetWorkflowListTests {
    private static final String CLIENT_ID = "DtLZblh4cfQdNc1iNXNV2JXy4zFL6qCM";
    private static final String AUDIENCE = "https://api.dev.autoretouch.com/";
    private static final String API_SERVER = "https://api.dev.autoretouch.com/";

    @Test
    void shouldGetListOfAllWorkflows() {
        Client underTest = new Client()
                .id(CLIENT_ID)
                .audience(AUDIENCE)
                .api(API_SERVER)
                .logIn();

        await().atMost(underTest.deviceCodeExpiresIn, TimeUnit.SECONDS)
                .pollInterval(underTest.refreshInterval, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThat(underTest.requestAuthToken()).isTrue();
                });

        List<Workflow> workflows = underTest.getWorkflows();
        workflows.forEach(workflow -> System.out.println(workflow.getName()));
        assertThat(workflows).isNotEmpty();
    }

    @Test
    void shouldLoadCredentialsFromDisk() {
        Client blankClient = new Client()
                .id(CLIENT_ID)
                .audience(AUDIENCE)
                .api(API_SERVER)
                .logIn();

        await().atMost(blankClient.deviceCodeExpiresIn, TimeUnit.SECONDS)
                .pollInterval(blankClient.refreshInterval, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThat(blankClient.requestAuthToken()).isTrue();
                });
        DeviceAuthorization authorization = blankClient.getDeviceAuthorization();

        assertThat(authorization.getAccessToken()).isNotNull();
        assertThat(authorization.getIdToken()).isNotNull();
        assertThat(authorization.getRefreshToken()).isNotNull();
        assertThat(authorization.getType()).isEqualTo("Bearer");
        assertThat(authorization.getClientId()).isEqualTo(CLIENT_ID);

        Client clientWithGivenAuthInformation = new Client()
                .audience(AUDIENCE)
                .api(API_SERVER)
                .withDeviceAuth(authorization);

        assertThat(clientWithGivenAuthInformation.getWorkflows()).isNotEmpty();
    }



    private class Client {
        private final RestTemplate restTemplate = new RestTemplate();
        private String apiServer = "https://api.autoretouch.com/";
        private String clientId = "V8EkfbxtBi93cAySTVWAecEum4d6pt4J";
        private String audience = "https://api.autoretouch.com/";
        private String authType = "Bearer";
        private String scope = "offline_access openid";
        private String accessToken = null;
        private String refreshToken = null;
        private String idToken = null;
        private String deviceCode = null;
        public int deviceCodeExpiresIn = -1;
        public int refreshInterval = -1;

        public Client() {
        }

        public Client id(String id) {
            clientId = id;
            return this;
        }

        public Client audience(String audience) {
            this.audience = audience;
            return this;
        }

        public Client scope(String scope) {
            this.scope = scope;
            return this;
        }

        public Client requestDeviceAuth() {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            map.add("client_id", clientId);
            map.add("scope", scope);
            map.add("audience", audience);
            GetDeviceCodeResponse response = Objects.requireNonNull(restTemplate.exchange("https://dev-autoretouch.eu.auth0.com/oauth/device/code", HttpMethod.POST, request, GetDeviceCodeResponse.class).getBody());
            String verificationUri = response.getVerificationUriComplete();
            deviceCodeExpiresIn = response.getExpiresIn();
            refreshInterval = response.getInterval();
            deviceCode = response.getDeviceCode();
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
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

                    AuthResponse response = Objects.requireNonNull(restTemplate.exchange("https://dev-autoretouch.eu.auth0.com/oauth/token", HttpMethod.POST, request, AuthResponse.class).getBody());
                    this.accessToken = response.getAccessToken();
                    this.refreshToken = response.getRefreshToken();
                    this.idToken = response.getIdToken();
                } catch (HttpClientErrorException ignored) {
                }
            }

            return accessToken != null;
        }

        public Client logIn() {
            if (this.accessToken == null) {
                return requestDeviceAuth();
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

        public DeviceAuthorization getDeviceAuthorization() {
            DeviceAuthorization result = new DeviceAuthorization();
            result.setAccessToken(accessToken);
            result.setIdToken(idToken);
            result.setRefreshToken(refreshToken);
            result.setType(authType);
            result.setClientId(clientId);
            return result;
        }

        public Client withDeviceAuth(DeviceAuthorization authorization) {
            this.accessToken = authorization.getAccessToken();
            this.refreshToken = authorization.getRefreshToken();
            this.idToken = authorization.getIdToken();
            this.clientId = authorization.getClientId();
            this.authType = authorization.getType();
            return this;
        }

        public Client api(String apiServer) {
            this.apiServer = apiServer;
            return this;
        }
    }
}
