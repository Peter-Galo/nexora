package com.nexora.dto.inventory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Stock entity.
 */
@Schema(description = "Stock information for a product in a warehouse")
public class StockDTO {
    
    @Schema(description = "Stock ID", example = "5ee0d5d6-5e72-4f73-adfd-691b8c9f136a")
    private UUID id;
    
    @Schema(description = "Product information", required = true)
    @NotNull(message = "Product is required")
    private ProductDTO product;
    
    @Schema(description = "Warehouse information", required = true)
    @NotNull(message = "Warehouse is required")
    private WarehouseDTO warehouse;
    
    @Schema(description = "Current quantity in stock", example = "100", required = true)
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity = 0;
    
    @Schema(description = "Minimum stock level (for low stock alerts)", example = "10")
    @Min(value = 0, message = "Minimum stock level cannot be negative")
    private Integer minStockLevel = 0;
    
    @Schema(description = "Maximum stock level (for over stock alerts)", example = "200")
    @Min(value = 0, message = "Maximum stock level cannot be negative")
    private Integer maxStockLevel;
    
    @Schema(description = "Date and time of last restock", example = "2023-01-25T09:15:00")
    private LocalDateTime lastRestockDate;
    
    @Schema(description = "Date and time when the stock record was created", example = "2023-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Date and time when the stock record was last updated", example = "2023-01-20T14:45:00")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Whether the stock is low (quantity <= minStockLevel)", example = "false")
    private boolean lowStock;
    
    @Schema(description = "Whether the stock is over maximum level (quantity >= maxStockLevel)", example = "false")
    private boolean overStock;
    
    // Default constructor
    public StockDTO() {
    }
    
    // Constructor with required fields
    public StockDTO(ProductDTO product, WarehouseDTO warehouse, Integer quantity) {
        this.product = product;
        this.warehouse = warehouse;
        this.quantity = quantity;
    }
    
    // Full constructor
    public StockDTO(UUID id, ProductDTO product, WarehouseDTO warehouse, Integer quantity,
                    Integer minStockLevel, Integer maxStockLevel, LocalDateTime lastRestockDate,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.product = product;
        this.warehouse = warehouse;
        this.quantity = quantity;
        this.minStockLevel = minStockLevel;
        this.maxStockLevel = maxStockLevel;
        this.lastRestockDate = lastRestockDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.calculateStockStatus();
    }
    
    // Calculate stock status
    private void calculateStockStatus() {
        this.lowStock = quantity <= minStockLevel;
        this.overStock = maxStockLevel != null && quantity >= maxStockLevel;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public ProductDTO getProduct() {
        return product;
    }
    
    public void setProduct(ProductDTO product) {
        this.product = product;
    }
    
    public WarehouseDTO getWarehouse() {
        return warehouse;
    }
    
    public void setWarehouse(WarehouseDTO warehouse) {
        this.warehouse = warehouse;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        calculateStockStatus();
    }
    
    public Integer getMinStockLevel() {
        return minStockLevel;
    }
    
    public void setMinStockLevel(Integer minStockLevel) {
        this.minStockLevel = minStockLevel;
        calculateStockStatus();
    }
    
    public Integer getMaxStockLevel() {
        return maxStockLevel;
    }
    
    public void setMaxStockLevel(Integer maxStockLevel) {
        this.maxStockLevel = maxStockLevel;
        calculateStockStatus();
    }
    
    public LocalDateTime getLastRestockDate() {
        return lastRestockDate;
    }
    
    public void setLastRestockDate(LocalDateTime lastRestockDate) {
        this.lastRestockDate = lastRestockDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public boolean isLowStock() {
        return lowStock;
    }
    
    public boolean isOverStock() {
        return overStock;
    }


}