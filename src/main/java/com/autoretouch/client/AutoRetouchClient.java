package com.autoretouch.client;

import com.autoretouch.client.auth.AuthResponse;
import com.autoretouch.client.auth.DeviceAuthorization;
import com.autoretouch.client.auth.GetDeviceCodeResponse;
import com.autoretouch.client.model.Page;
import com.autoretouch.client.model.Workflow;
import com.autoretouch.client.model.WorkflowExecution;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AutoRetouchClient {
    private final RestTemplate restTemplate = new RestTemplate();

    private String apiServer = "https://api.autoretouch.com";
    private String authServer = "https://auth.autoretouch.com/";
    private String clientId = "V8EkfbxtBi93cAySTVWAecEum4d6pt4J";
    private String audience = "https://api.autoretouch.com";
    private String authType = "Bearer";
    private String accessToken = null;
    private String refreshToken = null;
    private String deviceCode = null;
    public int deviceCodeExpiresIn = -1;
    public int refreshInterval = -1;

    public AutoRetouchClient() {
    }

    public AutoRetouchClient requestDeviceAuth() {
        HttpHeaders headers = createUnauthorizedFormHeaders();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        map.add("client_id", clientId);
        map.add("scope", "offline_access");
        map.add("audience", audience);
        GetDeviceCodeResponse response = Objects.requireNonNull(restTemplate.exchange(authServer + "oauth/device/code", HttpMethod.POST, request, GetDeviceCodeResponse.class).getBody());
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
                HttpHeaders headers = createUnauthorizedFormHeaders();
                MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
                map.add("grant_type", "urn:ietf:params:oauth:grant-type:device_code");
                map.add("device_code", deviceCode);
                map.add("client_id", clientId);

                AuthResponse response = Objects.requireNonNull(restTemplate.exchange(authServer + "oauth/token", HttpMethod.POST, request, AuthResponse.class).getBody());
                accessToken = response.getAccessToken();
                refreshToken = response.getRefreshToken();
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

    public AutoRetouchClient refreshAccessToken() {
        HttpHeaders headers = createUnauthorizedFormHeaders();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        map.add("grant_type", "refresh_token");
        map.add("client_id", clientId);
        map.add("refresh_token", refreshToken);

        AuthResponse response = Objects.requireNonNull(restTemplate.exchange(authServer + "oauth/token", HttpMethod.POST, request, AuthResponse.class).getBody());
        accessToken = response.getAccessToken();
        refreshToken = response.getRefreshToken();
        return this;
    }

    private HttpHeaders createUnauthorizedFormHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    public DeviceAuthorization getDeviceAuthorization() {
        DeviceAuthorization result = new DeviceAuthorization();
        result.setRefreshToken(refreshToken);
        result.setType(authType);
        result.setClientId(clientId);
        return result;
    }

    public AutoRetouchClient withDeviceAuth(DeviceAuthorization authorization) {
        refreshToken = authorization.getRefreshToken();
        clientId = authorization.getClientId();
        authType = authorization.getType();
        return refreshAccessToken();
    }

    public List<Workflow> getWorkflows() {
        HttpEntity<Void> request = new HttpEntity<>(createAuthorizedHeaders());
        Page<Workflow> workflows = Objects.requireNonNull(restTemplate.exchange(apiServer + "/workflow/", HttpMethod.GET, request, new ParameterizedTypeReference<Page<Workflow>>() {}).getBody());
        return workflows.getEntries();
    }

    public HttpStatus getApiStatus() {
        return restTemplate.getForEntity(apiServer + "/health/", String.class).getStatusCode();
    }

    protected AutoRetouchClient useDevelopmentEnvironment() {
        clientId = "DtLZblh4cfQdNc1iNXNV2JXy4zFL6qCM";
        audience = "https://api.dev.autoretouch.com/";
        apiServer = "https://api.dev.autoretouch.com";
        authServer = "https://dev-autoretouch.eu.auth0.com/";
        return this;
    }

    public String createWorkflowExecution(String workflowId, FileSystemResource file, Map<String, String> labels) {
        HttpHeaders headers = createAuthorizedHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file);
        HttpEntity<MultiValueMap<String, Object>> creationRequest = new HttpEntity<>(body, headers);
        String labelParams = createLabelParams(labels);
        return restTemplate.postForEntity(apiServer + "/workflow/execution/create?workflow=" + workflowId + "&" + labelParams, creationRequest, String.class).getBody();
    }

    private String createLabelParams(Map<String, String> labels) {
        StringBuilder paramsBuilder = new StringBuilder();
        labels.entrySet().stream()
            .map(label -> "&labels[" + label.getKey() + "]=" + label.getValue())
            .forEach(paramsBuilder::append);
        return paramsBuilder.toString();
    }

    public String createWorkflowExecution(String workflowId, FileSystemResource file) {
        return createWorkflowExecution(workflowId, file, new HashMap<>());
    }

    public WorkflowExecution getWorkflowExecution(String executionId) {
        HttpEntity<Void> statusRequest = new HttpEntity<>(createAuthorizedHeaders());
        return restTemplate.exchange(apiServer + "/workflow/execution/" + executionId, HttpMethod.GET, statusRequest, WorkflowExecution.class).getBody();
    }

    public String getWorkflowExecutionStatus(String executionId) {
        HttpEntity<Void> statusRequest = new HttpEntity<>(createAuthorizedHeaders());
        return restTemplate.exchange(apiServer + "/workflow/execution/" + executionId + "/status", HttpMethod.GET, statusRequest, String.class).getBody();
    }

    public HttpStatus downloadWorkflowExecutionResultImage(WorkflowExecution execution, OutputStream resultStream) {
        return restTemplate.execute(
                apiServer + execution.getResultPath(),
                HttpMethod.GET,
                clientHttpRequest -> clientHttpRequest.getHeaders().setBearerAuth(accessToken),
                clientHttpResponse -> {
                    StreamUtils.copy(clientHttpResponse.getBody(), resultStream);
                    return clientHttpResponse.getStatusCode();
                });
    }

    public HttpStatus retryWorkflowExecution(String executionId) {
        HttpEntity<Void> triggerRetryRequest = new HttpEntity<>(createAuthorizedHeaders());
        return restTemplate.exchange(apiServer + "/workflow/execution/" + executionId + "/retry", HttpMethod.POST, triggerRetryRequest, String.class).getStatusCode();
    }

    public BigInteger getBalance() {
        HttpEntity<Void> request = new HttpEntity<>(createAuthorizedHeaders());
        return restTemplate.exchange(apiServer + "/company/balance", HttpMethod.GET, request, BigInteger.class).getBody();
    }

    private HttpHeaders createAuthorizedHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }

    public List<WorkflowExecution> getLatestWorkflowExecutions(String workflowId) {
        HttpEntity<Void> request = new HttpEntity<>(createAuthorizedHeaders());
        return restTemplate.exchange(apiServer + "/workflow/execution?workflow=" + workflowId, HttpMethod.GET, request, new ParameterizedTypeReference<Page<WorkflowExecution>>(){})
                .getBody()
                .getEntries();
    }
}
