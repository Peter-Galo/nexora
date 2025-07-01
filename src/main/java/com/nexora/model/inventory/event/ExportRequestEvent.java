package com.nexora.model.inventory.event;

// Event to request an export
public class ExportRequestEvent {
    private String jobId;
    private String userId;
    private String exportType;

    public ExportRequestEvent() {
    }

    public ExportRequestEvent(String jobId, String userId, String exportType) {
        this.jobId = jobId;
        this.userId = userId;
        this.exportType = exportType;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getExportType() {
        return exportType;
    }

    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

    @Override
    public String toString() {
        return "ExportRequestEvent{" +
                "jobId='" + jobId + '\'' +
                ", userId='" + userId + '\'' +
                ", exportType='" + exportType + '\'' +
                '}';
    }
}
