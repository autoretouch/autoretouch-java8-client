package com.autoretouch.client;

import com.autoretouch.client.auth.DeviceAuthorization;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class DeviceAuthIT {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldLoadCredentialsFromDisk() throws IOException {
        AutoRetouchClient blankClient = new AutoRetouchClient().useDevelopmentEnvironment().logIn();

        waitForUserToAuthViaBrowser(blankClient);

        assertThat(blankClient.getWorkflows()).isNotEmpty();

        DeviceAuthorization authorization = blankClient.getDeviceAuthorization();

        assertThat(authorization.getRefreshToken()).isNotNull();
        assertThat(authorization.getType()).isEqualTo("Bearer");

        File deviceAuthFile = File.createTempFile("autoretouch", ".json");
        deviceAuthFile.deleteOnExit();
        ObjectWriter writer = objectMapper.writer(new DefaultPrettyPrinter());
        writer.writeValue(deviceAuthFile, authorization);

        DeviceAuthorization loadedDeviceAuth = objectMapper.readValue(deviceAuthFile, DeviceAuthorization.class);

        AutoRetouchClient clientWithGivenAuthInformation = new AutoRetouchClient().useDevelopmentEnvironment()
                .withDeviceAuth(loadedDeviceAuth);

        DeviceAuthorization refreshedAuthorization = clientWithGivenAuthInformation.getDeviceAuthorization();
        assertThat(refreshedAuthorization.getRefreshToken()).isNotNull();
        assertThat(refreshedAuthorization.getType()).isEqualTo("Bearer");


        assertThat(clientWithGivenAuthInformation.getWorkflows()).isNotEmpty();
    }

    private static void waitForUserToAuthViaBrowser(AutoRetouchClient underTest) {
        await().atMost(underTest.deviceCodeExpiresIn, TimeUnit.SECONDS)
                .pollInterval(underTest.refreshInterval, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(underTest.requestAuthToken()).isTrue());
    }


    private static AutoRetouchClient instance = null;

    public static AutoRetouchClient createOrGetClient() throws IOException {
        if (instance == null) {
            instance = new AutoRetouchClient();
            if ("development".equalsIgnoreCase(System.getenv("env"))) {
                instance.useDevelopmentEnvironment();
            }
            File credentialFile = new File("autoretouch-credentials.json");
            if (credentialFile.exists()) {
                DeviceAuthorization loadedDeviceAuth = objectMapper.readValue(credentialFile, DeviceAuthorization.class);
                instance.withDeviceAuth(loadedDeviceAuth);
            } else {
                instance.logIn();
                waitForUserToAuthViaBrowser(instance);
                DeviceAuthorization authorization = instance.getDeviceAuthorization();
                ObjectWriter writer = objectMapper.writer(new DefaultPrettyPrinter());
                writer.writeValue(credentialFile, authorization);
            }
        }
        return instance;
    }
}
