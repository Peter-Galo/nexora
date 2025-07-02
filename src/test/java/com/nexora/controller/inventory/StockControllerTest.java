package com.nexora.controller.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexora.dto.inventory.ProductDTO;
import com.nexora.dto.inventory.StockDTO;
import com.nexora.dto.inventory.WarehouseDTO;
import com.nexora.exception.ApplicationException;
import com.nexora.exception.GlobalExceptionHandler;
import com.nexora.service.inventory.StockService;
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
class StockControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StockService stockService;

    @InjectMocks
    private StockController stockController;

    private ObjectMapper objectMapper;
    private UUID testStockUuid;
    private UUID testProductUuid;
    private UUID testWarehouseUuid;
    private StockDTO testStockDTO;
    private List<StockDTO> testStockDTOList;
    private ProductDTO testProductDTO;
    private WarehouseDTO testWarehouseDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(stockController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        testProductUuid = UUID.randomUUID();
        testWarehouseUuid = UUID.randomUUID();
        testStockUuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        testProductDTO = buildProductDTO(testProductUuid, "P001", "Test Product");
        testWarehouseDTO = buildWarehouseDTO(testWarehouseUuid, "WH001", "Test Warehouse");
        testStockDTO = buildStockDTO(testStockUuid, testProductDTO, testWarehouseDTO, 100);

        testStockDTOList = Arrays.asList(
                testStockDTO,
                buildStockDTO(
                        UUID.randomUUID(),
                        buildProductDTO(UUID.randomUUID(), "P002", "Another Product"),
                        buildWarehouseDTO(UUID.randomUUID(), "WH002", "Another Warehouse"),
                        50
                )
        );
    }

    private ProductDTO buildProductDTO(UUID uuid, String code, String name) {
        LocalDateTime now = LocalDateTime.now();
        return new ProductDTO(uuid, code, name, "desc", new BigDecimal("99.99"), now, now, true, "Cat", "Brand", "SKU");
    }

    private WarehouseDTO buildWarehouseDTO(UUID uuid, String code, String name) {
        LocalDateTime now = LocalDateTime.now();
        return new WarehouseDTO(uuid, code, name, "desc", "addr", "city", "state", "zip", "country", now, now, true);
    }

    private StockDTO buildStockDTO(UUID uuid, ProductDTO product, WarehouseDTO warehouse, int quantity) {
        LocalDateTime now = LocalDateTime.now();
        return new StockDTO(uuid, product, warehouse, quantity, 10, 200, now, now, now);
    }

    private void assertBadRequest(org.springframework.test.web.servlet.RequestBuilder request, String errorCode, String messagePart) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(errorCode)))
                .andExpect(jsonPath("$.messages[0]", containsString(messagePart)));
    }

    @Test
    void getAllStocks_shouldReturnAllStocks() throws Exception {
        when(stockService.getAllStocks()).thenReturn(testStockDTOList);

        mockMvc.perform(get("/api/v1/inventory/stocks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].uuid", is(testStockDTO.getUuid().toString())))
                .andExpect(jsonPath("$[0].quantity", is(testStockDTO.getQuantity())))
                .andExpect(jsonPath("$[0].product.code", is(testProductDTO.getCode())))
                .andExpect(jsonPath("$[0].warehouse.code", is(testWarehouseDTO.getCode())));

        verify(stockService).getAllStocks();
    }

    @Test
    void getStockById_withExistingId_shouldReturnStock() throws Exception {
        when(stockService.getStockById(testStockUuid)).thenReturn(testStockDTO);

        mockMvc.perform(get("/api/v1/inventory/stocks/{id}", testStockUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid", is(testStockDTO.getUuid().toString())))
                .andExpect(jsonPath("$.quantity", is(testStockDTO.getQuantity())))
                .andExpect(jsonPath("$.product.code", is(testProductDTO.getCode())))
                .andExpect(jsonPath("$.warehouse.code", is(testWarehouseDTO.getCode())));

        verify(stockService).getStockById(testStockUuid);
    }

    @Test
    void getStockById_withNonExistingId_shouldReturnBadRequest() throws Exception {
        when(stockService.getStockById(testStockUuid))
                .thenThrow(new ApplicationException("Stock not found with id: " + testStockUuid, "STOCK_NOT_FOUND"));

        assertBadRequest(get("/api/v1/inventory/stocks/{id}", testStockUuid), "STOCK_NOT_FOUND", "Stock not found");
        verify(stockService).getStockById(testStockUuid);
    }

    @Test
    void getStocksByProductId_shouldReturnStocksForProduct() throws Exception {
        when(stockService.getStocksByProductId(testProductUuid)).thenReturn(Collections.singletonList(testStockDTO));

        mockMvc.perform(get("/api/v1/inventory/stocks/product/{productId}", testProductUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].uuid", is(testStockDTO.getUuid().toString())))
                .andExpect(jsonPath("$[0].product.uuid", is(testProductDTO.getUuid().toString())));

        verify(stockService).getStocksByProductId(testProductUuid);
    }

    @Test
    void getStocksByProductCode_shouldReturnStocksForProductCode() throws Exception {
        String productCode = "P001";
        when(stockService.getStocksByProductCode(productCode)).thenReturn(Collections.singletonList(testStockDTO));

        mockMvc.perform(get("/api/v1/inventory/stocks/product/code/{productCode}", productCode))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].uuid", is(testStockDTO.getUuid().toString())))
                .andExpect(jsonPath("$[0].product.code", is(productCode)));

        verify(stockService).getStocksByProductCode(productCode);
    }

    @Test
    void getStocksByWarehouseId_shouldReturnStocksForWarehouse() throws Exception {
        when(stockService.getStocksByWarehouseId(testWarehouseUuid)).thenReturn(Collections.singletonList(testStockDTO));

        mockMvc.perform(get("/api/v1/inventory/stocks/warehouse/{warehouseId}", testWarehouseUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].uuid", is(testStockDTO.getUuid().toString())))
                .andExpect(jsonPath("$[0].warehouse.uuid", is(testWarehouseDTO.getUuid().toString())));

        verify(stockService).getStocksByWarehouseId(testWarehouseUuid);
    }

    @Test
    void getStocksByWarehouseCode_shouldReturnStocksForWarehouseCode() throws Exception {
        String warehouseCode = "WH001";
        when(stockService.getStocksByWarehouseCode(warehouseCode)).thenReturn(Collections.singletonList(testStockDTO));

        mockMvc.perform(get("/api/v1/inventory/stocks/warehouse/code/{warehouseCode}", warehouseCode))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].uuid", is(testStockDTO.getUuid().toString())))
                .andExpect(jsonPath("$[0].warehouse.code", is(warehouseCode)));

        verify(stockService).getStocksByWarehouseCode(warehouseCode);
    }

    @Test
    void getStockByProductAndWarehouse_shouldReturnStock() throws Exception {
        when(stockService.getStockByProductAndWarehouse(testProductUuid, testWarehouseUuid)).thenReturn(testStockDTO);

        mockMvc.perform(get("/api/v1/inventory/stocks/product/{productId}/warehouse/{warehouseId}",
                        testProductUuid, testWarehouseUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid", is(testStockDTO.getUuid().toString())))
                .andExpect(jsonPath("$.product.uuid", is(testProductDTO.getUuid().toString())))
                .andExpect(jsonPath("$.warehouse.uuid", is(testWarehouseDTO.getUuid().toString())));

        verify(stockService).getStockByProductAndWarehouse(testProductUuid, testWarehouseUuid);
    }

    @Test
    void createStock_withValidData_shouldCreateAndReturnStock() throws Exception {
        StockDTO newStockDTO = new StockDTO(testProductDTO, testWarehouseDTO, 50);
        when(stockService.createStock(any(StockDTO.class))).thenReturn(testStockDTO);

        mockMvc.perform(post("/api/v1/inventory/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStockDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid", is(testStockDTO.getUuid().toString())))
                .andExpect(jsonPath("$.quantity", is(testStockDTO.getQuantity())));

        verify(stockService).createStock(any(StockDTO.class));
    }

    @Test
    void updateStock_withValidData_shouldUpdateAndReturnStock() throws Exception {
        StockDTO updatedStockDTO = new StockDTO(testProductDTO, testWarehouseDTO, 75);
        when(stockService.updateStock(eq(testStockUuid), any(StockDTO.class))).thenReturn(testStockDTO);

        mockMvc.perform(put("/api/v1/inventory/stocks/{id}", testStockUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedStockDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid", is(testStockDTO.getUuid().toString())))
                .andExpect(jsonPath("$.quantity", is(testStockDTO.getQuantity())));

        verify(stockService).updateStock(eq(testStockUuid), any(StockDTO.class));
    }

    @Test
    void deleteStock_withExistingId_shouldReturnNoContent() throws Exception {
        doNothing().when(stockService).deleteStock(testStockUuid);

        mockMvc.perform(delete("/api/v1/inventory/stocks/{id}", testStockUuid))
                .andExpect(status().isNoContent());

        verify(stockService).deleteStock(testStockUuid);
    }

    @Test
    void addStock_withValidData_shouldAddStockAndReturnUpdatedStock() throws Exception {
        int quantityToAdd = 25;
        when(stockService.addStock(testStockUuid, quantityToAdd)).thenReturn(testStockDTO);

        mockMvc.perform(put("/api/v1/inventory/stocks/{id}/add", testStockUuid)
                        .param("quantity", String.valueOf(quantityToAdd)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid", is(testStockDTO.getUuid().toString())))
                .andExpect(jsonPath("$.quantity", is(testStockDTO.getQuantity())));

        verify(stockService).addStock(testStockUuid, quantityToAdd);
    }

    @Test
    void removeStock_withValidData_shouldRemoveStockAndReturnUpdatedStock() throws Exception {
        int quantityToRemove = 25;
        when(stockService.removeStock(testStockUuid, quantityToRemove)).thenReturn(testStockDTO);

        mockMvc.perform(put("/api/v1/inventory/stocks/{id}/remove", testStockUuid)
                        .param("quantity", String.valueOf(quantityToRemove)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid", is(testStockDTO.getUuid().toString())))
                .andExpect(jsonPath("$.quantity", is(testStockDTO.getQuantity())));

        verify(stockService).removeStock(testStockUuid, quantityToRemove);
    }

    @Test
    void getLowStocks_shouldReturnLowStocks() throws Exception {
        when(stockService.getLowStocks()).thenReturn(testStockDTOList);

        mockMvc.perform(get("/api/v1/inventory/stocks/low"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));

        verify(stockService).getLowStocks();
    }

    @Test
    void getOverStocks_shouldReturnOverStocks() throws Exception {
        when(stockService.getOverStocks()).thenReturn(testStockDTOList);

        mockMvc.perform(get("/api/v1/inventory/stocks/over"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));

        verify(stockService).getOverStocks();
    }

    @Test
    void getZeroStocks_shouldReturnZeroStocks() throws Exception {
        when(stockService.getZeroStocks()).thenReturn(testStockDTOList);

        mockMvc.perform(get("/api/v1/inventory/stocks/zero"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));

        verify(stockService).getZeroStocks();
    }
}