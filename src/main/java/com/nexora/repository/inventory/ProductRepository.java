package com.nexora.repository.inventory;

import com.nexora.model.inventory.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Find a product by its unique code.
     *
     * @param code the product code
     * @return an Optional containing the product if found, or empty if not found
     */
    Optional<Product> findByCode(String code);
    
    /**
     * Check if a product with the given code exists.
     *
     * @param code the product code
     * @return true if a product with the code exists, false otherwise
     */
    boolean existsByCode(String code);
    
    /**
     * Find all active products.
     *
     * @return a list of active products
     */
    List<Product> findByActiveTrue();
    
    /**
     * Find products by category.
     *
     * @param category the product category
     * @return a list of products in the specified category
     */
    List<Product> findByCategory(String category);
    
    /**
     * Find products by brand.
     *
     * @param brand the product brand
     * @return a list of products of the specified brand
     */
    List<Product> findByBrand(String brand);
    
    /**
     * Find products by SKU.
     *
     * @param sku the product SKU
     * @return a list of products with the specified SKU
     */
    List<Product> findBySku(String sku);
    
    /**
     * Find products by name containing the given text (case-insensitive).
     *
     * @param name the text to search for in product names
     * @return a list of products whose names contain the specified text
     */
    List<Product> findByNameContainingIgnoreCase(String name);
}