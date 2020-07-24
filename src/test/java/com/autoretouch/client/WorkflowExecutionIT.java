package com.autoretouch.client;

import com.autoretouch.client.model.Workflow;

import com.autoretouch.client.model.WorkflowExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class WorkflowExecutionIT {

    private AutoRetouchClient underTest;
    private Workflow givenWorkflow;
    private FileSystemResource givenSampleImage;

    @BeforeEach
    void setUp() throws IOException {
        underTest = DeviceAuthIT.createOrGetClient();
        givenWorkflow = underTest.getWorkflows().get(0);
        givenSampleImage = new FileSystemResource(getClass().getClassLoader().getResource("sample_image.jpg").getFile());
    }

    @Test
    void shouldTriggerOneWorkflowExecutionForGivenWorkflow() throws IOException {
        BigInteger initialBalance = requestInitialBalance();
        String executionId = startExecutionWithGivenSampleImage();
        waitUntilWorkflowExecutionIsSuccessful(executionId);
        WorkflowExecution finishedExecution = getFinishedWorkflowExcutionResult(executionId);
        assertThatCurrentBalanceIsReducedByTheCostOfOneWorkflowExecution(initialBalance, finishedExecution);
        downloadResultToTemporaryFile(finishedExecution);
    }

    @Test
    void shouldReturnListOfAllExecutionsOfWorkflowContainingTheNewlyTriggeredExecution() {
        String executionId = startExecutionWithGivenSampleImage();
        List<WorkflowExecution> executions = underTest.getLatestWorkflowExecutions(givenWorkflow.getId());
        assertThatExecutionListContainsExecutionOnce(executionId, executions);
    }

    private void assertThatExecutionListContainsExecutionOnce(String executionId, List<WorkflowExecution> executions) {
        assertThat(executions).isNotEmpty();
        assertThat(executions.stream().map(WorkflowExecution::getId).collect(Collectors.toList())).containsOnlyOnce(executionId);
    }

    private void downloadResultToTemporaryFile(WorkflowExecution finishedExecution) throws IOException {
        File resultingFile = File.createTempFile("result", ".png");
        resultingFile.deleteOnExit();

        HttpStatus downloadStatus = underTest.downloadWorkflowExecutionResultImage(finishedExecution, new FileOutputStream(resultingFile));

        assertThat(downloadStatus).isEqualTo(HttpStatus.OK);
        assertThat(resultingFile.length()).isGreaterThan(0L);
    }

    private WorkflowExecution getFinishedWorkflowExcutionResult(String executionId) {
        WorkflowExecution finishedExecution = underTest.getWorkflowExecution(executionId);
        assertThat(finishedExecution.getResultContentHash()).isNotNull();
        assertThat(finishedExecution.getResultFileName()).contains("sample_image");
        assertThat(finishedExecution.getLabels().get("tenant")).isEqualTo("1234");
        return finishedExecution;
    }

    private void assertThatCurrentBalanceIsReducedByTheCostOfOneWorkflowExecution(BigInteger initialBalance, WorkflowExecution finishedExecution) {
        BigInteger currentBalance = underTest.getBalance();
        assertThat(currentBalance).isEqualTo(initialBalance.subtract(BigInteger.valueOf(finishedExecution.getChargedCredits())));
    }

    private String startExecutionWithGivenSampleImage() {
        Map<String, String> labels = new HashMap<>();
        labels.put("tenant", "1234");
        String executionId = underTest.createWorkflowExecution(givenWorkflow.getId(), givenSampleImage, labels);
        assertThat(executionId).isNotNull();
        return executionId;
    }

    private BigInteger requestInitialBalance() {
        BigInteger initialBalance = underTest.getBalance();
        assertThat(initialBalance).isGreaterThanOrEqualTo(BigInteger.TEN);
        return initialBalance;
    }

    private void waitUntilWorkflowExecutionIsSuccessful(String executionId) {
        String status = underTest.getWorkflowExecutionStatus(executionId);
        System.out.println(status);
        await().atMost(60, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(underTest.getWorkflowExecution(executionId).getStatus()).isEqualTo("COMPLETED"));
    }
}
