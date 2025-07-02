package com.nexora.service.inventory;

import com.nexora.dto.inventory.ProductDTO;
import com.nexora.exception.ApplicationException;
import com.nexora.model.inventory.Product;
import com.nexora.repository.inventory.ProductRepository;
import com.nexora.service.inventory.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private static final String PRODUCT_CODE = "P001";
    private static final String PRODUCT_NAME = "Test Product";
    private static final String PRODUCT_BRAND = "Test Brand";
    private static final String PRODUCT_CATEGORY = "Test Category";
    private static final String PRODUCT_SKU = "SKU001";
    private static final BigDecimal PRODUCT_PRICE = new BigDecimal("99.99");
    private static final String PRODUCT_DESCRIPTION = "Test description";

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    private Product testProduct;
    private ProductDTO testProductDTO;
    private UUID testUuid;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository);
        testUuid = UUID.randomUUID();
        now = LocalDateTime.now();
        testProduct = createTestProduct(testUuid, true);
        testProductDTO = createTestProductDTO(testUuid, true);
    }

    private Product createTestProduct(UUID uuid, boolean active) {
        Product product = new Product(PRODUCT_CODE, PRODUCT_NAME, PRODUCT_PRICE);
        product.setUuid(uuid);
        product.setDescription(PRODUCT_DESCRIPTION);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        product.setActive(active);
        product.setCategory(PRODUCT_CATEGORY);
        product.setBrand(PRODUCT_BRAND);
        product.setSku(PRODUCT_SKU);
        return product;
    }

    private ProductDTO createTestProductDTO(UUID uuid, boolean active) {
        return new ProductDTO(
                uuid,
                PRODUCT_CODE,
                PRODUCT_NAME,
                PRODUCT_DESCRIPTION,
                PRODUCT_PRICE,
                now,
                now,
                active,
                PRODUCT_CATEGORY,
                PRODUCT_BRAND,
                PRODUCT_SKU
        );
    }

    @Test
    @DisplayName("Should return all products")
    void getAllProducts_shouldReturnAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(testProduct));

        List<ProductDTO> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals(testProduct.getUuid(), result.get(0).getUuid());
        verify(productRepository).findAll();
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should return only active products")
    void getActiveProducts_shouldReturnOnlyActiveProducts() {
        when(productRepository.findByActiveTrue()).thenReturn(List.of(testProduct));

        List<ProductDTO> result = productService.getActiveProducts();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
        assertEquals(testProduct.getUuid(), result.get(0).getUuid());
        verify(productRepository).findByActiveTrue();
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should return product by id if exists")
    void getProductById_withExistingId_shouldReturnProduct() {
        when(productRepository.findById(testUuid)).thenReturn(Optional.of(testProduct));

        ProductDTO result = productService.getProductById(testUuid);

        assertNotNull(result);
        assertEquals(testUuid, result.getUuid());
        assertEquals(testProduct.getCode(), result.getCode());
        verify(productRepository).findById(testUuid);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should throw exception if product by id does not exist")
    void getProductById_withNonExistingId_shouldThrowException() {
        UUID nonExistingId = UUID.randomUUID();
        when(productRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> productService.getProductById(nonExistingId)
        );

        assertEquals("Product not found with id: " + nonExistingId, exception.getMessage());
        assertEquals("PRODUCT_NOT_FOUND", exception.getCode());
        verify(productRepository).findById(nonExistingId);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should return product by code if exists")
    void getProductByCode_withExistingCode_shouldReturnProduct() {
        when(productRepository.findByCode(PRODUCT_CODE)).thenReturn(Optional.of(testProduct));

        ProductDTO result = productService.getProductByCode(PRODUCT_CODE);

        assertNotNull(result);
        assertEquals(PRODUCT_CODE, result.getCode());
        assertEquals(testProduct.getUuid(), result.getUuid());
        verify(productRepository).findByCode(PRODUCT_CODE);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should throw exception if product by code does not exist")
    void getProductByCode_withNonExistingCode_shouldThrowException() {
        String nonExistingCode = "NONEXISTENT";
        when(productRepository.findByCode(nonExistingCode)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> productService.getProductByCode(nonExistingCode)
        );

        assertEquals("Product not found with code: " + nonExistingCode, exception.getMessage());
        assertEquals("PRODUCT_NOT_FOUND", exception.getCode());
        verify(productRepository).findByCode(nonExistingCode);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should create and return product with valid data")
    void createProduct_withValidData_shouldCreateAndReturnProduct() {
        ProductDTO newProductDTO = new ProductDTO("P002", "New Product", new BigDecimal("149.99"));
        newProductDTO.setDescription("New description");
        newProductDTO.setCategory("New Category");
        newProductDTO.setBrand("New Brand");
        newProductDTO.setSku("SKU002");

        Product savedProduct = new Product("P002", "New Product", new BigDecimal("149.99"));
        savedProduct.setUuid(UUID.randomUUID());
        savedProduct.setDescription("New description");
        savedProduct.setCategory("New Category");
        savedProduct.setBrand("New Brand");
        savedProduct.setSku("SKU002");
        savedProduct.setCreatedAt(now);
        savedProduct.setUpdatedAt(now);

        when(productRepository.existsByCode(newProductDTO.getCode())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductDTO result = productService.createProduct(newProductDTO);

        assertNotNull(result);
        assertEquals(savedProduct.getUuid(), result.getUuid());
        assertEquals(newProductDTO.getCode(), result.getCode());
        assertEquals(newProductDTO.getName(), result.getName());

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();

        assertEquals(newProductDTO.getCode(), capturedProduct.getCode());
        assertEquals(newProductDTO.getName(), capturedProduct.getName());
        assertEquals(newProductDTO.getDescription(), capturedProduct.getDescription());
        assertEquals(newProductDTO.getPrice(), capturedProduct.getPrice());
        assertEquals(newProductDTO.getCategory(), capturedProduct.getCategory());
        assertEquals(newProductDTO.getBrand(), capturedProduct.getBrand());
        assertEquals(newProductDTO.getSku(), capturedProduct.getSku());
        assertNotNull(capturedProduct.getCreatedAt());
        assertNotNull(capturedProduct.getUpdatedAt());

        verify(productRepository).existsByCode(newProductDTO.getCode());
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should throw exception when creating product with existing code")
    void createProduct_withExistingCode_shouldThrowException() {
        ProductDTO newProductDTO = new ProductDTO(PRODUCT_CODE, "New Product", new BigDecimal("149.99"));
        when(productRepository.existsByCode(newProductDTO.getCode())).thenReturn(true);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> productService.createProduct(newProductDTO)
        );

        assertEquals("Product with code P001 already exists", exception.getMessage());
        assertEquals("PRODUCT_CODE_EXISTS", exception.getCode());
        verify(productRepository).existsByCode(newProductDTO.getCode());
        verify(productRepository, never()).save(any(Product.class));
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should update and return product with valid data")
    void updateProduct_withValidData_shouldUpdateAndReturnProduct() {
        ProductDTO updateDTO = new ProductDTO(
                testUuid,
                PRODUCT_CODE,
                "Updated Product",
                "Updated description",
                new BigDecimal("129.99"),
                null,
                null,
                true,
                "Updated Category",
                "Updated Brand",
                "SKU001-UPD"
        );

        Product updatedProduct = new Product(PRODUCT_CODE, "Updated Product", new BigDecimal("129.99"));
        updatedProduct.setUuid(testUuid);
        updatedProduct.setDescription("Updated description");
        updatedProduct.setCategory("Updated Category");
        updatedProduct.setBrand("Updated Brand");
        updatedProduct.setSku("SKU001-UPD");
        updatedProduct.setCreatedAt(testProduct.getCreatedAt());
        updatedProduct.setUpdatedAt(now);

        when(productRepository.findById(testUuid)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        ProductDTO result = productService.updateProduct(testUuid, updateDTO);

        assertNotNull(result);
        assertEquals(testUuid, result.getUuid());
        assertEquals(updateDTO.getName(), result.getName());
        assertEquals(updateDTO.getDescription(), result.getDescription());
        assertEquals(updateDTO.getPrice(), result.getPrice());
        assertEquals(updateDTO.getCategory(), result.getCategory());
        assertEquals(updateDTO.getBrand(), result.getBrand());
        assertEquals(updateDTO.getSku(), result.getSku());

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();

        assertEquals(updateDTO.getName(), capturedProduct.getName());
        assertEquals(updateDTO.getDescription(), capturedProduct.getDescription());
        assertEquals(updateDTO.getPrice(), capturedProduct.getPrice());
        assertEquals(updateDTO.getCategory(), capturedProduct.getCategory());
        assertEquals(updateDTO.getBrand(), capturedProduct.getBrand());
        assertEquals(updateDTO.getSku(), capturedProduct.getSku());

        verify(productRepository).findById(testUuid);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existing product")
    void updateProduct_withNonExistingId_shouldThrowException() {
        UUID nonExistingId = UUID.randomUUID();
        ProductDTO updateDTO = new ProductDTO(PRODUCT_CODE, "Updated Product", new BigDecimal("129.99"));
        when(productRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> productService.updateProduct(nonExistingId, updateDTO)
        );

        assertEquals("Product not found with id: " + nonExistingId, exception.getMessage());
        assertEquals("PRODUCT_NOT_FOUND", exception.getCode());
        verify(productRepository).findById(nonExistingId);
        verify(productRepository, never()).save(any(Product.class));
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should throw exception when updating with existing code")
    void updateProduct_withExistingCode_shouldThrowException() {
        String existingCode = "P002";
        ProductDTO updateDTO = new ProductDTO(
                testUuid,
                existingCode,
                "Updated Product",
                "Updated description",
                new BigDecimal("129.99"),
                null,
                null,
                true,
                "Updated Category",
                "Updated Brand",
                "SKU001-UPD"
        );

        when(productRepository.findById(testUuid)).thenReturn(Optional.of(testProduct));
        when(productRepository.existsByCode(existingCode)).thenReturn(true);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> productService.updateProduct(testUuid, updateDTO)
        );

        assertEquals("Product with code P002 already exists", exception.getMessage());
        assertEquals("PRODUCT_CODE_EXISTS", exception.getCode());
        verify(productRepository).findById(testUuid);
        verify(productRepository).existsByCode(existingCode);
        verify(productRepository, never()).save(any(Product.class));
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should delete product with existing id")
    void deleteProduct_withExistingId_shouldDeleteProduct() {
        when(productRepository.existsById(testUuid)).thenReturn(true);

        productService.deleteProduct(testUuid);

        verify(productRepository).existsById(testUuid);
        verify(productRepository).deleteById(testUuid);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existing product")
    void deleteProduct_withNonExistingId_shouldThrowException() {
        UUID nonExistingId = UUID.randomUUID();
        when(productRepository.existsById(nonExistingId)).thenReturn(false);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> productService.deleteProduct(nonExistingId)
        );

        assertEquals("Product not found with id: " + nonExistingId, exception.getMessage());
        assertEquals("PRODUCT_NOT_FOUND", exception.getCode());
        verify(productRepository).existsById(nonExistingId);
        verify(productRepository, never()).deleteById(any(UUID.class));
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should deactivate product with existing id")
    void deactivateProduct_withExistingId_shouldDeactivateAndReturnProduct() {
        when(productRepository.findById(testUuid)).thenReturn(Optional.of(testProduct));

        Product deactivatedProduct = createTestProduct(testUuid, false);
        deactivatedProduct.setUpdatedAt(now);

        when(productRepository.save(any(Product.class))).thenReturn(deactivatedProduct);

        ProductDTO result = productService.deactivateProduct(testUuid);

        assertNotNull(result);
        assertFalse(result.isActive());

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();

        assertFalse(capturedProduct.isActive());
        verify(productRepository).findById(testUuid);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should activate product with existing id")
    void activateProduct_withExistingId_shouldActivateAndReturnProduct() {
        testProduct.setActive(false);
        when(productRepository.findById(testUuid)).thenReturn(Optional.of(testProduct));

        Product activatedProduct = createTestProduct(testUuid, true);
        activatedProduct.setUpdatedAt(now);

        when(productRepository.save(any(Product.class))).thenReturn(activatedProduct);

        ProductDTO result = productService.activateProduct(testUuid);

        assertNotNull(result);
        assertTrue(result.isActive());

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();

        assertTrue(capturedProduct.isActive());
        verify(productRepository).findById(testUuid);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should return products by category")
    void getProductsByCategory_shouldReturnProductsInCategory() {
        when(productRepository.findByCategory(PRODUCT_CATEGORY)).thenReturn(List.of(testProduct));

        List<ProductDTO> result = productService.getProductsByCategory(PRODUCT_CATEGORY);

        assertEquals(1, result.size());
        assertEquals(testProduct.getUuid(), result.get(0).getUuid());
        assertEquals(PRODUCT_CATEGORY, result.get(0).getCategory());
        verify(productRepository).findByCategory(PRODUCT_CATEGORY);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should return products by brand")
    void getProductsByBrand_shouldReturnProductsOfBrand() {
        when(productRepository.findByBrand(PRODUCT_BRAND)).thenReturn(List.of(testProduct));

        List<ProductDTO> result = productService.getProductsByBrand(PRODUCT_BRAND);

        assertEquals(1, result.size());
        assertEquals(testProduct.getUuid(), result.get(0).getUuid());
        assertEquals(PRODUCT_BRAND, result.get(0).getBrand());
        verify(productRepository).findByBrand(PRODUCT_BRAND);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should return products with name containing text")
    void searchProductsByName_shouldReturnProductsWithNameContainingText() {
        String searchText = "Test";
        when(productRepository.findByNameContainingIgnoreCase(searchText)).thenReturn(List.of(testProduct));

        List<ProductDTO> result = productService.searchProductsByName(searchText);

        assertEquals(1, result.size());
        assertEquals(testProduct.getUuid(), result.get(0).getUuid());
        assertTrue(result.get(0).getName().contains(searchText));
        verify(productRepository).findByNameContainingIgnoreCase(searchText);
        verifyNoMoreInteractions(productRepository);
    }
}