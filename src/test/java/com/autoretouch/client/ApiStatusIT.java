package com.autoretouch.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiStatusIT {

    @Test
    void shouldReturnStatusOfProductionApi() {
        AutoRetouchClient underTest = new AutoRetouchClient();
        assertThat(underTest.getApiStatus()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnStatusOfDevelopmentApi() {
        AutoRetouchClient underTest = new AutoRetouchClient().useDevelopmentEnvironment();
        assertThat(underTest.getApiStatus()).isEqualTo(HttpStatus.OK);
    }
}
