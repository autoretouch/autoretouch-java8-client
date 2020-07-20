package com.autoretouch.client;

import com.autoretouch.client.model.Workflow;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GetWorkflowListTests {

    @Test
    void shouldGetListOfAllWorkflows() throws IOException {
        AutoRetouchClient underTest = DeviceAuthIT.createOrGetDevelopmentClient();

        List<Workflow> workflows = underTest.getWorkflows();
        workflows.forEach(System.out::println);
        assertThat(workflows).isNotEmpty();
    }
}
