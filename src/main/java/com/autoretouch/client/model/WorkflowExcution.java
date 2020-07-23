package com.autoretouch.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class WorkflowExcution {
    @JsonProperty("id")
    private String id;

    @JsonProperty("workflow")
    private String workflowId;

    @JsonProperty("workflowVersion")
    private String workflowVersionId;

    @JsonProperty("companyId")
    private String companyId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("startedAt")
    private String startedAt;

    @JsonProperty("finishedAt")
    private String finishedAt;

    @JsonProperty("inputFileName")
    private String inputFileName;

    @JsonProperty("resultContentHash")
    private String resultContentHash;

    @JsonProperty("resultFileName")
    private String resultFileName;

    @JsonProperty("resultPath")
    private String resultPath;

    @JsonProperty("labels")
    private Map<String, String> labels;

    @Override
    public String toString() {
        return "WorkflowExcution{" +
                "id='" + id + '\'' +
                ", workflowId='" + workflowId + '\'' +
                ", workflowVersionId='" + workflowVersionId + '\'' +
                ", companyId='" + companyId + '\'' +
                ", status='" + status + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", startedAt='" + startedAt + '\'' +
                ", finishedAt='" + finishedAt + '\'' +
                ", inputFileName='" + inputFileName + '\'' +
                ", resultContentHash='" + resultContentHash + '\'' +
                ", resultFileName='" + resultFileName + '\'' +
                ", resultPath='" + resultPath + '\'' +
                ", labels=" + labels +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowVersionId() {
        return workflowVersionId;
    }

    public void setWorkflowVersionId(String workflowVersionId) {
        this.workflowVersionId = workflowVersionId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    public String getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(String finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public void setInputFileName(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    public String getResultContentHash() {
        return resultContentHash;
    }

    public void setResultContentHash(String resultContentHash) {
        this.resultContentHash = resultContentHash;
    }

    public String getResultFileName() {
        return resultFileName;
    }

    public void setResultFileName(String resultFileName) {
        this.resultFileName = resultFileName;
    }

    public String getResultPath() {
        return resultPath;
    }

    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
}
