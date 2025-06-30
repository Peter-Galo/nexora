package com.nexora.controller.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexora.dto.inventory.ProductDTO;
import com.nexora.exception.ApplicationException;
import com.nexora.security.TestSecurityConfig;
import com.nexora.service.inventory.ProductService;
import org.springframework.context.annotation.Import;
import com.nexora.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean; // TODO: Replace with non-deprecated alternative when available
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the ProductController class.
 */
@WebMvcTest(controllers = ProductController.class)
@Import(TestSecurityConfig.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductDTO productDTO1;
    private ProductDTO productDTO2;

    @BeforeEach
    public void setup() {
        // Create test products
        productDTO1 = TestDataFactory.createSampleProductDTO(1L);
        productDTO2 = TestDataFactory.createSampleProductDTO(2L);
    }

    @Test
    public void testGetAllProducts() throws Exception {
        // Arrange
        when(productService.getAllProducts()).thenReturn(List.of(productDTO1, productDTO2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventory/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    public void testGetActiveProducts() throws Exception {
        // Arrange
        when(productService.getActiveProducts()).thenReturn(List.of(productDTO1, productDTO2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventory/products/active")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    public void testGetProductById_Success() throws Exception {
        // Arrange
        when(productService.getProductById(anyLong())).thenReturn(productDTO1);

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventory/products/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(productDTO1.getName())));
    }

    @Test
    public void testGetProductById_NotFound() throws Exception {
        // Arrange
        when(productService.getProductById(anyLong())).thenThrow(new ApplicationException("Product not found", "PRODUCT_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventory/products/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetProductByCode_Success() throws Exception {
        // Arrange
        when(productService.getProductByCode(anyString())).thenReturn(productDTO1);

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventory/products/code/TEST-PROD-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.code", is(productDTO1.getCode())));
    }

    @Test
    public void testGetProductByCode_NotFound() throws Exception {
        // Arrange
        when(productService.getProductByCode(anyString())).thenThrow(new ApplicationException("Product not found", "PRODUCT_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventory/products/code/NONEXISTENT")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateProduct_Success() throws Exception {
        // Arrange
        ProductDTO newProductDTO = new ProductDTO();
        newProductDTO.setCode("NEW-PROD");
        newProductDTO.setName("New Product");
        newProductDTO.setPrice(new BigDecimal("99.99"));

        when(productService.createProduct(any(ProductDTO.class))).thenReturn(productDTO1);

        // Act & Assert
        mockMvc.perform(post("/api/v1/inventory/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProductDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(productDTO1.getName())));
    }

    @Test
    public void testCreateProduct_BadRequest() throws Exception {
        // Arrange
        ProductDTO invalidProductDTO = new ProductDTO();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/api/v1/inventory/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProductDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateProduct_Conflict() throws Exception {
        // Arrange
        ProductDTO existingProductDTO = new ProductDTO();
        existingProductDTO.setCode("EXISTING-CODE");
        existingProductDTO.setName("Existing Product");
        existingProductDTO.setPrice(new BigDecimal("99.99"));

        when(productService.createProduct(any(ProductDTO.class))).thenThrow(new ApplicationException("Product code already exists", "PRODUCT_CODE_EXISTS"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/inventory/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(existingProductDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateProduct_Success() throws Exception {
        // Arrange
        ProductDTO updateProductDTO = new ProductDTO();
        updateProductDTO.setCode("UPDATE-PROD");
        updateProductDTO.setName("Updated Product");
        updateProductDTO.setPrice(new BigDecimal("149.99"));

        when(productService.updateProduct(anyLong(), any(ProductDTO.class))).thenReturn(productDTO1);

        // Act & Assert
        mockMvc.perform(put("/api/v1/inventory/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProductDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(productDTO1.getName())));
    }

    @Test
    public void testUpdateProduct_NotFound() throws Exception {
        // Arrange
        ProductDTO updateProductDTO = new ProductDTO();
        updateProductDTO.setCode("UPDATE-PROD");
        updateProductDTO.setName("Updated Product");
        updateProductDTO.setPrice(new BigDecimal("149.99"));

        when(productService.updateProduct(anyLong(), any(ProductDTO.class))).thenThrow(new ApplicationException("Product not found", "PRODUCT_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/inventory/products/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProductDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteProduct_Success() throws Exception {
        // Arrange
        doNothing().when(productService).deleteProduct(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/inventory/products/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteProduct_NotFound() throws Exception {
        // Arrange
        doThrow(new ApplicationException("Product not found", "PRODUCT_NOT_FOUND")).when(productService).deleteProduct(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/inventory/products/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeactivateProduct_Success() throws Exception {
        // Arrange
        when(productService.deactivateProduct(anyLong())).thenReturn(productDTO1);

        // Act & Assert
        mockMvc.perform(put("/api/v1/inventory/products/1/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(productDTO1.getName())));
    }

    @Test
    public void testDeactivateProduct_NotFound() throws Exception {
        // Arrange
        when(productService.deactivateProduct(anyLong())).thenThrow(new ApplicationException("Product not found", "PRODUCT_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/inventory/products/999/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testActivateProduct_Success() throws Exception {
        // Arrange
        when(productService.activateProduct(anyLong())).thenReturn(productDTO1);

        // Act & Assert
        mockMvc.perform(put("/api/v1/inventory/products/1/activate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(productDTO1.getName())));
    }

    @Test
    public void testActivateProduct_NotFound() throws Exception {
        // Arrange
        when(productService.activateProduct(anyLong())).thenThrow(new ApplicationException("Product not found", "PRODUCT_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/inventory/products/999/activate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetProductsByCategory() throws Exception {
        // Arrange
        when(productService.getProductsByCategory(anyString())).thenReturn(List.of(productDTO1, productDTO2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventory/products/category/Electronics")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    public void testGetProductsByBrand() throws Exception {
        // Arrange
        when(productService.getProductsByBrand(anyString())).thenReturn(List.of(productDTO1, productDTO2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventory/products/brand/TechBrand")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    public void testSearchProductsByName() throws Exception {
        // Arrange
        when(productService.searchProductsByName(anyString())).thenReturn(List.of(productDTO1, productDTO2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventory/products/search?name=Test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }
}
