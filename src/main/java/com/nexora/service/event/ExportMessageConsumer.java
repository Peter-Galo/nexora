package com.nexora.service.event;

import com.nexora.dto.inventory.ProductDTO;
import com.nexora.model.inventory.ExportJob;
import com.nexora.model.inventory.event.ExportRequestEvent;
import com.nexora.model.inventory.event.ExportStatusUpdateEvent;
import com.nexora.repository.inventory.ExportJobRepository;
import com.nexora.service.inventory.ExportService;
import com.nexora.service.inventory.ProductService;
import com.nexora.util.ExcelExportUtil;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class ExportMessageConsumer {

    private final ProductService productService;
    private final ExportService exportService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ExportJobRepository exportJobRepository;

    public ExportMessageConsumer(ExportJobRepository exportJobRepository, SimpMessagingTemplate messagingTemplate, ExportService exportService, ProductService productService) {
        this.exportJobRepository = exportJobRepository;
        this.messagingTemplate = messagingTemplate;
        this.exportService = exportService;
        this.productService = productService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.export.name}")
    public void consumeExportRequest(ExportRequestEvent event) {
        try {
            // Update status to PROCESSING
            updateStatus(event.getJobId(), "PROCESSING", null, null);

            // Get products and generate Excel
            List<ProductDTO> products = productService.getAllProducts();
            byte[] excelData = ExcelExportUtil.exportToExcel(products, "Products");

            // Generate filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));
            String filename = "products_" + timestamp + ".xlsx";

            // Upload to Digital Ocean Spaces
            exportService.uploadToSpaces(event.getJobId(), event.getUserId(), excelData, filename);

        } catch (Exception e) {
            // Update status to FAILED
            updateStatus(event.getJobId(), "FAILED", null, e.getMessage());
        }
    }

    private void updateStatus(String jobId, String status, String fileUrl, String errorMessage) {
        // Update database
        ExportJob job = exportJobRepository.findByJobId(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(status);

        if (fileUrl != null) {
            job.setFileUrl(fileUrl);
        }

        if (errorMessage != null) {
            job.setErrorMessage(errorMessage);
        }

        job.setUpdatedAt(LocalDateTime.now());
        exportJobRepository.save(job);

        // Send WebSocket update
        ExportStatusUpdateEvent updateEvent = new ExportStatusUpdateEvent();
        updateEvent.setJobId(jobId);
        updateEvent.setStatus(status);
        updateEvent.setFileUrl(fileUrl);
        updateEvent.setErrorMessage(errorMessage);

        messagingTemplate.convertAndSend("/topic/export-status/" + jobId, updateEvent);
    }
}
