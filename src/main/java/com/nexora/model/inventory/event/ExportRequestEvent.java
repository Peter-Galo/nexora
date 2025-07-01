package com.nexora.model.inventory.event;

import com.nexora.model.inventory.Category;

import java.util.UUID;

// Event to request an export
public class ExportRequestEvent {
    private UUID jobId;
    private UUID userId;
    private Category category;
    private String exportType;

    public ExportRequestEvent() {
    }

    public ExportRequestEvent(UUID jobId, UUID userId, Category category, String exportType) {
        this.jobId = jobId;
        this.userId = userId;
        this.category = category;
        this.exportType = exportType;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
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
                "jobId=" + jobId +
                ", userId=" + userId +
                ", category=" + category +
                ", exportType='" + exportType + '\'' +
                '}';
    }
}
