package com.autoretouch.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientBuilderTest {

    @Test void shouldCreateClientInstance() {
        AutoRetouchClient.Builder underTest = AutoRetouchClient.builder()
                .pinToVersion(1);

        AutoRetouchClient client = underTest.build();

        assertThat(client.getApiStatus()).isEqualTo(HttpStatus.OK);
    }

}
