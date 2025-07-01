package com.nexora.repository.inventory;

import com.nexora.model.inventory.Product;
import com.nexora.model.inventory.Stock;
import com.nexora.model.inventory.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Stock entity.
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {
    
    /**
     * Find stock by product and warehouse.
     *
     * @param product the product
     * @param warehouse the warehouse
     * @return an Optional containing the stock if found, or empty if not found
     */
    Optional<Stock> findByProductAndWarehouse(Product product, Warehouse warehouse);
    
    /**
     * Find all stock records for a specific product.
     *
     * @param product the product
     * @return a list of stock records for the product
     */
    List<Stock> findByProduct(Product product);
    
    /**
     * Find all stock records for a specific warehouse.
     *
     * @param warehouse the warehouse
     * @return a list of stock records for the warehouse
     */
    List<Stock> findByWarehouse(Warehouse warehouse);
    
    /**
     * Find all stock records where quantity is less than or equal to minimum stock level.
     *
     * @return a list of stock records with low stock
     */
    @Query("SELECT s FROM Stock s WHERE s.quantity <= s.minStockLevel")
    List<Stock> findLowStock();
    
    /**
     * Find all stock records where quantity is greater than or equal to maximum stock level.
     *
     * @return a list of stock records with over stock
     */
    @Query("SELECT s FROM Stock s WHERE s.maxStockLevel IS NOT NULL AND s.quantity >= s.maxStockLevel")
    List<Stock> findOverStock();
    
    /**
     * Find all stock records for a product by product code.
     *
     * @param productCode the product code
     * @return a list of stock records for the product
     */
    @Query("SELECT s FROM Stock s JOIN s.product p WHERE p.code = :productCode")
    List<Stock> findByProductCode(@Param("productCode") String productCode);
    
    /**
     * Find all stock records for a warehouse by warehouse code.
     *
     * @param warehouseCode the warehouse code
     * @return a list of stock records for the warehouse
     */
    @Query("SELECT s FROM Stock s JOIN s.warehouse w WHERE w.code = :warehouseCode")
    List<Stock> findByWarehouseCode(@Param("warehouseCode") String warehouseCode);
    
    /**
     * Find all stock records where quantity is zero.
     *
     * @return a list of stock records with zero quantity
     */
    List<Stock> findByQuantity(Integer quantity);
}