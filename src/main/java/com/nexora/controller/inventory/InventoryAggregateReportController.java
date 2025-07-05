package com.nexora.controller.inventory;

import com.nexora.service.inventory.impl.InventoryAggregateReportServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory/report")
@Tag(
        name = "Inventory Aggregate Report",
        description = "Endpoints for retrieving aggregate inventory data, including low stock, inventory value, and product stock summary."
)
public class InventoryAggregateReportController {

    private final InventoryAggregateReportServiceImpl reportService;

    public InventoryAggregateReportController(InventoryAggregateReportServiceImpl reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/aggregate")
    @Operation(
            summary = "Get aggregate inventory report",
            description = "Returns aggregate inventory data including low stock products per warehouse, inventory value per warehouse, total inventory value, and product stock summary."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Aggregate inventory report retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = """
                                            {
                                              "lowStockByWarehouse": {
                                                "Warehouse A": [
                                                  {
                                                    "productCode": "P001",
                                                    "productName": "Product 1",
                                                    "quantity": 3,
                                                    "minStockLevel": 5
                                                  }
                                                ]
                                              },
                                              "inventoryValueByWarehouse": {
                                                "Warehouse A": 1500.00
                                              },
                                              "totalInventoryValue": 3500.00,
                                              "productStockSummary": [
                                                {
                                                  "productCode": "P001",
                                                  "productName": "Product 1",
                                                  "totalQuantity": 10
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Map<String, Object>> getAggregateReport() {
        return ResponseEntity.ok(reportService.getAggregateReport());
    }
}