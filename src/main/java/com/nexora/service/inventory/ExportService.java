package com.nexora.service.inventory;

public interface ExportService {

    String initiateExport(String userId, String exportType);
    void uploadToSpaces(String jobId, String userId, byte[] fileData, String filename);
}
