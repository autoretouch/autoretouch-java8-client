package com.autoretouch.client;

import com.autoretouch.client.auth.DeviceAuthorization;
import com.autoretouch.client.model.Workflow;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class GetWorkflowListTests {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldGetListOfAllWorkflows() {
        AutoRetouchClient underTest = new AutoRetouchClient().developmentServer().logIn();

        waitForUserToAuthViaBrowser(underTest);

        List<Workflow> workflows = underTest.getWorkflows();
        workflows.forEach(System.out::println);
        assertThat(workflows).isNotEmpty();
    }

    @Test
    void shouldReturnStatusOfProductionApi() {
        AutoRetouchClient underTest = new AutoRetouchClient();
        assertThat(underTest.getApiStatus()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnStatusOfDevelopmentApi() {
        AutoRetouchClient underTest = new AutoRetouchClient().developmentServer();
        assertThat(underTest.getApiStatus()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldLoadCredentialsFromDisk() throws IOException {
        AutoRetouchClient blankClient = new AutoRetouchClient().developmentServer().logIn();

        waitForUserToAuthViaBrowser(blankClient);

        DeviceAuthorization authorization = blankClient.getDeviceAuthorization();

        assertThat(authorization.getRefreshToken()).isNotNull();
        assertThat(authorization.getType()).isEqualTo("Bearer");

        File deviceAuthFile = File.createTempFile("autoretouch", ".json");
        deviceAuthFile.deleteOnExit();
        ObjectWriter writer = objectMapper.writer(new DefaultPrettyPrinter());
        writer.writeValue(deviceAuthFile, authorization);

        DeviceAuthorization loadedDeviceAuth = objectMapper.readValue(deviceAuthFile, DeviceAuthorization.class);

        AutoRetouchClient clientWithGivenAuthInformation = new AutoRetouchClient().developmentServer()
                .withDeviceAuth(loadedDeviceAuth);

        DeviceAuthorization refreshedAuthorization = clientWithGivenAuthInformation.getDeviceAuthorization();
        assertThat(refreshedAuthorization.getRefreshToken()).isNotNull();
        assertThat(refreshedAuthorization.getType()).isEqualTo("Bearer");


        assertThat(clientWithGivenAuthInformation.getWorkflows()).isNotEmpty();
    }

    private void waitForUserToAuthViaBrowser(AutoRetouchClient underTest) {
        await().atMost(underTest.deviceCodeExpiresIn, TimeUnit.SECONDS)
                .pollInterval(underTest.refreshInterval, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(underTest.requestAuthToken()).isTrue());
    }
}
