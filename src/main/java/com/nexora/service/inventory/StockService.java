package com.nexora.service.inventory;

import com.nexora.dto.inventory.StockDTO;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing stock.
 */
public interface StockService {
    
    /**
     * Get all stock records.
     *
     * @return a list of all stock records
     */
    List<StockDTO> getAllStocks();
    
    /**
     * Get a stock record by its ID.
     *
     * @param id the stock ID
     * @return the stock record with the specified ID
     * @throws com.nexora.exception.ApplicationException if the stock record is not found
     */
    StockDTO getStockById(UUID id);
    
    /**
     * Get stock records for a specific product.
     *
     * @param productId the product ID
     * @return a list of stock records for the product
     */
    List<StockDTO> getStocksByProductId(UUID productId);
    
    /**
     * Get stock records for a specific product by its code.
     *
     * @param productCode the product code
     * @return a list of stock records for the product
     */
    List<StockDTO> getStocksByProductCode(String productCode);
    
    /**
     * Get stock records for a specific warehouse.
     *
     * @param warehouseId the warehouse ID
     * @return a list of stock records for the warehouse
     */
    List<StockDTO> getStocksByWarehouseId(UUID warehouseId);
    
    /**
     * Get stock records for a specific warehouse by its code.
     *
     * @param warehouseCode the warehouse code
     * @return a list of stock records for the warehouse
     */
    List<StockDTO> getStocksByWarehouseCode(String warehouseCode);
    
    /**
     * Get stock record for a specific product in a specific warehouse.
     *
     * @param productId the product ID
     * @param warehouseId the warehouse ID
     * @return the stock record for the product in the warehouse
     * @throws com.nexora.exception.ApplicationException if the stock record is not found
     */
    StockDTO getStockByProductAndWarehouse(UUID productId, UUID warehouseId);
    
    /**
     * Create a new stock record.
     *
     * @param stockDTO the stock data
     * @return the created stock record
     * @throws com.nexora.exception.ApplicationException if a stock record for the same product and warehouse already exists
     */
    StockDTO createStock(StockDTO stockDTO);
    
    /**
     * Update an existing stock record.
     *
     * @param id the ID of the stock record to update
     * @param stockDTO the updated stock data
     * @return the updated stock record
     * @throws com.nexora.exception.ApplicationException if the stock record is not found
     */
    StockDTO updateStock(UUID id, StockDTO stockDTO);
    
    /**
     * Delete a stock record by its ID.
     *
     * @param id the ID of the stock record to delete
     * @throws com.nexora.exception.ApplicationException if the stock record is not found
     */
    void deleteStock(UUID id);
    
    /**
     * Add stock to a product in a warehouse.
     *
     * @param id the ID of the stock record
     * @param quantity the quantity to add
     * @return the updated stock record
     * @throws com.nexora.exception.ApplicationException if the stock record is not found or if the quantity is negative
     */
    StockDTO addStock(UUID id, int quantity);
    
    /**
     * Remove stock from a product in a warehouse.
     *
     * @param id the ID of the stock record
     * @param quantity the quantity to remove
     * @return the updated stock record
     * @throws com.nexora.exception.ApplicationException if the stock record is not found, if the quantity is negative, or if there is not enough stock
     */
    StockDTO removeStock(UUID id, int quantity);
    
    /**
     * Get all stock records with low stock (quantity <= minStockLevel).
     *
     * @return a list of stock records with low stock
     */
    List<StockDTO> getLowStocks();
    
    /**
     * Get all stock records with over stock (quantity >= maxStockLevel).
     *
     * @return a list of stock records with over stock
     */
    List<StockDTO> getOverStocks();
    
    /**
     * Get all stock records with zero quantity.
     *
     * @return a list of stock records with zero quantity
     */
    List<StockDTO> getZeroStocks();
}