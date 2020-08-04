package com.autoretouch.client;

import com.autoretouch.client.model.Workflow;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GetWorkflowTests {

    @Test
    void shouldGetListOfAllWorkflows() throws IOException {
        AutoRetouchClient underTest = DeviceAuthIT.createOrGetClient().pinToVersion(1);

        List<Workflow> workflows = underTest.getWorkflows();
        assertThat(workflows).isNotEmpty();
        assertThat(workflows).allSatisfy(this::assertThatWorkflowContainsValidInformation);
    }

    @Test void shouldGetWorkflow() throws IOException {
        AutoRetouchClient underTest = DeviceAuthIT.createOrGetClient();
        String firstWorkflowId = underTest.getWorkflows().stream().findFirst().map(Workflow::getId).get();

        Workflow workflow = underTest.getWorkflow(firstWorkflowId);

        assertThatWorkflowContainsValidInformation(workflow);
    }

    private void assertThatWorkflowContainsValidInformation(Workflow workflow) {
        assertThat(workflow.getId()).isNotEmpty();
        assertThat(workflow.getVersion()).isNotEmpty();
        assertThat(workflow.getCreationDate()).isNotEmpty();
        assertThat(workflow.getExecutionPrice()).isGreaterThanOrEqualTo(10);
    }
}
