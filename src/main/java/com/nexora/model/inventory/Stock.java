package com.nexora.model.inventory;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing stock levels of products in warehouses.
 */
@Entity
@Table(name = "stocks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"product_id", "warehouse_id"})
})
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @NotNull(message = "Product is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_uuid", nullable = false)
    private Product product;

    @NotNull(message = "Warehouse is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_uuid", nullable = false)
    private Warehouse warehouse;

    @Min(value = 0, message = "Quantity cannot be negative")
    @Column(nullable = false)
    private Integer quantity = 0;

    @Min(value = 0, message = "Minimum stock level cannot be negative")
    @Column(name = "min_stock_level")
    private Integer minStockLevel = 0;

    @Min(value = 0, message = "Maximum stock level cannot be negative")
    @Column(name = "max_stock_level")
    private Integer maxStockLevel;

    @Column(name = "last_restock_date")
    private LocalDateTime lastRestockDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default constructor
    public Stock() {
    }

    // Constructor with required fields
    public Stock(Product product, Warehouse warehouse, Integer quantity) {
        this.product = product;
        this.warehouse = warehouse;
        this.quantity = quantity;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Full constructor
    public Stock(UUID uuid, Product product, Warehouse warehouse, Integer quantity,
                 Integer minStockLevel, Integer maxStockLevel, LocalDateTime lastRestockDate,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.uuid = uuid;
        this.product = product;
        this.warehouse = warehouse;
        this.quantity = quantity;
        this.minStockLevel = minStockLevel;
        this.maxStockLevel = maxStockLevel;
        this.lastRestockDate = lastRestockDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(Integer minStockLevel) {
        this.minStockLevel = minStockLevel;
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

    // Business methods
    public boolean isLowStock() {
        return quantity <= minStockLevel;
    }

    public boolean isOverStock() {
        return maxStockLevel != null && quantity >= maxStockLevel;
    }

    public void addStock(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot add negative stock amount");
        }
        this.quantity += amount;
        this.lastRestockDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void removeStock(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot remove negative stock amount");
        }
        if (amount > this.quantity) {
            throw new IllegalArgumentException("Cannot remove more stock than available");
        }
        this.quantity -= amount;
        this.updatedAt = LocalDateTime.now();
    }

    // JPA lifecycle methods
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Stock{" +
                "uuid=" + uuid +
                ", product=" + (product != null ? product.getCode() : "null") +
                ", warehouse=" + (warehouse != null ? warehouse.getCode() : "null") +
                ", quantity=" + quantity +
                ", minStockLevel=" + minStockLevel +
                ", maxStockLevel=" + maxStockLevel +
                '}';
    }
}