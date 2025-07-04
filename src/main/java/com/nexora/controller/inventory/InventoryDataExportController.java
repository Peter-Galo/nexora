package com.nexora.controller.inventory;

import com.nexora.model.inventory.Category;
import com.nexora.model.inventory.ExportJob;
import com.nexora.model.inventory.Status;
import com.nexora.model.inventory.event.ExportRequestEvent;
import com.nexora.repository.inventory.ExportJobRepository;
import com.nexora.security.JwtService;
import com.nexora.service.event.ExportMessageProducer;
import com.nexora.service.inventory.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory/export")
@Tag(name = "Inventory Data Export", description = "APIs for managing data exports in the inventory system")
public class InventoryDataExportController {

    private final JwtService jwtService;
    private final ExportService exportService;
    private final ExportMessageProducer exportMessageProducer;
    private final ExportJobRepository exportJobRepository;

    public InventoryDataExportController(JwtService jwtService, ExportService exportService, ExportMessageProducer exportMessageProducer, ExportJobRepository exportJobRepository) {
        this.jwtService = jwtService;
        this.exportService = exportService;
        this.exportMessageProducer = exportMessageProducer;
        this.exportJobRepository = exportJobRepository;
    }

    @Operation(summary = "Request product export as XLSX",
            description = "Initiates an asynchronous export of all products as Excel")
    @ApiResponse(responseCode = "202", description = "Export job accepted")
    @GetMapping("/{category}")
    public ResponseEntity<Map<String, Object>> requestProductExport(
            @RequestHeader("Authorization") String authHeader, @PathVariable Category category) {

        // Extract user ID from JWT token
        UUID userId = jwtService.extractUserUUIDFromAuthHeader(authHeader);

        // Initiate export job
        UUID jobId = exportService.initiateExport(userId, category,"XLSX");

        // Send message to queue
        ExportRequestEvent event = new ExportRequestEvent();
        event.setJobId(jobId);
        event.setUserId(userId);
        event.setCategory(category);
        event.setExportType("XLSX");
        exportMessageProducer.sendExportRequest(event);

        // Return job ID to client
        Map<String, Object> response = new HashMap<>();
        response.put("jobId", jobId);
        response.put("message", "Export job initiated successfully");

        return ResponseEntity.accepted().body(response);
    }

    @Operation(summary = "Get export job status",
            description = "Retrieves the current status of an export job")
    @ApiResponse(responseCode = "200", description = "Job status retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Export job not found")
    @GetMapping("/status/{jobId}")
    public ResponseEntity<ExportJob> getExportStatus(@PathVariable UUID jobId) {
        return exportJobRepository.findById(jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Download exported file",
            description = "Downloads a completed export file")
    @ApiResponse(responseCode = "302", description = "Redirect to file download")
    @ApiResponse(responseCode = "404", description = "Export job not found or not completed")
    @GetMapping("/download/{jobId}")
    public ResponseEntity<?> downloadExportedFile(@PathVariable UUID jobId) {
        return exportJobRepository.findById(jobId)
                .filter(job -> Status.COMPLETED.equals(job.getStatus()))
                .map(job -> ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(job.getFileUrl()))
                        .build())
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all export jobs for authenticated user",
            description = "Retrieves all export jobs for the authenticated user, ordered by creation date descending")
    @ApiResponse(responseCode = "200", description = "Export jobs retrieved successfully")
    @GetMapping("/jobs")
    public ResponseEntity<List<ExportJob>> getUserExportJobs(@RequestHeader("Authorization") String authHeader) {
        // Extract user ID from JWT token
        UUID userId = jwtService.extractUserUUIDFromAuthHeader(authHeader);

        // Get all export jobs for the user
        List<ExportJob> exportJobs = exportJobRepository.findByUserUuidOrderByCreatedAtDesc(userId);

        return ResponseEntity.ok(exportJobs);
    }
}
