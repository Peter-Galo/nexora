package com.nexora.controller.inventory;

import com.nexora.dto.inventory.ProductDTO;
import com.nexora.dto.inventory.StockDTO;
import com.nexora.service.inventory.StockService;
import com.nexora.util.ExcelExportUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing stock.
 */
@RestController
@RequestMapping("/api/v1/inventory/stocks")
@Tag(name = "Stock Management", description = "APIs for managing stock levels in the inventory system")
public class StockController {
    
    private final StockService stockService;
    
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }
    
    @Operation(summary = "Get all stock records", description = "Retrieves a list of all stock records")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of stock records"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<StockDTO>> getAllStocks() {
        return ResponseEntity.ok(stockService.getAllStocks());
    }
    
    @Operation(summary = "Get stock record by ID", description = "Retrieves a stock record by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the stock record"),
        @ApiResponse(responseCode = "404", description = "Stock record not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<StockDTO> getStockById(
            @Parameter(description = "ID of the stock record to retrieve", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(stockService.getStockById(id));
    }
    
    @Operation(summary = "Get stock records for a product", description = "Retrieves all stock records for a specific product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of stock records"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<StockDTO>> getStocksByProductId(
            @Parameter(description = "UUID of the product", required = true)
            @PathVariable UUID productId) {
        return ResponseEntity.ok(stockService.getStocksByProductId(productId));
    }
    
    @Operation(summary = "Get stock records for a product by code", description = "Retrieves all stock records for a specific product by its code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of stock records"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/product/code/{productCode}")
    public ResponseEntity<List<StockDTO>> getStocksByProductCode(
            @Parameter(description = "Code of the product", required = true)
            @PathVariable String productCode) {
        return ResponseEntity.ok(stockService.getStocksByProductCode(productCode));
    }
    
    @Operation(summary = "Get stock records for a warehouse", description = "Retrieves all stock records for a specific warehouse")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of stock records"),
        @ApiResponse(responseCode = "404", description = "Warehouse not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<StockDTO>> getStocksByWarehouseId(
            @Parameter(description = "ID of the warehouse", required = true)
            @PathVariable UUID warehouseId) {
        return ResponseEntity.ok(stockService.getStocksByWarehouseId(warehouseId));
    }
    
    @Operation(summary = "Get stock records for a warehouse by code", description = "Retrieves all stock records for a specific warehouse by its code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of stock records"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/warehouse/code/{warehouseCode}")
    public ResponseEntity<List<StockDTO>> getStocksByWarehouseCode(
            @Parameter(description = "Code of the warehouse", required = true)
            @PathVariable String warehouseCode) {
        return ResponseEntity.ok(stockService.getStocksByWarehouseCode(warehouseCode));
    }
    
    @Operation(summary = "Get stock record for a product in a warehouse", description = "Retrieves the stock record for a specific product in a specific warehouse")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the stock record"),
        @ApiResponse(responseCode = "404", description = "Stock record not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/product/{productId}/warehouse/{warehouseId}")
    public ResponseEntity<StockDTO> getStockByProductAndWarehouse(
            @Parameter(description = "UUID of the product", required = true)
            @PathVariable UUID productId,
            @Parameter(description = "UUID of the warehouse", required = true)
            @PathVariable UUID warehouseId) {
        return ResponseEntity.ok(stockService.getStockByProductAndWarehouse(productId, warehouseId));
    }
    
    @Operation(summary = "Create a new stock record", description = "Creates a new stock record with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Stock record successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid input or stock record already exists"),
        @ApiResponse(responseCode = "404", description = "Product or warehouse not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<StockDTO> createStock(
            @Parameter(description = "Stock record information", required = true, schema = @Schema(implementation = StockDTO.class))
            @Valid @RequestBody StockDTO stockDTO) {
        return new ResponseEntity<>(stockService.createStock(stockDTO), HttpStatus.CREATED);
    }
    
    @Operation(summary = "Update an existing stock record", description = "Updates an existing stock record with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock record successfully updated"),
        @ApiResponse(responseCode = "400", description = "Invalid input or stock record already exists"),
        @ApiResponse(responseCode = "404", description = "Stock record, product, or warehouse not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<StockDTO> updateStock(
            @Parameter(description = "ID of the stock record to update", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Updated stock record information", required = true, schema = @Schema(implementation = StockDTO.class))
            @Valid @RequestBody StockDTO stockDTO) {
        return ResponseEntity.ok(stockService.updateStock(id, stockDTO));
    }
    
    @Operation(summary = "Delete a stock record", description = "Deletes a stock record by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Stock record successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Stock record not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(
            @Parameter(description = "ID of the stock record to delete", required = true)
            @PathVariable UUID id) {
        stockService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Add stock", description = "Adds a specified quantity to a stock record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock successfully added"),
        @ApiResponse(responseCode = "400", description = "Invalid quantity"),
        @ApiResponse(responseCode = "404", description = "Stock record not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}/add")
    public ResponseEntity<StockDTO> addStock(
            @Parameter(description = "ID of the stock record", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Quantity to add", required = true)
            @RequestParam int quantity) {
        return ResponseEntity.ok(stockService.addStock(id, quantity));
    }
    
    @Operation(summary = "Remove stock", description = "Removes a specified quantity from a stock record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock successfully removed"),
        @ApiResponse(responseCode = "400", description = "Invalid quantity or insufficient stock"),
        @ApiResponse(responseCode = "404", description = "Stock record not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}/remove")
    public ResponseEntity<StockDTO> removeStock(
            @Parameter(description = "ID of the stock record", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Quantity to remove", required = true)
            @RequestParam int quantity) {
        return ResponseEntity.ok(stockService.removeStock(id, quantity));
    }
    
    @Operation(summary = "Get low stock records", description = "Retrieves all stock records with low stock (quantity <= minStockLevel)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of low stock records"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/low")
    public ResponseEntity<List<StockDTO>> getLowStocks() {
        return ResponseEntity.ok(stockService.getLowStocks());
    }
    
    @Operation(summary = "Get over stock records", description = "Retrieves all stock records with over stock (quantity >= maxStockLevel)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of over stock records"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/over")
    public ResponseEntity<List<StockDTO>> getOverStocks() {
        return ResponseEntity.ok(stockService.getOverStocks());
    }
    
    @Operation(summary = "Get zero stock records", description = "Retrieves all stock records with zero quantity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of zero stock records"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/zero")
    public ResponseEntity<List<StockDTO>> getZeroStocks() {
        return ResponseEntity.ok(stockService.getZeroStocks());
    }
}