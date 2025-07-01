package com.nexora.service.inventory;

import com.nexora.model.inventory.Category;

import java.util.UUID;

public interface ExportService {

    UUID initiateExport(UUID userId, Category category, String exportType);
    void uploadToSpaces(UUID jobId, UUID userId, byte[] fileData, String filename);
}
