package com.nexora.model.inventory.event;

import com.nexora.model.inventory.Status;

import java.util.UUID;

// Event for export status updates
public class ExportStatusUpdateEvent {
    private UUID jobId;
    private Status status;
    private String fileUrl;
    private String errorMessage;

    public ExportStatusUpdateEvent() {
    }

    public ExportStatusUpdateEvent(UUID jobId, Status status, String fileUrl, String errorMessage) {
        this.jobId = jobId;
        this.status = status;
        this.fileUrl = fileUrl;
        this.errorMessage = errorMessage;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "ExportStatusUpdateEvent{" +
                "jobId=" + jobId +
                ", status=" + status +
                ", fileUrl='" + fileUrl + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}