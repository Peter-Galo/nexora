package com.nexora.util;

import com.nexora.dto.inventory.ProductDTO;
import com.nexora.dto.inventory.StockDTO;
import com.nexora.dto.inventory.WarehouseDTO;
import com.nexora.model.inventory.Product;
import com.nexora.model.inventory.Stock;
import com.nexora.model.inventory.Warehouse;
import org.springframework.stereotype.Component;

/**
 * Utility class for mapping between entities and DTOs.
 * Centralizes mapping logic to avoid code duplication across service implementations.
 */
@Component
public class EntityMapper {

    /**
     * Maps a Product entity to a ProductDTO.
     *
     * @param product the Product entity
     * @return the ProductDTO
     */
    public ProductDTO mapToDTO(Product product) {
        if (product == null) {
            return null;
        }
        
        return new ProductDTO(
                product.getUuid(),
                product.getCode(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.isActive(),
                product.getCategory(),
                product.getBrand(),
                product.getSku()
        );
    }

    /**
     * Maps a Warehouse entity to a WarehouseDTO.
     *
     * @param warehouse the Warehouse entity
     * @return the WarehouseDTO
     */
    public WarehouseDTO mapToDTO(Warehouse warehouse) {
        if (warehouse == null) {
            return null;
        }
        
        return new WarehouseDTO(
                warehouse.getUuid(),
                warehouse.getCode(),
                warehouse.getName(),
                warehouse.getDescription(),
                warehouse.getAddress(),
                warehouse.getCity(),
                warehouse.getStateProvince(),
                warehouse.getPostalCode(),
                warehouse.getCountry(),
                warehouse.getCreatedAt(),
                warehouse.getUpdatedAt(),
                warehouse.isActive()
        );
    }

    /**
     * Maps a Stock entity to a StockDTO.
     *
     * @param stock the Stock entity
     * @return the StockDTO
     */
    public StockDTO mapToDTO(Stock stock) {
        if (stock == null) {
            return null;
        }
        
        ProductDTO productDTO = mapToDTO(stock.getProduct());
        WarehouseDTO warehouseDTO = mapToDTO(stock.getWarehouse());

        return new StockDTO(
                stock.getUuid(),
                productDTO,
                warehouseDTO,
                stock.getQuantity(),
                stock.getMinStockLevel(),
                stock.getMaxStockLevel(),
                stock.getLastRestockDate(),
                stock.getCreatedAt(),
                stock.getUpdatedAt()
        );
    }
}