package com.nexora.controller.inventory;

import com.nexora.dto.inventory.ProductDTO;
import com.nexora.dto.inventory.StockDTO;
import com.nexora.dto.inventory.WarehouseDTO;
import com.nexora.exception.ApplicationException;
import com.nexora.service.inventory.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
class StockControllerTest {

    @Mock
    private StockService stockService;

    @InjectMocks
    private StockController stockController;

    private StockDTO testStockDTO;
    private ProductDTO testProductDTO;
    private WarehouseDTO testWarehouseDTO;
    private UUID stockUuid;
    private UUID productUuid;
    private UUID warehouseUuid;

    @BeforeEach
    void setUp() {
        stockUuid = UUID.randomUUID();
        productUuid = UUID.randomUUID();
        warehouseUuid = UUID.randomUUID();

        // Create test ProductDTO
        testProductDTO = new ProductDTO();
        testProductDTO.setUuid(productUuid);
        testProductDTO.setCode("PROD001");
        testProductDTO.setName("Test Product");
        testProductDTO.setPrice(new BigDecimal("99.99"));
        testProductDTO.setActive(true);

        // Create test WarehouseDTO
        testWarehouseDTO = new WarehouseDTO();
        testWarehouseDTO.setUuid(warehouseUuid);
        testWarehouseDTO.setCode("WH001");
        testWarehouseDTO.setName("Main Warehouse");
        testWarehouseDTO.setCity("Boston");
        testWarehouseDTO.setActive(true);

        // Create test StockDTO
        testStockDTO = new StockDTO();
        testStockDTO.setUuid(stockUuid);
        testStockDTO.setProduct(testProductDTO);
        testStockDTO.setWarehouse(testWarehouseDTO);
        testStockDTO.setQuantity(50);
        testStockDTO.setMinStockLevel(10);
        testStockDTO.setMaxStockLevel(100);
        testStockDTO.setLastRestockDate(LocalDateTime.now().minusDays(5));
    }

    @Test
    void testGetAllStocks_ShouldReturnStockList() {
        // Given
        List<StockDTO> stocks = Arrays.asList(testStockDTO);
        when(stockService.getAllStocks()).thenReturn(stocks);

        // When
        ResponseEntity<List<StockDTO>> response = stockController.getAllStocks();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getUuid()).isEqualTo(stockUuid);
        assertThat(response.getBody().get(0).getQuantity()).isEqualTo(50);
        verify(stockService).getAllStocks();
    }

    @Test
    void testGetStockById_WhenStockExists_ShouldReturnStock() {
        // Given
        when(stockService.getStockById(stockUuid)).thenReturn(testStockDTO);

        // When
        ResponseEntity<StockDTO> response = stockController.getStockById(stockUuid);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getUuid()).isEqualTo(stockUuid);
        assertThat(response.getBody().getQuantity()).isEqualTo(50);
        verify(stockService).getStockById(stockUuid);
    }

    @Test
    void testGetStockById_WhenStockNotFound_ShouldThrowException() {
        // Given
        when(stockService.getStockById(stockUuid))
                .thenThrow(new ApplicationException("Stock not found with id: " + stockUuid, "STOCK_NOT_FOUND"));

        // When & Then
        assertThatThrownBy(() -> stockController.getStockById(stockUuid))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Stock not found with id: " + stockUuid);
        verify(stockService).getStockById(stockUuid);
    }

    @Test
    void testGetStocksByProductId_ShouldReturnStockList() {
        // Given
        List<StockDTO> stocks = Arrays.asList(testStockDTO);
        when(stockService.getStocksByProductId(productUuid)).thenReturn(stocks);

        // When
        ResponseEntity<List<StockDTO>> response = stockController.getStocksByProductId(productUuid);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getProduct().getUuid()).isEqualTo(productUuid);
        verify(stockService).getStocksByProductId(productUuid);
    }

    @Test
    void testGetStocksByProductCode_ShouldReturnStockList() {
        // Given
        List<StockDTO> stocks = Arrays.asList(testStockDTO);
        when(stockService.getStocksByProductCode("PROD001")).thenReturn(stocks);

        // When
        ResponseEntity<List<StockDTO>> response = stockController.getStocksByProductCode("PROD001");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getProduct().getCode()).isEqualTo("PROD001");
        verify(stockService).getStocksByProductCode("PROD001");
    }

    @Test
    void testGetStocksByWarehouseId_ShouldReturnStockList() {
        // Given
        List<StockDTO> stocks = Arrays.asList(testStockDTO);
        when(stockService.getStocksByWarehouseId(warehouseUuid)).thenReturn(stocks);

        // When
        ResponseEntity<List<StockDTO>> response = stockController.getStocksByWarehouseId(warehouseUuid);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getWarehouse().getUuid()).isEqualTo(warehouseUuid);
        verify(stockService).getStocksByWarehouseId(warehouseUuid);
    }

    @Test
    void testGetStocksByWarehouseCode_ShouldReturnStockList() {
        // Given
        List<StockDTO> stocks = Arrays.asList(testStockDTO);
        when(stockService.getStocksByWarehouseCode("WH001")).thenReturn(stocks);

        // When
        ResponseEntity<List<StockDTO>> response = stockController.getStocksByWarehouseCode("WH001");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getWarehouse().getCode()).isEqualTo("WH001");
        verify(stockService).getStocksByWarehouseCode("WH001");
    }

    @Test
    void testGetStockByProductAndWarehouse_WhenStockExists_ShouldReturnStock() {
        // Given
        when(stockService.getStockByProductAndWarehouse(productUuid, warehouseUuid)).thenReturn(testStockDTO);

        // When
        ResponseEntity<StockDTO> response = stockController.getStockByProductAndWarehouse(productUuid, warehouseUuid);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getProduct().getUuid()).isEqualTo(productUuid);
        assertThat(response.getBody().getWarehouse().getUuid()).isEqualTo(warehouseUuid);
        verify(stockService).getStockByProductAndWarehouse(productUuid, warehouseUuid);
    }

    @Test
    void testCreateStock_WithValidData_ShouldReturnCreatedStock() {
        // Given
        StockDTO createDTO = new StockDTO();
        createDTO.setProduct(testProductDTO);
        createDTO.setWarehouse(testWarehouseDTO);
        createDTO.setQuantity(25);
        createDTO.setMinStockLevel(5);
        createDTO.setMaxStockLevel(50);

        StockDTO createdStock = new StockDTO();
        createdStock.setUuid(UUID.randomUUID());
        createdStock.setProduct(testProductDTO);
        createdStock.setWarehouse(testWarehouseDTO);
        createdStock.setQuantity(25);
        createdStock.setMinStockLevel(5);
        createdStock.setMaxStockLevel(50);

        when(stockService.createStock(any(StockDTO.class))).thenReturn(createdStock);

        // When
        ResponseEntity<StockDTO> response = stockController.createStock(createDTO);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getQuantity()).isEqualTo(25);
        assertThat(response.getBody().getMinStockLevel()).isEqualTo(5);
        verify(stockService).createStock(createDTO);
    }

    @Test
    void testCreateStock_WhenStockAlreadyExists_ShouldThrowException() {
        // Given
        StockDTO createDTO = new StockDTO();
        createDTO.setProduct(testProductDTO);
        createDTO.setWarehouse(testWarehouseDTO);

        when(stockService.createStock(any(StockDTO.class)))
                .thenThrow(new ApplicationException("Stock already exists for this product and warehouse combination", "STOCK_ALREADY_EXISTS"));

        // When & Then
        assertThatThrownBy(() -> stockController.createStock(createDTO))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Stock already exists for this product and warehouse combination");
        verify(stockService).createStock(createDTO);
    }

    @Test
    void testUpdateStock_WithValidData_ShouldReturnUpdatedStock() {
        // Given
        StockDTO updateDTO = new StockDTO();
        updateDTO.setProduct(testProductDTO);
        updateDTO.setWarehouse(testWarehouseDTO);
        updateDTO.setQuantity(75);
        updateDTO.setMinStockLevel(15);
        updateDTO.setMaxStockLevel(150);

        StockDTO updatedStock = new StockDTO();
        updatedStock.setUuid(stockUuid);
        updatedStock.setProduct(testProductDTO);
        updatedStock.setWarehouse(testWarehouseDTO);
        updatedStock.setQuantity(75);
        updatedStock.setMinStockLevel(15);
        updatedStock.setMaxStockLevel(150);

        when(stockService.updateStock(eq(stockUuid), any(StockDTO.class))).thenReturn(updatedStock);

        // When
        ResponseEntity<StockDTO> response = stockController.updateStock(stockUuid, updateDTO);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getQuantity()).isEqualTo(75);
        assertThat(response.getBody().getMinStockLevel()).isEqualTo(15);
        verify(stockService).updateStock(stockUuid, updateDTO);
    }

    @Test
    void testDeleteStock_WhenStockExists_ShouldReturn204() {
        // When
        ResponseEntity<Void> response = stockController.deleteStock(stockUuid);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(stockService).deleteStock(stockUuid);
    }

    @Test
    void testDeleteStock_WhenStockNotFound_ShouldThrowException() {
        // Given
        doThrow(new ApplicationException("Stock not found with id: " + stockUuid, "STOCK_NOT_FOUND"))
                .when(stockService).deleteStock(stockUuid);

        // When & Then
        assertThatThrownBy(() -> stockController.deleteStock(stockUuid))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Stock not found with id: " + stockUuid);
        verify(stockService).deleteStock(stockUuid);
    }

    @Test
    void testAddStock_WithValidQuantity_ShouldReturnUpdatedStock() {
        // Given
        StockDTO updatedStock = new StockDTO();
        updatedStock.setUuid(stockUuid);
        updatedStock.setQuantity(75); // 50 + 25
        when(stockService.addStock(stockUuid, 25)).thenReturn(updatedStock);

        // When
        ResponseEntity<StockDTO> response = stockController.addStock(stockUuid, 25);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getQuantity()).isEqualTo(75);
        verify(stockService).addStock(stockUuid, 25);
    }

    @Test
    void testAddStock_WithNegativeQuantity_ShouldThrowException() {
        // Given
        when(stockService.addStock(stockUuid, -10))
                .thenThrow(new ApplicationException("Quantity to add must be positive", "INVALID_QUANTITY"));

        // When & Then
        assertThatThrownBy(() -> stockController.addStock(stockUuid, -10))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Quantity to add must be positive");
        verify(stockService).addStock(stockUuid, -10);
    }

    @Test
    void testRemoveStock_WithValidQuantity_ShouldReturnUpdatedStock() {
        // Given
        StockDTO updatedStock = new StockDTO();
        updatedStock.setUuid(stockUuid);
        updatedStock.setQuantity(40); // 50 - 10
        when(stockService.removeStock(stockUuid, 10)).thenReturn(updatedStock);

        // When
        ResponseEntity<StockDTO> response = stockController.removeStock(stockUuid, 10);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getQuantity()).isEqualTo(40);
        verify(stockService).removeStock(stockUuid, 10);
    }

    @Test
    void testRemoveStock_WithInsufficientStock_ShouldThrowException() {
        // Given
        when(stockService.removeStock(stockUuid, 100))
                .thenThrow(new ApplicationException("Not enough stock available", "INSUFFICIENT_STOCK"));

        // When & Then
        assertThatThrownBy(() -> stockController.removeStock(stockUuid, 100))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Not enough stock available");
        verify(stockService).removeStock(stockUuid, 100);
    }

    @Test
    void testGetLowStocks_ShouldReturnLowStockList() {
        // Given
        StockDTO lowStock = new StockDTO();
        lowStock.setUuid(stockUuid);
        lowStock.setQuantity(5); // Below min level
        lowStock.setMinStockLevel(10);
        
        List<StockDTO> lowStocks = Arrays.asList(lowStock);
        when(stockService.getLowStocks()).thenReturn(lowStocks);

        // When
        ResponseEntity<List<StockDTO>> response = stockController.getLowStocks();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getQuantity()).isLessThan(response.getBody().get(0).getMinStockLevel());
        verify(stockService).getLowStocks();
    }

    @Test
    void testGetOverStocks_ShouldReturnOverStockList() {
        // Given
        StockDTO overStock = new StockDTO();
        overStock.setUuid(stockUuid);
        overStock.setQuantity(150); // Above max level
        overStock.setMaxStockLevel(100);
        
        List<StockDTO> overStocks = Arrays.asList(overStock);
        when(stockService.getOverStocks()).thenReturn(overStocks);

        // When
        ResponseEntity<List<StockDTO>> response = stockController.getOverStocks();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getQuantity()).isGreaterThan(response.getBody().get(0).getMaxStockLevel());
        verify(stockService).getOverStocks();
    }

    @Test
    void testGetZeroStocks_ShouldReturnZeroStockList() {
        // Given
        StockDTO zeroStock = new StockDTO();
        zeroStock.setUuid(stockUuid);
        zeroStock.setQuantity(0);
        
        List<StockDTO> zeroStocks = Arrays.asList(zeroStock);
        when(stockService.getZeroStocks()).thenReturn(zeroStocks);

        // When
        ResponseEntity<List<StockDTO>> response = stockController.getZeroStocks();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getQuantity()).isEqualTo(0);
        verify(stockService).getZeroStocks();
    }
}