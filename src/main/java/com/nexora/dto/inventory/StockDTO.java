package com.nexora.dto.inventory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Stock entity.
 * Immutable record representing stock information for a product in a warehouse.
 */
@Schema(description = "Stock information for a product in a warehouse")
public record StockDTO(
        @Schema(
                description = "Stock ID", 
                example = "5ee0d5d6-5e72-4f73-adfd-691b8c9f136a",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        UUID uuid,

        @Schema(description = "Product information")
        @NotNull(message = "Product is required")
        ProductDTO product,

        @Schema(description = "Warehouse information")
        @NotNull(message = "Warehouse is required")
        WarehouseDTO warehouse,

        @Schema(description = "Current quantity in stock", example = "100")
        @Min(value = 0, message = "Quantity cannot be negative")
        Integer quantity,

        @Schema(description = "Minimum stock level (for low stock alerts)", example = "10")
        @Min(value = 0, message = "Minimum stock level cannot be negative")
        Integer minStockLevel,

        @Schema(description = "Maximum stock level (for over stock alerts)", example = "200")
        @Min(value = 0, message = "Maximum stock level cannot be negative")
        Integer maxStockLevel,

        @Schema(description = "Date and time of last restock", example = "2023-01-25T09:15:00")
        LocalDateTime lastRestockDate,

        @Schema(
                description = "Date and time when the stock record was created", 
                example = "2023-01-15T10:30:00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        LocalDateTime createdAt,

        @Schema(
                description = "Date and time when the stock record was last updated", 
                example = "2023-01-20T14:45:00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        LocalDateTime updatedAt
) {

    /**
     * Constructor with default values for optional fields.
     */
    public StockDTO {
        // Set default values if null
        quantity = quantity != null ? quantity : 0;
        minStockLevel = minStockLevel != null ? minStockLevel : 0;
    }

    /**
     * Determines if the stock is low (quantity <= minStockLevel).
     *
     * @return true if stock is low, false otherwise
     */
    @Schema(description = "Whether the stock is low (quantity <= minStockLevel)", example = "false")
    public boolean isLowStock() {
        return quantity != null && minStockLevel != null && quantity <= minStockLevel;
    }

    /**
     * Determines if the stock is over maximum level (quantity >= maxStockLevel).
     *
     * @return true if stock is over maximum level, false otherwise
     */
    @Schema(description = "Whether the stock is over maximum level (quantity >= maxStockLevel)", example = "false")
    public boolean isOverStock() {
        return quantity != null && maxStockLevel != null && quantity >= maxStockLevel;
    }
}
