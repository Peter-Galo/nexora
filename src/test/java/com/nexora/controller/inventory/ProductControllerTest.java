package com.nexora.controller.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexora.dto.inventory.ProductDTO;
import com.nexora.exception.ApplicationException;
import com.nexora.exception.GlobalExceptionHandler;
import com.nexora.service.inventory.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private UUID testUuid;
    private ProductDTO testProductDTO;
    private List<ProductDTO> testProductDTOList;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper().findAndRegisterModules();

        now = LocalDateTime.now();
        testUuid = UUID.randomUUID();
        testProductDTO = buildProductDTO(testUuid, "P001", "Test Product", "Test description", new BigDecimal("99.99"), true, "Test Category", "Test Brand", "SKU001");
        testProductDTOList = Arrays.asList(
                testProductDTO,
                buildProductDTO(UUID.randomUUID(), "P002", "Another Product", "Another description", new BigDecimal("149.99"), true, "Another Category", "Another Brand", "SKU002")
        );
    }

    private ProductDTO buildProductDTO(UUID uuid, String code, String name, String desc, BigDecimal price, boolean active, String category, String brand, String sku) {
        return new ProductDTO(uuid, code, name, desc, price, now, now, active, category, brand, sku);
    }

    @Test
    void getAllProducts_shouldReturnAllProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(testProductDTOList);

        mockMvc.perform(get("/api/v1/inventory/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].uuid", is(testProductDTO.getUuid().toString())))
                .andExpect(jsonPath("$[0].code", is(testProductDTO.getCode())))
                .andExpect(jsonPath("$[0].name", is(testProductDTO.getName())))
                .andExpect(jsonPath("$[1].code", is("P002")));

        verify(productService).getAllProducts();
    }

    @Test
    void getActiveProducts_shouldReturnActiveProducts() throws Exception {
        when(productService.getActiveProducts()).thenReturn(testProductDTOList);

        mockMvc.perform(get("/api/v1/inventory/products/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].active", is(true)))
                .andExpect(jsonPath("$[1].active", is(true)));

        verify(productService).getActiveProducts();
    }

    @Test
    void getProductById_withExistingId_shouldReturnProduct() throws Exception {
        when(productService.getProductById(testUuid)).thenReturn(testProductDTO);

        mockMvc.perform(get("/api/v1/inventory/products/{id}", testUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid", is(testProductDTO.getUuid().toString())))
                .andExpect(jsonPath("$.code", is(testProductDTO.getCode())))
                .andExpect(jsonPath("$.name", is(testProductDTO.getName())));

        verify(productService).getProductById(testUuid);
    }

    @Test
    void getProductById_withNonExistingId_shouldReturnBadRequest() throws Exception {
        when(productService.getProductById(testUuid))
                .thenThrow(new ApplicationException("Product not found with id: " + testUuid, "PRODUCT_NOT_FOUND"));

        assertBadRequest(get("/api/v1/inventory/products/{id}", testUuid), "PRODUCT_NOT_FOUND", "Product not found");
        verify(productService).getProductById(testUuid);
    }

    @Test
    void getProductByCode_withExistingCode_shouldReturnProduct() throws Exception {
        String code = "P001";
        when(productService.getProductByCode(code)).thenReturn(testProductDTO);

        mockMvc.perform(get("/api/v1/inventory/products/code/{code}", code))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(code)))
                .andExpect(jsonPath("$.name", is(testProductDTO.getName())));

        verify(productService).getProductByCode(code);
    }

    @Test
    void getProductByCode_withNonExistingCode_shouldReturnBadRequest() throws Exception {
        String code = "NONEXISTENT";
        when(productService.getProductByCode(code))
                .thenThrow(new ApplicationException("Product not found with code: " + code, "PRODUCT_NOT_FOUND"));

        assertBadRequest(get("/api/v1/inventory/products/code/{code}", code), "PRODUCT_NOT_FOUND", "Product not found");
        verify(productService).getProductByCode(code);
    }

    @Test
    void createProduct_withValidData_shouldCreateAndReturnProduct() throws Exception {
        ProductDTO newProductDTO = new ProductDTO("P003", "New Product", new BigDecimal("199.99"));
        newProductDTO.setDescription("New description");
        newProductDTO.setCategory("New Category");
        newProductDTO.setBrand("New Brand");
        newProductDTO.setSku("SKU003");

        ProductDTO createdProductDTO = buildProductDTO(UUID.randomUUID(), newProductDTO.getCode(), newProductDTO.getName(), newProductDTO.getDescription(), newProductDTO.getPrice(), true, newProductDTO.getCategory(), newProductDTO.getBrand(), newProductDTO.getSku());

        when(productService.createProduct(any(ProductDTO.class))).thenReturn(createdProductDTO);

        mockMvc.perform(post("/api/v1/inventory/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProductDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid", notNullValue()))
                .andExpect(jsonPath("$.code", is(newProductDTO.getCode())))
                .andExpect(jsonPath("$.name", is(newProductDTO.getName())))
                .andExpect(jsonPath("$.price", is(newProductDTO.getPrice().doubleValue())));

        verify(productService).createProduct(any(ProductDTO.class));
    }

    @Test
    void createProduct_withExistingCode_shouldReturnBadRequest() throws Exception {
        ProductDTO newProductDTO = new ProductDTO("P001", "New Product", new BigDecimal("199.99"));

        when(productService.createProduct(any(ProductDTO.class)))
                .thenThrow(new ApplicationException("Product with code P001 already exists", "PRODUCT_CODE_EXISTS"));

        assertBadRequest(post("/api/v1/inventory/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProductDTO)),
                "PRODUCT_CODE_EXISTS", "already exists");

        verify(productService).createProduct(any(ProductDTO.class));
    }

    @Test
    void updateProduct_withValidData_shouldUpdateAndReturnProduct() throws Exception {
        ProductDTO updateDTO = buildProductDTO(testUuid, "P001", "Updated Product", "Updated description", new BigDecimal("129.99"), true, "Updated Category", "Updated Brand", "SKU001-UPD");

        when(productService.updateProduct(eq(testUuid), any(ProductDTO.class))).thenReturn(updateDTO);

        mockMvc.perform(put("/api/v1/inventory/products/{id}", testUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid", is(testUuid.toString())))
                .andExpect(jsonPath("$.name", is("Updated Product")))
                .andExpect(jsonPath("$.description", is("Updated description")))
                .andExpect(jsonPath("$.price", is(129.99)));

        verify(productService).updateProduct(eq(testUuid), any(ProductDTO.class));
    }

    @Test
    void updateProduct_withNonExistingId_shouldReturnBadRequest() throws Exception {
        ProductDTO updateDTO = buildProductDTO(testUuid, "P001", "Updated Product", "Updated description", new BigDecimal("129.99"), true, "Updated Category", "Updated Brand", "SKU001-UPD");

        when(productService.updateProduct(eq(testUuid), any(ProductDTO.class)))
                .thenThrow(new ApplicationException("Product not found with id: " + testUuid, "PRODUCT_NOT_FOUND"));

        assertBadRequest(put("/api/v1/inventory/products/{id}", testUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)),
                "PRODUCT_NOT_FOUND", "Product not found");

        verify(productService).updateProduct(eq(testUuid), any(ProductDTO.class));
    }

    @Test
    void deleteProduct_withExistingId_shouldReturnNoContent() throws Exception {
        doNothing().when(productService).deleteProduct(testUuid);

        mockMvc.perform(delete("/api/v1/inventory/products/{id}", testUuid))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(testUuid);
    }

    @Test
    void deleteProduct_withNonExistingId_shouldReturnBadRequest() throws Exception {
        doThrow(new ApplicationException("Product not found with id: " + testUuid, "PRODUCT_NOT_FOUND"))
                .when(productService).deleteProduct(testUuid);

        assertBadRequest(delete("/api/v1/inventory/products/{id}", testUuid), "PRODUCT_NOT_FOUND", "Product not found");

        verify(productService).deleteProduct(testUuid);
    }

    @Test
    void deactivateProduct_withExistingId_shouldDeactivateAndReturnProduct() throws Exception {
        ProductDTO deactivatedProductDTO = buildProductDTO(testUuid, testProductDTO.getCode(), testProductDTO.getName(), testProductDTO.getDescription(), testProductDTO.getPrice(), false, testProductDTO.getCategory(), testProductDTO.getBrand(), testProductDTO.getSku());

        when(productService.deactivateProduct(testUuid)).thenReturn(deactivatedProductDTO);

        mockMvc.perform(put("/api/v1/inventory/products/{id}/deactivate", testUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid", is(testUuid.toString())))
                .andExpect(jsonPath("$.active", is(false)));

        verify(productService).deactivateProduct(testUuid);
    }

    @Test
    void activateProduct_withExistingId_shouldActivateAndReturnProduct() throws Exception {
        ProductDTO activatedProductDTO = buildProductDTO(testUuid, testProductDTO.getCode(), testProductDTO.getName(), testProductDTO.getDescription(), testProductDTO.getPrice(), true, testProductDTO.getCategory(), testProductDTO.getBrand(), testProductDTO.getSku());

        when(productService.activateProduct(testUuid)).thenReturn(activatedProductDTO);

        mockMvc.perform(put("/api/v1/inventory/products/{id}/activate", testUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid", is(testUuid.toString())))
                .andExpect(jsonPath("$.active", is(true)));

        verify(productService).activateProduct(testUuid);
    }

    @Test
    void getProductsByCategory_shouldReturnProductsInCategory() throws Exception {
        String category = "Test Category";
        when(productService.getProductsByCategory(category)).thenReturn(Collections.singletonList(testProductDTO));

        mockMvc.perform(get("/api/v1/inventory/products/category/{category}", category))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category", is(category)));

        verify(productService).getProductsByCategory(category);
    }

    @Test
    void getProductsByBrand_shouldReturnProductsOfBrand() throws Exception {
        String brand = "Test Brand";
        when(productService.getProductsByBrand(brand)).thenReturn(Collections.singletonList(testProductDTO));

        mockMvc.perform(get("/api/v1/inventory/products/brand/{brand}", brand))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].brand", is(brand)));

        verify(productService).getProductsByBrand(brand);
    }

    @Test
    void searchProductsByName_shouldReturnProductsWithNameContainingText() throws Exception {
        String searchText = "Test";
        when(productService.searchProductsByName(searchText)).thenReturn(Collections.singletonList(testProductDTO));

        mockMvc.perform(get("/api/v1/inventory/products/search")
                        .param("name", searchText))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", containsString(searchText)));

        verify(productService).searchProductsByName(searchText);
    }

    // Helper for error assertions
    private void assertBadRequest(org.springframework.test.web.servlet.RequestBuilder request, String errorCode, String messagePart) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(errorCode)))
                .andExpect(jsonPath("$.messages[0]", containsString(messagePart)));
    }
}