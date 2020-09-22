package com.autoretouch.client;

import com.autoretouch.client.model.Organization;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class OrganizationIT {

    @Test
    void shouldReturnAtLeastOneOrganization() throws IOException {
        AutoRetouchClient underTest = DeviceAuthIT.createOrGetClient();

        Organization firstOrganization = underTest.getMyOrganizations().stream().findFirst().get();
        assertThat(firstOrganization.getId()).isNotNull();
        assertThat(firstOrganization.getName()).isNotNull();
    }
}
