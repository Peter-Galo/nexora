package com.nexora.service.event;

import com.nexora.model.inventory.Category;
import com.nexora.model.inventory.ExportJob;
import com.nexora.model.inventory.Status;
import com.nexora.model.inventory.event.ExportRequestEvent;
import com.nexora.model.inventory.event.ExportStatusUpdateEvent;
import com.nexora.repository.inventory.ExportJobRepository;
import com.nexora.service.inventory.ExportService;
import com.nexora.service.inventory.ProductService;
import com.nexora.service.inventory.StockService;
import com.nexora.service.inventory.WarehouseService;
import com.nexora.util.ExcelExportUtil;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;


@Service
public class ExportMessageConsumer {

    private final ProductService productService;
    private final StockService stockService;
    private final WarehouseService warehouseService;
    private final ExportService exportService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ExportJobRepository exportJobRepository;

    public ExportMessageConsumer(ExportJobRepository exportJobRepository, SimpMessagingTemplate messagingTemplate,
                                 ExportService exportService, ProductService productService,
                                 StockService stockService, WarehouseService warehouseService) {
        this.exportJobRepository = exportJobRepository;
        this.messagingTemplate = messagingTemplate;
        this.exportService = exportService;
        this.productService = productService;
        this.stockService = stockService;
        this.warehouseService = warehouseService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.export.name}")
    public void consumeExportRequest(ExportRequestEvent event) {
        try {
            // Update status to PROCESSING
            updateJobStatus(event.getJobId(), Status.PROCESSING, null);

            // Process the export based on category
            ExportData exportData = generateExportData(event.getCategory());

            // Upload to Digital Ocean Spaces
            exportService.uploadToSpaces(event.getJobId(), event.getUserId(), exportData.data(), exportData.filename());

        } catch (Exception e) {
            handleExportError(event.getJobId(), e);
        }
    }

    /**
     * Generates export data based on the specified category.
     *
     * @param category the category to export
     * @return ExportData containing the Excel data and filename
     * @throws Exception if an error occurs during export
     */
    private ExportData generateExportData(Category category) throws Exception {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));
        String categoryName = category.name().toLowerCase();
        String sheetName = "data_" + categoryName;

        List<?> data = getDataForCategory(category);
        byte[] excelData = ExcelExportUtil.exportToExcel(data, sheetName);
        String filename = categoryName + "_" + timestamp + ".xlsx";

        return new ExportData(excelData, filename);
    }

    /**
     * Gets the appropriate data for the specified category.
     *
     * @param category the category to get data for
     * @return a list of data items for the category
     */
    private List<?> getDataForCategory(Category category) {
        return switch (category) {
            case PRODUCT -> productService.getAllProducts();
            case STOCK -> stockService.getAllStocks();
            case WAREHOUSE -> warehouseService.getAllWarehouses();
            default -> throw new IllegalArgumentException("Unsupported category: " + category);
        };
    }

    /**
     * Updates the status of an export job.
     *
     * @param jobId        the ID of the job to update
     * @param status       the new status
     * @param errorMessage the error message (if any)
     */
    private void updateJobStatus(UUID jobId, Status status, String errorMessage) {
        ExportJob job = exportJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(status);
        if (errorMessage != null) {
            job.setErrorMessage(errorMessage);
        }
        exportJobRepository.save(job);
    }

    /**
     * Handles export errors by updating the job status and sending a WebSocket notification.
     *
     * @param jobId     the ID of the job that failed
     * @param exception the exception that occurred
     */
    private void handleExportError(UUID jobId, Exception exception) {
        String errorMessage = exception.getMessage();

        // Update job status
        updateJobStatus(jobId, Status.FAILED, errorMessage);

        // Send WebSocket update
        ExportStatusUpdateEvent updateEvent = new ExportStatusUpdateEvent();
        updateEvent.setJobId(jobId);
        updateEvent.setStatus(Status.FAILED);
        updateEvent.setErrorMessage(errorMessage);
        messagingTemplate.convertAndSend("/topic/export-status/" + jobId, updateEvent);
    }

    /**
     * Inner class to hold export data and filename.
     */
    private record ExportData(byte[] data, String filename) {
    }

}
