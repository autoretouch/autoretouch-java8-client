package com.autoretouch.client;

import com.autoretouch.client.auth.DeviceAuthorization;
import com.autoretouch.client.model.Workflow;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class GetWorkflowListTests {
    private static final String CLIENT_ID = "DtLZblh4cfQdNc1iNXNV2JXy4zFL6qCM";
    private static final String AUDIENCE = "https://api.dev.autoretouch.com/";
    private static final String API_SERVER = "https://api.dev.autoretouch.com/";
    private static final String AUTH_SERVER = "https://dev-autoretouch.eu.auth0.com/";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldGetListOfAllWorkflows() {
        AutoRetouchClient underTest = createDevClient().logIn();

        waitForUserToAuthViaBrowser(underTest);

        List<Workflow> workflows = underTest.getWorkflows();
        workflows.forEach(workflow -> System.out.println(workflow.getName()));
        assertThat(workflows).isNotEmpty();
    }

    @Test
    void shouldLoadCredentialsFromDisk() throws IOException {
        AutoRetouchClient blankClient = createDevClient().logIn();

        waitForUserToAuthViaBrowser(blankClient);

        DeviceAuthorization authorization = blankClient.getDeviceAuthorization();

        assertThat(authorization.getRefreshToken()).isNotNull();
        assertThat(authorization.getType()).isEqualTo("Bearer");
        assertThat(authorization.getClientId()).isEqualTo(CLIENT_ID);

        File deviceAuthFile = File.createTempFile("autoretouch", ".json");
        deviceAuthFile.deleteOnExit();
        ObjectWriter writer = objectMapper.writer(new DefaultPrettyPrinter());
        writer.writeValue(deviceAuthFile, authorization);

        DeviceAuthorization loadedDeviceAuth = objectMapper.readValue(deviceAuthFile, DeviceAuthorization.class);

        AutoRetouchClient clientWithGivenAuthInformation = createDevClient()
                .withDeviceAuth(loadedDeviceAuth);

        DeviceAuthorization refreshedAuthorization = clientWithGivenAuthInformation.getDeviceAuthorization();
        assertThat(refreshedAuthorization.getRefreshToken()).isNotNull();
        assertThat(refreshedAuthorization.getType()).isEqualTo("Bearer");
        assertThat(refreshedAuthorization.getClientId()).isEqualTo(CLIENT_ID);


        assertThat(clientWithGivenAuthInformation.getWorkflows()).isNotEmpty();
    }

    private void waitForUserToAuthViaBrowser(AutoRetouchClient underTest) {
        await().atMost(underTest.deviceCodeExpiresIn, TimeUnit.SECONDS)
                .pollInterval(underTest.refreshInterval, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(underTest.requestAuthToken()).isTrue());
    }

    private AutoRetouchClient createDevClient() {
        return new AutoRetouchClient().id(CLIENT_ID).audience(AUDIENCE).api(API_SERVER).authServer(AUTH_SERVER);
    }
}
