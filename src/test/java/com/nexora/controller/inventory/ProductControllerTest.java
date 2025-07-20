package com.nexora.controller.inventory;

import com.nexora.dto.inventory.ProductDTO;
import com.nexora.exception.ApplicationException;
import com.nexora.service.inventory.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private ProductDTO testProductDTO;
    private UUID productUuid;

    @BeforeEach
    void setUp() {
        productUuid = UUID.randomUUID();
        
        testProductDTO = new ProductDTO();
        testProductDTO.setUuid(productUuid);
        testProductDTO.setCode("PROD001");
        testProductDTO.setName("Test Product");
        testProductDTO.setDescription("Test Description");
        testProductDTO.setPrice(new BigDecimal("99.99"));
        testProductDTO.setActive(true);
        testProductDTO.setCategory("Electronics");
        testProductDTO.setBrand("TestBrand");
        testProductDTO.setSku("SKU001");
    }

    @Test
    void testGetAllProducts_ShouldReturnProductList() {
        // Given
        List<ProductDTO> products = Arrays.asList(testProductDTO);
        when(productService.getAllProducts()).thenReturn(products);

        // When
        ResponseEntity<List<ProductDTO>> response = productController.getAllProducts();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getUuid()).isEqualTo(productUuid);
        assertThat(response.getBody().get(0).getCode()).isEqualTo("PROD001");
        assertThat(response.getBody().get(0).getName()).isEqualTo("Test Product");
        verify(productService).getAllProducts();
    }

    @Test
    void testGetActiveProducts_ShouldReturnActiveProductList() {
        // Given
        List<ProductDTO> activeProducts = Arrays.asList(testProductDTO);
        when(productService.getActiveProducts()).thenReturn(activeProducts);

        // When
        ResponseEntity<List<ProductDTO>> response = productController.getActiveProducts();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).isActive()).isTrue();
        verify(productService).getActiveProducts();
    }

    @Test
    void testGetProductById_WhenProductExists_ShouldReturnProduct() {
        // Given
        when(productService.getProductById(productUuid)).thenReturn(testProductDTO);

        // When
        ResponseEntity<ProductDTO> response = productController.getProductById(productUuid);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getUuid()).isEqualTo(productUuid);
        assertThat(response.getBody().getCode()).isEqualTo("PROD001");
        verify(productService).getProductById(productUuid);
    }

    @Test
    void testGetProductById_WhenProductNotFound_ShouldThrowException() {
        // Given
        when(productService.getProductById(productUuid))
                .thenThrow(new ApplicationException("Product not found with id: " + productUuid, "PRODUCT_NOT_FOUND"));

        // When & Then
        assertThatThrownBy(() -> productController.getProductById(productUuid))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Product not found with id: " + productUuid);
        verify(productService).getProductById(productUuid);
    }

    @Test
    void testGetProductByCode_WhenProductExists_ShouldReturnProduct() {
        // Given
        when(productService.getProductByCode("PROD001")).thenReturn(testProductDTO);

        // When
        ResponseEntity<ProductDTO> response = productController.getProductByCode("PROD001");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCode()).isEqualTo("PROD001");
        verify(productService).getProductByCode("PROD001");
    }

    @Test
    void testCreateProduct_WithValidData_ShouldReturnCreatedProduct() {
        // Given
        ProductDTO createDTO = new ProductDTO();
        createDTO.setCode("PROD002");
        createDTO.setName("New Product");
        createDTO.setPrice(new BigDecimal("149.99"));
        createDTO.setActive(true);

        ProductDTO createdProduct = new ProductDTO();
        createdProduct.setUuid(UUID.randomUUID());
        createdProduct.setCode("PROD002");
        createdProduct.setName("New Product");
        createdProduct.setPrice(new BigDecimal("149.99"));
        createdProduct.setActive(true);

        when(productService.createProduct(any(ProductDTO.class))).thenReturn(createdProduct);

        // When
        ResponseEntity<ProductDTO> response = productController.createProduct(createDTO);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getCode()).isEqualTo("PROD002");
        assertThat(response.getBody().getName()).isEqualTo("New Product");
        verify(productService).createProduct(createDTO);
    }

    @Test
    void testCreateProduct_WithDuplicateCode_ShouldThrowException() {
        // Given
        ProductDTO createDTO = new ProductDTO();
        createDTO.setCode("PROD001");
        createDTO.setName("Duplicate Product");

        when(productService.createProduct(any(ProductDTO.class)))
                .thenThrow(new ApplicationException("Product with code PROD001 already exists", "PRODUCT_CODE_EXISTS"));

        // When & Then
        assertThatThrownBy(() -> productController.createProduct(createDTO))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Product with code PROD001 already exists");
        verify(productService).createProduct(createDTO);
    }

    @Test
    void testUpdateProduct_WithValidData_ShouldReturnUpdatedProduct() {
        // Given
        ProductDTO updateDTO = new ProductDTO();
        updateDTO.setCode("PROD001");
        updateDTO.setName("Updated Product");
        updateDTO.setPrice(new BigDecimal("199.99"));

        ProductDTO updatedProduct = new ProductDTO();
        updatedProduct.setUuid(productUuid);
        updatedProduct.setCode("PROD001");
        updatedProduct.setName("Updated Product");
        updatedProduct.setPrice(new BigDecimal("199.99"));

        when(productService.updateProduct(eq(productUuid), any(ProductDTO.class))).thenReturn(updatedProduct);

        // When
        ResponseEntity<ProductDTO> response = productController.updateProduct(productUuid, updateDTO);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Updated Product");
        assertThat(response.getBody().getPrice()).isEqualTo(new BigDecimal("199.99"));
        verify(productService).updateProduct(productUuid, updateDTO);
    }

    @Test
    void testDeleteProduct_WhenProductExists_ShouldReturn204() {
        // When
        ResponseEntity<Void> response = productController.deleteProduct(productUuid);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(productService).deleteProduct(productUuid);
    }

    @Test
    void testDeleteProduct_WhenProductNotFound_ShouldThrowException() {
        // Given
        doThrow(new ApplicationException("Product not found with id: " + productUuid, "PRODUCT_NOT_FOUND"))
                .when(productService).deleteProduct(productUuid);

        // When & Then
        assertThatThrownBy(() -> productController.deleteProduct(productUuid))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Product not found with id: " + productUuid);
        verify(productService).deleteProduct(productUuid);
    }

    @Test
    void testActivateProduct_WhenProductExists_ShouldReturnActivatedProduct() {
        // Given
        testProductDTO.setActive(true);
        when(productService.activateProduct(productUuid)).thenReturn(testProductDTO);

        // When
        ResponseEntity<ProductDTO> response = productController.activateProduct(productUuid);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isActive()).isTrue();
        verify(productService).activateProduct(productUuid);
    }

    @Test
    void testDeactivateProduct_WhenProductExists_ShouldReturnDeactivatedProduct() {
        // Given
        testProductDTO.setActive(false);
        when(productService.deactivateProduct(productUuid)).thenReturn(testProductDTO);

        // When
        ResponseEntity<ProductDTO> response = productController.deactivateProduct(productUuid);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isActive()).isFalse();
        verify(productService).deactivateProduct(productUuid);
    }

    @Test
    void testGetProductsByCategory_ShouldReturnProductsInCategory() {
        // Given
        List<ProductDTO> products = Arrays.asList(testProductDTO);
        when(productService.getProductsByCategory("Electronics")).thenReturn(products);

        // When
        ResponseEntity<List<ProductDTO>> response = productController.getProductsByCategory("Electronics");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getCategory()).isEqualTo("Electronics");
        verify(productService).getProductsByCategory("Electronics");
    }

    @Test
    void testGetProductsByBrand_ShouldReturnProductsOfBrand() {
        // Given
        List<ProductDTO> products = Arrays.asList(testProductDTO);
        when(productService.getProductsByBrand("TestBrand")).thenReturn(products);

        // When
        ResponseEntity<List<ProductDTO>> response = productController.getProductsByBrand("TestBrand");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getBrand()).isEqualTo("TestBrand");
        verify(productService).getProductsByBrand("TestBrand");
    }

    @Test
    void testSearchProductsByName_ShouldReturnMatchingProducts() {
        // Given
        List<ProductDTO> products = Arrays.asList(testProductDTO);
        when(productService.searchProductsByName("Test")).thenReturn(products);

        // When
        ResponseEntity<List<ProductDTO>> response = productController.searchProductsByName("Test");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getName()).contains("Test");
        verify(productService).searchProductsByName("Test");
    }
}