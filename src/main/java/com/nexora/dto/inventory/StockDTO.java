package com.nexora.dto.inventory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Stock entity.
 * Represents stock information for a product in a warehouse.
 */
@Schema(description = "Stock information for a product in a warehouse")
public class StockDTO {

    @Schema(
            description = "Stock ID",
            example = "5ee0d5d6-5e72-4f73-adfd-691b8c9f136a",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private UUID uuid;

    @Schema(description = "Product information")
    @NotNull(message = "Product is required")
    private ProductDTO product;

    @Schema(description = "Warehouse information")
    @NotNull(message = "Warehouse is required")
    private WarehouseDTO warehouse;

    @Schema(description = "Current quantity in stock", example = "100")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @Schema(description = "Minimum stock level (for low stock alerts)", example = "10")
    @Min(value = 0, message = "Minimum stock level cannot be negative")
    private Integer minStockLevel;

    @Schema(description = "Maximum stock level (for over stock alerts)", example = "200")
    @Min(value = 0, message = "Maximum stock level cannot be negative")
    private Integer maxStockLevel;

    @Schema(description = "Date and time of last restock", example = "2023-01-25T09:15:00")
    private LocalDateTime lastRestockDate;

    @Schema(
            description = "Date and time when the stock record was created",
            example = "2023-01-15T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Date and time when the stock record was last updated",
            example = "2023-01-20T14:45:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime updatedAt;

    // Default no-argument constructor
    public StockDTO() {
    }

    // All-arguments constructor
    public StockDTO(UUID uuid, ProductDTO product, WarehouseDTO warehouse, Integer quantity,
                    Integer minStockLevel, Integer maxStockLevel, LocalDateTime lastRestockDate,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.uuid = uuid;
        this.product = product;
        this.warehouse = warehouse;
        // Set default values if null (preserving original record logic)
        this.quantity = quantity != null ? quantity : 0;
        this.minStockLevel = minStockLevel != null ? minStockLevel : 0;
        this.maxStockLevel = maxStockLevel;
        this.lastRestockDate = lastRestockDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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
        // Apply default value logic when setting quantity
        this.quantity = quantity != null ? quantity : 0;
    }

    public Integer getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(Integer minStockLevel) {
        // Apply default value logic when setting minStockLevel
        this.minStockLevel = minStockLevel != null ? minStockLevel : 0;
    }

    public Integer getMaxStockLevel() {
        return maxStockLevel;
    }

    public void setMaxStockLevel(Integer maxStockLevel) {
        this.maxStockLevel = maxStockLevel;
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

    @Override
    public String toString() {
        return "StockDTO{" +
                "uuid=" + uuid +
                ", product=" + product +
                ", warehouse=" + warehouse +
                ", quantity=" + quantity +
                ", minStockLevel=" + minStockLevel +
                ", maxStockLevel=" + maxStockLevel +
                ", lastRestockDate=" + lastRestockDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
