package com.nexora.service.inventory;

import com.nexora.dto.inventory.ProductDTO;

import java.util.List;

/**
 * Service interface for managing products.
 */
public interface ProductService {
    
    /**
     * Get all products.
     *
     * @return a list of all products
     */
    List<ProductDTO> getAllProducts();
    
    /**
     * Get all active products.
     *
     * @return a list of active products
     */
    List<ProductDTO> getActiveProducts();
    
    /**
     * Get a product by its ID.
     *
     * @param id the product ID
     * @return the product with the specified ID
     * @throws com.nexora.exception.ApplicationException if the product is not found
     */
    ProductDTO getProductById(Long id);
    
    /**
     * Get a product by its code.
     *
     * @param code the product code
     * @return the product with the specified code
     * @throws com.nexora.exception.ApplicationException if the product is not found
     */
    ProductDTO getProductByCode(String code);
    
    /**
     * Create a new product.
     *
     * @param productDTO the product data
     * @return the created product
     * @throws com.nexora.exception.ApplicationException if a product with the same code already exists
     */
    ProductDTO createProduct(ProductDTO productDTO);
    
    /**
     * Update an existing product.
     *
     * @param id the ID of the product to update
     * @param productDTO the updated product data
     * @return the updated product
     * @throws com.nexora.exception.ApplicationException if the product is not found
     */
    ProductDTO updateProduct(Long id, ProductDTO productDTO);
    
    /**
     * Delete a product by its ID.
     *
     * @param id the ID of the product to delete
     * @throws com.nexora.exception.ApplicationException if the product is not found
     */
    void deleteProduct(Long id);
    
    /**
     * Deactivate a product by its ID.
     *
     * @param id the ID of the product to deactivate
     * @return the deactivated product
     * @throws com.nexora.exception.ApplicationException if the product is not found
     */
    ProductDTO deactivateProduct(Long id);
    
    /**
     * Activate a product by its ID.
     *
     * @param id the ID of the product to activate
     * @return the activated product
     * @throws com.nexora.exception.ApplicationException if the product is not found
     */
    ProductDTO activateProduct(Long id);
    
    /**
     * Find products by category.
     *
     * @param category the product category
     * @return a list of products in the specified category
     */
    List<ProductDTO> getProductsByCategory(String category);
    
    /**
     * Find products by brand.
     *
     * @param brand the product brand
     * @return a list of products of the specified brand
     */
    List<ProductDTO> getProductsByBrand(String brand);
    
    /**
     * Search products by name.
     *
     * @param name the text to search for in product names
     * @return a list of products whose names contain the specified text
     */
    List<ProductDTO> searchProductsByName(String name);
}