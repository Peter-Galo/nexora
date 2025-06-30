package com.nexora.service.inventory.impl;

import com.nexora.dto.inventory.ProductDTO;
import com.nexora.exception.ApplicationException;
import com.nexora.model.inventory.Product;
import com.nexora.repository.inventory.ProductRepository;
import com.nexora.service.inventory.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the ProductService interface.
 */
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getActiveProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ApplicationException("Product not found with id: " + id, "PRODUCT_NOT_FOUND"));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductByCode(String code) {
        return productRepository.findByCode(code)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ApplicationException("Product not found with code: " + code, "PRODUCT_NOT_FOUND"));
    }

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        // Check if product with the same code already exists
        if (productRepository.existsByCode(productDTO.getCode())) {
            throw new ApplicationException("Product with code " + productDTO.getCode() + " already exists", "PRODUCT_CODE_EXISTS");
        }

        Product product = mapToEntity(productDTO);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("Product not found with id: " + id, "PRODUCT_NOT_FOUND"));

        // Check if the code is being changed and if the new code already exists
        if (!existingProduct.getCode().equals(productDTO.getCode()) && 
                productRepository.existsByCode(productDTO.getCode())) {
            throw new ApplicationException("Product with code " + productDTO.getCode() + " already exists", "PRODUCT_CODE_EXISTS");
        }

        // Update the product fields
        existingProduct.setCode(productDTO.getCode());
        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setActive(productDTO.isActive());
        existingProduct.setCategory(productDTO.getCategory());
        existingProduct.setBrand(productDTO.getBrand());
        existingProduct.setSku(productDTO.getSku());
        existingProduct.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(existingProduct);
        return mapToDTO(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ApplicationException("Product not found with id: " + id, "PRODUCT_NOT_FOUND");
        }

        productRepository.deleteById(id);
    }

    @Override
    public ProductDTO deactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("Product not found with id: " + id, "PRODUCT_NOT_FOUND"));

        product.setActive(false);
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);
        return mapToDTO(updatedProduct);
    }

    @Override
    public ProductDTO activateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("Product not found with id: " + id, "PRODUCT_NOT_FOUND"));

        product.setActive(true);
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);
        return mapToDTO(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Maps a Product entity to a ProductDTO.
     *
     * @param product the Product entity
     * @return the ProductDTO
     */
    private ProductDTO mapToDTO(Product product) {
        return new ProductDTO(
                product.getId(),
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
     * Maps a ProductDTO to a Product entity.
     *
     * @param productDTO the ProductDTO
     * @return the Product entity
     */
    private Product mapToEntity(ProductDTO productDTO) {
        Product product = new Product();
        // Don't set ID for new products, let the database generate it
        // Only set ID for existing products (in update operations)
        if (productDTO.getId() != null && productRepository.existsById(productDTO.getId())) {
            product.setId(productDTO.getId());
        }
        product.setCode(productDTO.getCode());
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setActive(productDTO.isActive());
        product.setCategory(productDTO.getCategory());
        product.setBrand(productDTO.getBrand());
        product.setSku(productDTO.getSku());

        // Don't set createdAt and updatedAt here, they are set in the service methods

        return product;
    }
}
