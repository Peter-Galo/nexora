package com.nexora.dto.inventory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Warehouse entity.
 * Immutable record representing warehouse information.
 */
@Schema(description = "Warehouse information")
public record WarehouseDTO(
        @Schema(
                description = "Warehouse UUID", 
                example = "5ee0d5d6-5e72-4f73-adfd-691b8c9f136a",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        UUID uuid,

        @Schema(description = "Warehouse code (unique identifier)", example = "WH-001")
        @NotBlank(message = "Warehouse code is required")
        @Size(min = 2, max = 50, message = "Warehouse code must be between 2 and 50 characters")
        String code,

        @Schema(description = "Warehouse name", example = "Main Distribution Center")
        @NotBlank(message = "Warehouse name is required")
        @Size(min = 2, max = 100, message = "Warehouse name must be between 2 and 100 characters")
        String name,

        @Schema(description = "Warehouse description", example = "Primary distribution center for the northeast region")
        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        @Schema(description = "Warehouse address", example = "123 Storage Lane")
        @NotBlank(message = "Address is required")
        @Size(max = 200, message = "Address cannot exceed 200 characters")
        String address,

        @Schema(description = "City where the warehouse is located", example = "Boston")
        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City cannot exceed 100 characters")
        String city,

        @Schema(description = "State or province where the warehouse is located", example = "Massachusetts")
        @Size(max = 100, message = "State/Province cannot exceed 100 characters")
        String stateProvince,

        @Schema(description = "Postal code of the warehouse location", example = "02108")
        @Size(max = 20, message = "Postal code cannot exceed 20 characters")
        String postalCode,

        @Schema(description = "Country where the warehouse is located", example = "USA")
        @NotBlank(message = "Country is required")
        @Size(max = 100, message = "Country cannot exceed 100 characters")
        String country,

        @Schema(
                description = "Date and time when the warehouse was created", 
                example = "2023-01-15T10:30:00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        LocalDateTime createdAt,

        @Schema(
                description = "Date and time when the warehouse was last updated", 
                example = "2023-01-20T14:45:00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        LocalDateTime updatedAt,

        @Schema(description = "Whether the warehouse is active", example = "true", defaultValue = "true")
        boolean active
) {

    /**
     * Constructor with default values for optional fields.
     */
    public WarehouseDTO {
        // Set default value for active if not specified
        active = active; // Keep the provided value or default from record parameter
    }

    /**
     * Returns the warehouse name as string representation.
     *
     * @return the warehouse name
     */
    @Override
    public String toString() {
        return name;
    }
}
