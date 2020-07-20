package com.autoretouch.client;

import com.autoretouch.client.model.Workflow;

import com.autoretouch.client.model.WorkflowExcution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class WorkflowExecutionIT {

    private AutoRetouchClient underTest;
    private Workflow givenWorkflow;
    private FileSystemResource givenSampleImage;

    @BeforeEach
    void setUp() throws IOException {
        underTest = DeviceAuthIT.createOrGetDevelopmentClient();
        givenWorkflow = underTest.getWorkflows().get(0);
        givenSampleImage = new FileSystemResource(getClass().getClassLoader().getResource("sample_image.jpg").getFile());
    }

    @Test
    void shouldTriggerOneWorkflowExecutionForGivenWorkflow() throws IOException {
        BigInteger initialBalance = requestInitialBalance();
        String executionId = startExecutionWithGivenSampleImage();
        waitUntilWorkflowExecutionIsSuccessful(executionId);
        assertThatCurrentBalanceIsReducedByTheCostOfOneWorkflowExecution(initialBalance);
        WorkflowExcution finishedExecution = getFinishedWorkflowExcutionResult(executionId);
        downloadResultToTemporaryFile(finishedExecution);
    }

    private void downloadResultToTemporaryFile(WorkflowExcution finishedExecution) throws IOException {
        File resultingFile = File.createTempFile("result", ".png");
        resultingFile.deleteOnExit();

        HttpStatus downloadStatus = underTest.downloadWorkflowExecutionResultImage(finishedExecution, new FileOutputStream(resultingFile));

        assertThat(downloadStatus).isEqualTo(HttpStatus.OK);
        assertThat(resultingFile.length()).isGreaterThan(0L);
    }

    private WorkflowExcution getFinishedWorkflowExcutionResult(String executionId) {
        WorkflowExcution finishedExecution = underTest.getWorkflowExecution(executionId);
        assertThat(finishedExecution.getResultContentHash()).isNotNull();
        assertThat(finishedExecution.getResultFileName()).contains("sample_image");
        return finishedExecution;
    }

    private void assertThatCurrentBalanceIsReducedByTheCostOfOneWorkflowExecution(BigInteger initialBalance) {
        BigInteger currentBalance = underTest.getBalance();
        assertThat(currentBalance).isEqualTo(initialBalance.subtract(BigInteger.TEN));
    }

    private String startExecutionWithGivenSampleImage() {
        String executionId = underTest.createWorkflowExecution(givenWorkflow.getId(), givenSampleImage);
        assertThat(executionId).isNotNull();
        return executionId;
    }

    private BigInteger requestInitialBalance() {
        BigInteger initialBalance = underTest.getBalance();
        assertThat(initialBalance).isGreaterThanOrEqualTo(BigInteger.TEN);
        return initialBalance;
    }

    private void waitUntilWorkflowExecutionIsSuccessful(String executionId) {
        await().atMost(60, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(underTest.getWorkflowExecution(executionId).getStatus()).isEqualTo("COMPLETED"));
    }
}
