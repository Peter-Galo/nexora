package com.nexora.dto.inventory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Warehouse entity.
 */
@Schema(description = "Warehouse information")
public class WarehouseDTO {

    @Schema(description = "Warehouse UUID", example = "5ee0d5d6-5e72-4f73-adfd-691b8c9f136a")
    private UUID uuid;

    @Schema(description = "Warehouse code (unique identifier)", example = "WH-001", required = true)
    @NotBlank(message = "Warehouse code is required")
    @Size(min = 2, max = 50, message = "Warehouse code must be between 2 and 50 characters")
    private String code;

    @Schema(description = "Warehouse name", example = "Main Distribution Center", required = true)
    @NotBlank(message = "Warehouse name is required")
    @Size(min = 2, max = 100, message = "Warehouse name must be between 2 and 100 characters")
    private String name;

    @Schema(description = "Warehouse description", example = "Primary distribution center for the northeast region")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Schema(description = "Warehouse address", example = "123 Storage Lane", required = true)
    @NotBlank(message = "Address is required")
    @Size(max = 200, message = "Address cannot exceed 200 characters")
    private String address;

    @Schema(description = "City where the warehouse is located", example = "Boston", required = true)
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Schema(description = "State or province where the warehouse is located", example = "Massachusetts")
    @Size(max = 100, message = "State/Province cannot exceed 100 characters")
    private String stateProvince;

    @Schema(description = "Postal code of the warehouse location", example = "02108")
    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    private String postalCode;

    @Schema(description = "Country where the warehouse is located", example = "USA", required = true)
    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    @Schema(description = "Date and time when the warehouse was created", example = "2023-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Date and time when the warehouse was last updated", example = "2023-01-20T14:45:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Whether the warehouse is active", example = "true", defaultValue = "true")
    private boolean active = true;

    // Default constructor
    public WarehouseDTO() {
    }

    // Constructor with required fields
    public WarehouseDTO(String code, String name) {
        this.code = code;
        this.name = name;
    }

    // Full constructor
    public WarehouseDTO(UUID uuid, String code, String name, String description, String address,
                       String city, String stateProvince, String postalCode, String country,
                       LocalDateTime createdAt, LocalDateTime updatedAt, boolean active) {
        this.uuid = uuid;
        this.code = code;
        this.name = name;
        this.description = description;
        this.address = address;
        this.city = city;
        this.stateProvince = stateProvince;
        this.postalCode = postalCode;
        this.country = country;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.active = active;
    }

    // Getters and Setters
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStateProvince() {
        return stateProvince;
    }

    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    @Override
    public String toString() {
        return name;
    }
}
