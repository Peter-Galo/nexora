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
                .map(product -> entityMapper.mapToDTO(product, ProductDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getActiveProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(product -> entityMapper.mapToDTO(product, ProductDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(UUID id) {
        return productRepository.findById(id)
                .map(product -> entityMapper.mapToDTO(product, ProductDTO.class))
                .orElseThrow(() -> new ApplicationException("Product not found with id: " + id, "PRODUCT_NOT_FOUND"));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductByCode(String code) {
        return productRepository.findByCode(code)
                .map(product -> entityMapper.mapToDTO(product, ProductDTO.class))
                .orElseThrow(() -> new ApplicationException("Product not found with code: " + code, "PRODUCT_NOT_FOUND"));
    }

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        if (productRepository.existsByCode(productDTO.getCode())) {
            throw new ApplicationException("Product with code " + productDTO.getCode() + " already exists", "PRODUCT_CODE_EXISTS");
        }

        Product product = entityMapper.mapToEntity(productDTO, Product.class);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);
        return entityMapper.mapToDTO(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProduct(UUID id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("Product not found with id: " + id, "PRODUCT_NOT_FOUND"));

        if (!existingProduct.getCode().equals(productDTO.getCode()) &&
                productRepository.existsByCode(productDTO.getCode())) {
            throw new ApplicationException("Product with code " + productDTO.getCode() + " already exists", "PRODUCT_CODE_EXISTS");
        }

        entityMapper.mapToExistingEntity(productDTO, existingProduct);

        existingProduct.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(existingProduct);
        return entityMapper.mapToDTO(updatedProduct, ProductDTO.class);
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
        return entityMapper.mapToDTO(updatedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO activateProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("Product not found with id: " + id, "PRODUCT_NOT_FOUND"));
        product.setActive(true);
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);
        return entityMapper.mapToDTO(updatedProduct, ProductDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(product -> entityMapper.mapToDTO(product, ProductDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand).stream()
                .map(product -> entityMapper.mapToDTO(product, ProductDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(product -> entityMapper.mapToDTO(product, ProductDTO.class))
                .collect(Collectors.toList());
    }
}
