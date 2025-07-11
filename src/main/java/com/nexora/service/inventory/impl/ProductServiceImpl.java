package com.nexora.service.inventory.impl;

import com.nexora.dto.inventory.ProductDTO;
import com.nexora.exception.ApplicationException;
import com.nexora.model.inventory.Product;
import com.nexora.repository.inventory.ProductRepository;
import com.nexora.service.inventory.ProductService;
import com.nexora.util.EntityMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the ProductService interface.
 */
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final EntityMapper entityMapper;

    public ProductServiceImpl(ProductRepository productRepository, EntityMapper entityMapper) {
        this.productRepository = productRepository;
        this.entityMapper = entityMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(entityMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getActiveProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(entityMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(UUID id) {
        return productRepository.findById(id)
                .map(entityMapper::mapToDTO)
                .orElseThrow(() -> new ApplicationException("Product not found with id: " + id, "PRODUCT_NOT_FOUND"));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductByCode(String code) {
        return productRepository.findByCode(code)
                .map(entityMapper::mapToDTO)
                .orElseThrow(() -> new ApplicationException("Product not found with code: " + code, "PRODUCT_NOT_FOUND"));
    }

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        // Check if product with the same code already exists
        if (productRepository.existsByCode(productDTO.code())) {
            throw new ApplicationException("Product with code " + productDTO.code() + " already exists", "PRODUCT_CODE_EXISTS");
        }

        Product product = mapToEntity(productDTO);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);
        return entityMapper.mapToDTO(savedProduct);
    }

    @Override
    public ProductDTO updateProduct(UUID id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("Product not found with id: " + id, "PRODUCT_NOT_FOUND"));

        // Check if the code is being changed and if the new code already exists
        if (!existingProduct.getCode().equals(productDTO.code()) &&
                productRepository.existsByCode(productDTO.code())) {
            throw new ApplicationException("Product with code " + productDTO.code() + " already exists", "PRODUCT_CODE_EXISTS");
        }

        // Update the product fields
        existingProduct.setCode(productDTO.code());
        existingProduct.setName(productDTO.name());
        existingProduct.setDescription(productDTO.description());
        existingProduct.setPrice(productDTO.price());
        existingProduct.setActive(productDTO.active());
        existingProduct.setCategory(productDTO.category());
        existingProduct.setBrand(productDTO.brand());
        existingProduct.setSku(productDTO.sku());
        existingProduct.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(existingProduct);
        return entityMapper.mapToDTO(updatedProduct);
    }

    @Override
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ApplicationException("Product not found with id: " + id, "PRODUCT_NOT_FOUND");
        }

        productRepository.deleteById(id);
    }

    @Override
    public ProductDTO deactivateProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("Product not found with id: " + id, "PRODUCT_NOT_FOUND"));

        product.setActive(false);
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);
        return entityMapper.mapToDTO(updatedProduct);
    }

    @Override
    public ProductDTO activateProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("Product not found with id: " + id, "PRODUCT_NOT_FOUND"));

        product.setActive(true);
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);
        return entityMapper.mapToDTO(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(entityMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand).stream()
                .map(entityMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(entityMapper::mapToDTO)
                .collect(Collectors.toList());
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
        if (productDTO.uuid() != null && productRepository.existsById(productDTO.uuid())) {
            product.setUuid(productDTO.uuid());
        }
        product.setCode(productDTO.code());
        product.setName(productDTO.name());
        product.setDescription(productDTO.description());
        product.setPrice(productDTO.price());
        product.setActive(productDTO.active());
        product.setCategory(productDTO.category());
        product.setBrand(productDTO.brand());
        product.setSku(productDTO.sku());

        return product;
    }
}
