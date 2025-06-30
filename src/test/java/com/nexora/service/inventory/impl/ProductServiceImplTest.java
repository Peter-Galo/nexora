package com.nexora.service.inventory.impl;

import com.nexora.dto.inventory.ProductDTO;
import com.nexora.exception.ApplicationException;
import com.nexora.model.inventory.Product;
import com.nexora.repository.inventory.ProductRepository;
import com.nexora.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the ProductServiceImpl class.
 */
@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product1;
    private Product product2;
    private ProductDTO productDTO;

    @BeforeEach
    public void setup() {
        // Create test products
        product1 = TestDataFactory.createSampleProduct(1L);
        product2 = TestDataFactory.createSampleProduct(2L);
        productDTO = TestDataFactory.createSampleProductDTO(null);
    }

    @Test
    public void testGetAllProducts() {
        // Arrange
        when(productRepository.findAll()).thenReturn(List.of(product1, product2));

        // Act
        List<ProductDTO> result = productService.getAllProducts();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(product1.getId());
        assertThat(result.get(1).getId()).isEqualTo(product2.getId());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    public void testGetActiveProducts() {
        // Arrange
        when(productRepository.findByActiveTrue()).thenReturn(List.of(product1, product2));

        // Act
        List<ProductDTO> result = productService.getActiveProducts();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(product1.getId());
        assertThat(result.get(1).getId()).isEqualTo(product2.getId());
        verify(productRepository, times(1)).findByActiveTrue();
    }

    @Test
    public void testGetProductById_Success() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product1));

        // Act
        ProductDTO result = productService.getProductById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(product1.getId());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetProductById_NotFound() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApplicationException.class, () -> productService.getProductById(1L));
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetProductByCode_Success() {
        // Arrange
        when(productRepository.findByCode(anyString())).thenReturn(Optional.of(product1));

        // Act
        ProductDTO result = productService.getProductByCode("TEST-PROD-1");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(product1.getId());
        verify(productRepository, times(1)).findByCode("TEST-PROD-1");
    }

    @Test
    public void testGetProductByCode_NotFound() {
        // Arrange
        when(productRepository.findByCode(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApplicationException.class, () -> productService.getProductByCode("TEST-PROD-1"));
        verify(productRepository, times(1)).findByCode("TEST-PROD-1");
    }

    @Test
    public void testCreateProduct_Success() {
        // Arrange
        when(productRepository.existsByCode(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product1);

        // Act
        ProductDTO result = productService.createProduct(productDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(product1.getId());
        verify(productRepository, times(1)).existsByCode(productDTO.getCode());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testCreateProduct_CodeExists() {
        // Arrange
        when(productRepository.existsByCode(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(ApplicationException.class, () -> productService.createProduct(productDTO));
        verify(productRepository, times(1)).existsByCode(productDTO.getCode());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    public void testUpdateProduct_Success() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product1));
        when(productRepository.save(any(Product.class))).thenReturn(product1);

        // Create a DTO with updated values
        ProductDTO updateDTO = new ProductDTO();
        updateDTO.setId(1L);
        updateDTO.setCode("TEST-PROD-1");
        updateDTO.setName("Updated Name");
        updateDTO.setPrice(new BigDecimal("149.99"));

        // Act
        ProductDTO result = productService.updateProduct(1L, updateDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(product1.getId());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testUpdateProduct_NotFound() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApplicationException.class, () -> productService.updateProduct(1L, productDTO));
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    public void testUpdateProduct_CodeExists() {
        // Arrange
        Product existingProduct = TestDataFactory.createSampleProduct(1L);
        existingProduct.setCode("EXISTING-CODE");
        
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsByCode(anyString())).thenReturn(true);

        // Create a DTO with a different code
        ProductDTO updateDTO = new ProductDTO();
        updateDTO.setId(1L);
        updateDTO.setCode("NEW-CODE");
        updateDTO.setName("Updated Name");

        // Act & Assert
        assertThrows(ApplicationException.class, () -> productService.updateProduct(1L, updateDTO));
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).existsByCode("NEW-CODE");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    public void testDeleteProduct_Success() {
        // Arrange
        when(productRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(productRepository).deleteById(anyLong());

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteProduct_NotFound() {
        // Arrange
        when(productRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        assertThrows(ApplicationException.class, () -> productService.deleteProduct(1L));
        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testDeactivateProduct_Success() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product1));
        when(productRepository.save(any(Product.class))).thenReturn(product1);

        // Act
        ProductDTO result = productService.deactivateProduct(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isActive()).isFalse();
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testDeactivateProduct_NotFound() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApplicationException.class, () -> productService.deactivateProduct(1L));
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    public void testActivateProduct_Success() {
        // Arrange
        product1.setActive(false);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product1));
        when(productRepository.save(any(Product.class))).thenReturn(product1);

        // Act
        ProductDTO result = productService.activateProduct(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isActive()).isTrue();
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testActivateProduct_NotFound() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApplicationException.class, () -> productService.activateProduct(1L));
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    public void testGetProductsByCategory() {
        // Arrange
        when(productRepository.findByCategory(anyString())).thenReturn(List.of(product1, product2));

        // Act
        List<ProductDTO> result = productService.getProductsByCategory("Test Category");

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(product1.getId());
        assertThat(result.get(1).getId()).isEqualTo(product2.getId());
        verify(productRepository, times(1)).findByCategory("Test Category");
    }

    @Test
    public void testGetProductsByBrand() {
        // Arrange
        when(productRepository.findByBrand(anyString())).thenReturn(List.of(product1, product2));

        // Act
        List<ProductDTO> result = productService.getProductsByBrand("Test Brand");

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(product1.getId());
        assertThat(result.get(1).getId()).isEqualTo(product2.getId());
        verify(productRepository, times(1)).findByBrand("Test Brand");
    }

    @Test
    public void testSearchProductsByName() {
        // Arrange
        when(productRepository.findByNameContainingIgnoreCase(anyString())).thenReturn(List.of(product1, product2));

        // Act
        List<ProductDTO> result = productService.searchProductsByName("Test");

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(product1.getId());
        assertThat(result.get(1).getId()).isEqualTo(product2.getId());
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("Test");
    }
}