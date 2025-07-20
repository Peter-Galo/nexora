package com.nexora.service.inventory;

import com.nexora.dto.inventory.ProductDTO;
import com.nexora.exception.ApplicationException;
import com.nexora.model.inventory.Product;
import com.nexora.repository.inventory.ProductRepository;
import com.nexora.service.inventory.impl.ProductServiceImpl;
import com.nexora.util.EntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct1;
    private Product testProduct2;
    private ProductDTO testProductDTO1;
    private ProductDTO testProductDTO2;
    private UUID productUuid1;
    private UUID productUuid2;

    @BeforeEach
    void setUp() {
        productUuid1 = UUID.randomUUID();
        productUuid2 = UUID.randomUUID();

        // Create test products
        testProduct1 = new Product();
        testProduct1.setUuid(productUuid1);
        testProduct1.setCode("PROD001");
        testProduct1.setName("Test Product 1");
        testProduct1.setDescription("Description for test product 1");
        testProduct1.setPrice(new BigDecimal("99.99"));
        testProduct1.setActive(true);
        testProduct1.setCategory("Electronics");
        testProduct1.setBrand("TestBrand");
        testProduct1.setSku("SKU001");
        testProduct1.setCreatedAt(LocalDateTime.now());
        testProduct1.setUpdatedAt(LocalDateTime.now());

        testProduct2 = new Product();
        testProduct2.setUuid(productUuid2);
        testProduct2.setCode("PROD002");
        testProduct2.setName("Test Product 2");
        testProduct2.setDescription("Description for test product 2");
        testProduct2.setPrice(new BigDecimal("149.99"));
        testProduct2.setActive(false);
        testProduct2.setCategory("Clothing");
        testProduct2.setBrand("AnotherBrand");
        testProduct2.setSku("SKU002");
        testProduct2.setCreatedAt(LocalDateTime.now());
        testProduct2.setUpdatedAt(LocalDateTime.now());

        // Create test DTOs
        testProductDTO1 = new ProductDTO();
        testProductDTO1.setUuid(productUuid1);
        testProductDTO1.setCode("PROD001");
        testProductDTO1.setName("Test Product 1");
        testProductDTO1.setDescription("Description for test product 1");
        testProductDTO1.setPrice(new BigDecimal("99.99"));
        testProductDTO1.setActive(true);
        testProductDTO1.setCategory("Electronics");
        testProductDTO1.setBrand("TestBrand");
        testProductDTO1.setSku("SKU001");

        testProductDTO2 = new ProductDTO();
        testProductDTO2.setUuid(productUuid2);
        testProductDTO2.setCode("PROD002");
        testProductDTO2.setName("Test Product 2");
        testProductDTO2.setDescription("Description for test product 2");
        testProductDTO2.setPrice(new BigDecimal("149.99"));
        testProductDTO2.setActive(false);
        testProductDTO2.setCategory("Clothing");
        testProductDTO2.setBrand("AnotherBrand");
        testProductDTO2.setSku("SKU002");
    }

    @Test
    void testGetAllProducts_ShouldReturnAllProducts() {
        // Given
        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct1, testProduct2));
        when(entityMapper.mapToDTO(testProduct1, ProductDTO.class)).thenReturn(testProductDTO1);
        when(entityMapper.mapToDTO(testProduct2, ProductDTO.class)).thenReturn(testProductDTO2);

        // When
        List<ProductDTO> result = productService.getAllProducts();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUuid()).isEqualTo(productUuid1);
        assertThat(result.get(1).getUuid()).isEqualTo(productUuid2);
        verify(productRepository).findAll();
        verify(entityMapper).mapToDTO(testProduct1, ProductDTO.class);
        verify(entityMapper).mapToDTO(testProduct2, ProductDTO.class);
    }

    @Test
    void testGetActiveProducts_ShouldReturnOnlyActiveProducts() {
        // Given
        when(productRepository.findByActiveTrue()).thenReturn(Arrays.asList(testProduct1));
        when(entityMapper.mapToDTO(testProduct1, ProductDTO.class)).thenReturn(testProductDTO1);

        // When
        List<ProductDTO> result = productService.getActiveProducts();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUuid()).isEqualTo(productUuid1);
        assertThat(result.get(0).isActive()).isTrue();
        verify(productRepository).findByActiveTrue();
        verify(entityMapper).mapToDTO(testProduct1, ProductDTO.class);
    }

    @Test
    void testGetProductById_WhenProductExists_ShouldReturnProduct() {
        // Given
        when(productRepository.findById(productUuid1)).thenReturn(Optional.of(testProduct1));
        when(entityMapper.mapToDTO(testProduct1, ProductDTO.class)).thenReturn(testProductDTO1);

        // When
        ProductDTO result = productService.getProductById(productUuid1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isEqualTo(productUuid1);
        assertThat(result.getCode()).isEqualTo("PROD001");
        verify(productRepository).findById(productUuid1);
        verify(entityMapper).mapToDTO(testProduct1, ProductDTO.class);
    }

    @Test
    void testGetProductById_WhenProductDoesNotExist_ShouldThrowException() {
        // Given
        when(productRepository.findById(productUuid1)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.getProductById(productUuid1))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Product not found with id: " + productUuid1);
        verify(productRepository).findById(productUuid1);
        verify(entityMapper, never()).mapToDTO(any(Product.class), eq(ProductDTO.class));
    }

    @Test
    void testGetProductByCode_WhenProductExists_ShouldReturnProduct() {
        // Given
        when(productRepository.findByCode("PROD001")).thenReturn(Optional.of(testProduct1));
        when(entityMapper.mapToDTO(testProduct1, ProductDTO.class)).thenReturn(testProductDTO1);

        // When
        ProductDTO result = productService.getProductByCode("PROD001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("PROD001");
        verify(productRepository).findByCode("PROD001");
        verify(entityMapper).mapToDTO(testProduct1, ProductDTO.class);
    }

    @Test
    void testGetProductByCode_WhenProductDoesNotExist_ShouldThrowException() {
        // Given
        when(productRepository.findByCode("NONEXISTENT")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.getProductByCode("NONEXISTENT"))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Product not found with code: NONEXISTENT");
        verify(productRepository).findByCode("NONEXISTENT");
        verify(entityMapper, never()).mapToDTO(any(Product.class), eq(ProductDTO.class));
    }

    @Test
    void testCreateProduct_WhenProductCodeDoesNotExist_ShouldCreateProduct() {
        // Given
        when(productRepository.existsByCode("PROD001")).thenReturn(false);
        when(entityMapper.mapToEntity(testProductDTO1, Product.class)).thenReturn(testProduct1);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setUuid(productUuid1);
            return product;
        });
        when(entityMapper.mapToDTO(any(Product.class), eq(ProductDTO.class))).thenReturn(testProductDTO1);

        // When
        ProductDTO result = productService.createProduct(testProductDTO1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isEqualTo(productUuid1);
        verify(productRepository).existsByCode("PROD001");
        verify(entityMapper).mapToEntity(testProductDTO1, Product.class);
        verify(productRepository).save(any(Product.class));
        verify(entityMapper).mapToDTO(any(Product.class), eq(ProductDTO.class));
    }

    @Test
    void testCreateProduct_WhenProductCodeAlreadyExists_ShouldThrowException() {
        // Given
        when(productRepository.existsByCode("PROD001")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> productService.createProduct(testProductDTO1))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Product with code PROD001 already exists");
        verify(productRepository).existsByCode("PROD001");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_WhenProductExists_ShouldUpdateProduct() {
        // Given
        when(productRepository.findById(productUuid1)).thenReturn(Optional.of(testProduct1));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(entityMapper.mapToDTO(any(Product.class), eq(ProductDTO.class))).thenReturn(testProductDTO1);

        ProductDTO updateDTO = new ProductDTO();
        updateDTO.setCode("PROD001");
        updateDTO.setName("Updated Product Name");
        updateDTO.setPrice(new BigDecimal("199.99"));

        // When
        ProductDTO result = productService.updateProduct(productUuid1, updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(productRepository).findById(productUuid1);
        verify(entityMapper).mapToExistingEntity(updateDTO, testProduct1);
        verify(productRepository).save(any(Product.class));
        verify(entityMapper).mapToDTO(any(Product.class), eq(ProductDTO.class));
    }

    @Test
    void testUpdateProduct_WhenProductDoesNotExist_ShouldThrowException() {
        // Given
        when(productRepository.findById(productUuid1)).thenReturn(Optional.empty());

        ProductDTO updateDTO = new ProductDTO();
        updateDTO.setCode("PROD001");

        // When & Then
        assertThatThrownBy(() -> productService.updateProduct(productUuid1, updateDTO))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Product not found with id: " + productUuid1);
        verify(productRepository).findById(productUuid1);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testDeleteProduct_WhenProductExists_ShouldDeleteProduct() {
        // Given
        when(productRepository.existsById(productUuid1)).thenReturn(true);

        // When
        productService.deleteProduct(productUuid1);

        // Then
        verify(productRepository).existsById(productUuid1);
        verify(productRepository).deleteById(productUuid1);
    }

    @Test
    void testDeleteProduct_WhenProductDoesNotExist_ShouldThrowException() {
        // Given
        when(productRepository.existsById(productUuid1)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> productService.deleteProduct(productUuid1))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Product not found with id: " + productUuid1);
        verify(productRepository).existsById(productUuid1);
        verify(productRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void testActivateProduct_WhenProductExists_ShouldActivateProduct() {
        // Given
        testProduct1.setActive(false);
        when(productRepository.findById(productUuid1)).thenReturn(Optional.of(testProduct1));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(entityMapper.mapToDTO(any(Product.class), eq(ProductDTO.class))).thenReturn(testProductDTO1);

        // When
        ProductDTO result = productService.activateProduct(productUuid1);

        // Then
        assertThat(result).isNotNull();
        verify(productRepository).findById(productUuid1);
        verify(productRepository).save(any(Product.class));
        verify(entityMapper).mapToDTO(any(Product.class), eq(ProductDTO.class));
    }

    @Test
    void testDeactivateProduct_WhenProductExists_ShouldDeactivateProduct() {
        // Given
        when(productRepository.findById(productUuid1)).thenReturn(Optional.of(testProduct1));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(entityMapper.mapToDTO(any(Product.class), eq(ProductDTO.class))).thenReturn(testProductDTO1);

        // When
        ProductDTO result = productService.deactivateProduct(productUuid1);

        // Then
        assertThat(result).isNotNull();
        verify(productRepository).findById(productUuid1);
        verify(productRepository).save(any(Product.class));
        verify(entityMapper).mapToDTO(any(Product.class), eq(ProductDTO.class));
    }

    @Test
    void testGetProductsByCategory_ShouldReturnProductsInCategory() {
        // Given
        when(productRepository.findByCategory("Electronics")).thenReturn(Arrays.asList(testProduct1));
        when(entityMapper.mapToDTO(testProduct1, ProductDTO.class)).thenReturn(testProductDTO1);

        // When
        List<ProductDTO> result = productService.getProductsByCategory("Electronics");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("Electronics");
        verify(productRepository).findByCategory("Electronics");
        verify(entityMapper).mapToDTO(testProduct1, ProductDTO.class);
    }

    @Test
    void testGetProductsByBrand_ShouldReturnProductsOfBrand() {
        // Given
        when(productRepository.findByBrand("TestBrand")).thenReturn(Arrays.asList(testProduct1));
        when(entityMapper.mapToDTO(testProduct1, ProductDTO.class)).thenReturn(testProductDTO1);

        // When
        List<ProductDTO> result = productService.getProductsByBrand("TestBrand");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBrand()).isEqualTo("TestBrand");
        verify(productRepository).findByBrand("TestBrand");
        verify(entityMapper).mapToDTO(testProduct1, ProductDTO.class);
    }

    @Test
    void testSearchProductsByName_ShouldReturnMatchingProducts() {
        // Given
        when(productRepository.findByNameContainingIgnoreCase("Test")).thenReturn(Arrays.asList(testProduct1, testProduct2));
        when(entityMapper.mapToDTO(testProduct1, ProductDTO.class)).thenReturn(testProductDTO1);
        when(entityMapper.mapToDTO(testProduct2, ProductDTO.class)).thenReturn(testProductDTO2);

        // When
        List<ProductDTO> result = productService.searchProductsByName("Test");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).contains("Test");
        assertThat(result.get(1).getName()).contains("Test");
        verify(productRepository).findByNameContainingIgnoreCase("Test");
        verify(entityMapper).mapToDTO(testProduct1, ProductDTO.class);
        verify(entityMapper).mapToDTO(testProduct2, ProductDTO.class);
    }

    @Test
    void testGetAllProducts_WhenNoProducts_ShouldReturnEmptyList() {
        // Given
        when(productRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<ProductDTO> result = productService.getAllProducts();

        // Then
        assertThat(result).isEmpty();
        verify(productRepository).findAll();
        verify(entityMapper, never()).mapToDTO(any(Product.class), eq(ProductDTO.class));
    }
}
