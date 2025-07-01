package com.nexora.model.inventory.event;

// Event for export status updates
public class ExportStatusUpdateEvent {
    private String jobId;
    private String status;
    private String fileUrl;
    private String errorMessage;

    public ExportStatusUpdateEvent() {
    }

    public ExportStatusUpdateEvent(String jobId, String status, String fileUrl, String errorMessage) {
        this.jobId = jobId;
        this.status = status;
        this.fileUrl = fileUrl;
        this.errorMessage = errorMessage;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
                "jobId='" + jobId + '\'' +
                ", status='" + status + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}