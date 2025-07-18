package com.nexora.dto.inventory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Product entity.
 */
@Schema(description = "Product information")
public record ProductDTO(
        @Schema(
                description = "Product ID (auto-generated, should not be provided in creation requests)",
                example = "5ee0d5d6-5e72-4f73-adfd-691b8c9f136a",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        UUID uuid,

        @Schema(
                description = "Product code (unique identifier)",
                example = "PROD-001"
        )
        @NotBlank(message = "Product code is required")
        @Size(min = 2, max = 50, message = "Product code must be between 2 and 50 characters")
        String code,

        @Schema(
                description = "Product name",
                example = "Office Chair"
        )
        @NotBlank(message = "Product name is required")
        @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
        String name,

        @Schema(
                description = "Product description",
                example = "Ergonomic office chair with adjustable height"
        )
        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        @Schema(
                description = "Product price",
                example = "199.99"
        )
        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        BigDecimal price,

        @Schema(
                description = "Date and time when the product was created",
                example = "2023-01-15T10:30:00"
        )
        LocalDateTime createdAt,

        @Schema(
                description = "Date and time when the product was last updated",
                example = "2023-01-20T14:45:00"
        )
        LocalDateTime updatedAt,

        @Schema(
                description = "Whether the product is active",
                example = "true",
                defaultValue = "true"
        )
        boolean active,

        @Schema(
                description = "Product category",
                example = "Furniture"
        )
        @Size(max = 100, message = "Category cannot exceed 100 characters")
        String category,

        @Schema(
                description = "Product brand",
                example = "ErgoComfort"
        )
        @Size(max = 100, message = "Brand cannot exceed 100 characters")
        String brand,

        @Schema(
                description = "Product SKU (Stock Keeping Unit)",
                example = "SKU12345"
        )
        @Size(max = 50, message = "SKU cannot exceed 50 characters")
        String sku
) {
}