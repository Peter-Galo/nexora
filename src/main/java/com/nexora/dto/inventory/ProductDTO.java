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
public class ProductDTO {

    @Schema(description = "Product ID (auto-generated, should not be provided in creation requests)", example = "5ee0d5d6-5e72-4f73-adfd-691b8c9f136a", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID uuid;

    @Schema(description = "Product code (unique identifier)", example = "PROD-001", required = true)
    @NotBlank(message = "Product code is required")
    @Size(min = 2, max = 50, message = "Product code must be between 2 and 50 characters")
    private String code;

    @Schema(description = "Product name", example = "Office Chair", required = true)
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;

    @Schema(description = "Product description", example = "Ergonomic office chair with adjustable height")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Schema(description = "Product price", example = "199.99", required = true)
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @Schema(description = "Date and time when the product was created", example = "2023-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Date and time when the product was last updated", example = "2023-01-20T14:45:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Whether the product is active", example = "true", defaultValue = "true")
    private boolean active = true;

    @Schema(description = "Product category", example = "Furniture")
    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    @Schema(description = "Product brand", example = "ErgoComfort")
    @Size(max = 100, message = "Brand cannot exceed 100 characters")
    private String brand;

    @Schema(description = "Product SKU (Stock Keeping Unit)", example = "SKU12345")
    @Size(max = 50, message = "SKU cannot exceed 50 characters")
    private String sku;

    // Default constructor
    public ProductDTO() {
    }

    // Constructor with required fields
    public ProductDTO(String code, String name, BigDecimal price) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.active = true;
    }

    // Full constructor
    public ProductDTO(UUID uuid, String code, String name, String description, BigDecimal price,
                      LocalDateTime createdAt, LocalDateTime updatedAt, boolean active,
                      String category, String brand, String sku) {
        this.uuid = uuid;
        this.code = code;
        this.name = name;
        this.description = description;
        this.price = price;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.active = active;
        this.category = category;
        this.brand = brand;
        this.sku = sku;
    }

    // Getters and Setters
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    @Override
    public String toString() {
        return name;
    }
}
