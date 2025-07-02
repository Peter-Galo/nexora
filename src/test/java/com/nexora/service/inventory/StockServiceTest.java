package com.nexora.service.inventory;

import com.nexora.dto.inventory.ProductDTO;
import com.nexora.dto.inventory.StockDTO;
import com.nexora.dto.inventory.WarehouseDTO;
import com.nexora.exception.ApplicationException;
import com.nexora.model.inventory.Product;
import com.nexora.model.inventory.Stock;
import com.nexora.model.inventory.Warehouse;
import com.nexora.repository.inventory.ProductRepository;
import com.nexora.repository.inventory.StockRepository;
import com.nexora.repository.inventory.WarehouseRepository;
import com.nexora.service.inventory.impl.StockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    // Constants for test data
    private static final String PRODUCT_CODE = "P001";
    private static final String PRODUCT_NAME = "Test Product";
    private static final String PRODUCT_DESCRIPTION = "Test product description";
    private static final BigDecimal PRODUCT_PRICE = new BigDecimal("99.99");
    private static final String PRODUCT_CATEGORY = "Test Category";
    private static final String PRODUCT_BRAND = "Test Brand";
    private static final String PRODUCT_SKU = "SKU001";

    private static final String WAREHOUSE_CODE = "WH001";
    private static final String WAREHOUSE_NAME = "Test Warehouse";
    private static final String WAREHOUSE_DESCRIPTION = "Test warehouse description";
    private static final String WAREHOUSE_ADDRESS = "123 Test St";
    private static final String WAREHOUSE_CITY = "Test City";
    private static final String WAREHOUSE_STATE = "Test State";
    private static final String WAREHOUSE_POSTAL_CODE = "12345";
    private static final String WAREHOUSE_COUNTRY = "Test Country";

    private static final int STOCK_QUANTITY = 100;
    private static final int STOCK_MIN_LEVEL = 20;
    private static final int STOCK_MAX_LEVEL = 150;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    private StockService stockService;

    private Stock testStock;
    private StockDTO testStockDTO;
    private Product testProduct;
    private ProductDTO testProductDTO;
    private Warehouse testWarehouse;
    private WarehouseDTO testWarehouseDTO;
    private UUID testStockUuid;
    private UUID testProductUuid;
    private UUID testWarehouseUuid;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        stockService = new StockServiceImpl(stockRepository, productRepository, warehouseRepository);

        // Initialize test data
        testStockUuid = UUID.randomUUID();
        testProductUuid = UUID.randomUUID();
        testWarehouseUuid = UUID.randomUUID();
        now = LocalDateTime.now();

        // Create test entities and DTOs
        testProduct = createTestProduct(testProductUuid);
        testProductDTO = createTestProductDTO(testProductUuid);
        testWarehouse = createTestWarehouse(testWarehouseUuid);
        testWarehouseDTO = createTestWarehouseDTO(testWarehouseUuid);
        testStock = createTestStock(testStockUuid, testProduct, testWarehouse);
        testStockDTO = createTestStockDTO(testStockUuid, testProductDTO, testWarehouseDTO);
    }

    // Helper methods for creating test data
    private Product createTestProduct(UUID uuid) {
        Product product = new Product(PRODUCT_CODE, PRODUCT_NAME, PRODUCT_PRICE);
        product.setUuid(uuid);
        product.setDescription(PRODUCT_DESCRIPTION);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        product.setActive(true);
        product.setCategory(PRODUCT_CATEGORY);
        product.setBrand(PRODUCT_BRAND);
        product.setSku(PRODUCT_SKU);
        return product;
    }

    private ProductDTO createTestProductDTO(UUID uuid) {
        return new ProductDTO(
                uuid,
                PRODUCT_CODE,
                PRODUCT_NAME,
                PRODUCT_DESCRIPTION,
                PRODUCT_PRICE,
                now,
                now,
                true,
                PRODUCT_CATEGORY,
                PRODUCT_BRAND,
                PRODUCT_SKU
        );
    }

    private Warehouse createTestWarehouse(UUID uuid) {
        Warehouse warehouse = new Warehouse(WAREHOUSE_CODE, WAREHOUSE_NAME);
        warehouse.setUuid(uuid);
        warehouse.setDescription(WAREHOUSE_DESCRIPTION);
        warehouse.setAddress(WAREHOUSE_ADDRESS);
        warehouse.setCity(WAREHOUSE_CITY);
        warehouse.setStateProvince(WAREHOUSE_STATE);
        warehouse.setPostalCode(WAREHOUSE_POSTAL_CODE);
        warehouse.setCountry(WAREHOUSE_COUNTRY);
        warehouse.setCreatedAt(now);
        warehouse.setUpdatedAt(now);
        warehouse.setActive(true);
        return warehouse;
    }

    private WarehouseDTO createTestWarehouseDTO(UUID uuid) {
        return new WarehouseDTO(
                uuid,
                WAREHOUSE_CODE,
                WAREHOUSE_NAME,
                WAREHOUSE_DESCRIPTION,
                WAREHOUSE_ADDRESS,
                WAREHOUSE_CITY,
                WAREHOUSE_STATE,
                WAREHOUSE_POSTAL_CODE,
                WAREHOUSE_COUNTRY,
                now,
                now,
                true
        );
    }

    private Stock createTestStock(UUID uuid, Product product, Warehouse warehouse) {
        Stock stock = new Stock(product, warehouse, STOCK_QUANTITY);
        stock.setUuid(uuid);
        stock.setMinStockLevel(STOCK_MIN_LEVEL);
        stock.setMaxStockLevel(STOCK_MAX_LEVEL);
        stock.setLastRestockDate(now.minusDays(10));
        stock.setCreatedAt(now);
        stock.setUpdatedAt(now);
        return stock;
    }

    private StockDTO createTestStockDTO(UUID uuid, ProductDTO productDTO, WarehouseDTO warehouseDTO) {
        return new StockDTO(
                uuid,
                productDTO,
                warehouseDTO,
                STOCK_QUANTITY,
                STOCK_MIN_LEVEL,
                STOCK_MAX_LEVEL,
                now.minusDays(10),
                now,
                now
        );
    }

    // ========== Basic Stock Operations Tests ==========

    @Test
    @DisplayName("Should return all stocks")
    void getAllStocks_shouldReturnAllStocks() {
        // Arrange
        when(stockRepository.findAll()).thenReturn(Arrays.asList(testStock));

        // Act
        List<StockDTO> result = stockService.getAllStocks();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testStock.getUuid(), result.get(0).getUuid());
        assertEquals(testStock.getProduct().getCode(), result.get(0).getProduct().getCode());
        assertEquals(testStock.getWarehouse().getCode(), result.get(0).getWarehouse().getCode());
        assertEquals(testStock.getQuantity(), result.get(0).getQuantity());
        verify(stockRepository).findAll();
        verifyNoMoreInteractions(stockRepository);
    }

    @Test
    @DisplayName("Should return stock by id if exists")
    void getStockById_withExistingId_shouldReturnStock() {
        // Arrange
        when(stockRepository.findById(testStockUuid)).thenReturn(Optional.of(testStock));

        // Act
        StockDTO result = stockService.getStockById(testStockUuid);

        // Assert
        assertNotNull(result);
        assertEquals(testStockUuid, result.getUuid());
        assertEquals(testStock.getProduct().getCode(), result.getProduct().getCode());
        assertEquals(testStock.getWarehouse().getCode(), result.getWarehouse().getCode());
        verify(stockRepository).findById(testStockUuid);
        verifyNoMoreInteractions(stockRepository);
    }

    @Test
    @DisplayName("Should throw exception if stock by id does not exist")
    void getStockById_withNonExistingId_shouldThrowException() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        when(stockRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> stockService.getStockById(nonExistingId)
        );

        assertEquals("Stock not found with id: " + nonExistingId, exception.getMessage());
        assertEquals("STOCK_NOT_FOUND", exception.getCode());
        verify(stockRepository).findById(nonExistingId);
        verifyNoMoreInteractions(stockRepository);
    }

    // ========== Product-related Stock Operations Tests ==========

    @Test
    @DisplayName("Should return stocks by product id if product exists")
    void getStocksByProductId_withExistingProductId_shouldReturnStocks() {
        // Arrange
        when(productRepository.findById(testProductUuid)).thenReturn(Optional.of(testProduct));
        when(stockRepository.findByProduct(testProduct)).thenReturn(Arrays.asList(testStock));

        // Act
        List<StockDTO> result = stockService.getStocksByProductId(testProductUuid);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testStock.getUuid(), result.get(0).getUuid());
        assertEquals(testStock.getProduct().getCode(), result.get(0).getProduct().getCode());
        verify(productRepository).findById(testProductUuid);
        verify(stockRepository).findByProduct(testProduct);
        verifyNoMoreInteractions(productRepository, stockRepository);
    }

    @Test
    @DisplayName("Should throw exception if product by id does not exist")
    void getStocksByProductId_withNonExistingProductId_shouldThrowException() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        when(productRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> stockService.getStocksByProductId(nonExistingId)
        );

        assertEquals("Product not found with id: " + nonExistingId, exception.getMessage());
        assertEquals("PRODUCT_NOT_FOUND", exception.getCode());
        verify(productRepository).findById(nonExistingId);
        verify(stockRepository, never()).findByProduct(any(Product.class));
        verifyNoMoreInteractions(productRepository, stockRepository);
    }

    @Test
    @DisplayName("Should return stocks by product code")
    void getStocksByProductCode_shouldReturnStocks() {
        // Arrange
        when(stockRepository.findByProductCode(PRODUCT_CODE)).thenReturn(Arrays.asList(testStock));

        // Act
        List<StockDTO> result = stockService.getStocksByProductCode(PRODUCT_CODE);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testStock.getUuid(), result.get(0).getUuid());
        assertEquals(testStock.getProduct().getCode(), result.get(0).getProduct().getCode());
        verify(stockRepository).findByProductCode(PRODUCT_CODE);
        verifyNoMoreInteractions(stockRepository);
    }

    // ========== Warehouse-related Stock Operations Tests ==========

    @Test
    @DisplayName("Should return stocks by warehouse id if warehouse exists")
    void getStocksByWarehouseId_withExistingWarehouseId_shouldReturnStocks() {
        // Arrange
        when(warehouseRepository.findById(testWarehouseUuid)).thenReturn(Optional.of(testWarehouse));
        when(stockRepository.findByWarehouse(testWarehouse)).thenReturn(Arrays.asList(testStock));

        // Act
        List<StockDTO> result = stockService.getStocksByWarehouseId(testWarehouseUuid);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testStock.getUuid(), result.get(0).getUuid());
        assertEquals(testStock.getWarehouse().getCode(), result.get(0).getWarehouse().getCode());
        verify(warehouseRepository).findById(testWarehouseUuid);
        verify(stockRepository).findByWarehouse(testWarehouse);
        verifyNoMoreInteractions(warehouseRepository, stockRepository);
    }

    @Test
    @DisplayName("Should throw exception if warehouse by id does not exist")
    void getStocksByWarehouseId_withNonExistingWarehouseId_shouldThrowException() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        when(warehouseRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> stockService.getStocksByWarehouseId(nonExistingId)
        );

        assertEquals("Warehouse not found with id: " + nonExistingId, exception.getMessage());
        assertEquals("WAREHOUSE_NOT_FOUND", exception.getCode());
        verify(warehouseRepository).findById(nonExistingId);
        verify(stockRepository, never()).findByWarehouse(any(Warehouse.class));
        verifyNoMoreInteractions(warehouseRepository, stockRepository);
    }

    @Test
    @DisplayName("Should return stocks by warehouse code")
    void getStocksByWarehouseCode_shouldReturnStocks() {
        // Arrange
        when(stockRepository.findByWarehouseCode(WAREHOUSE_CODE)).thenReturn(Arrays.asList(testStock));

        // Act
        List<StockDTO> result = stockService.getStocksByWarehouseCode(WAREHOUSE_CODE);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testStock.getUuid(), result.get(0).getUuid());
        assertEquals(testStock.getWarehouse().getCode(), result.get(0).getWarehouse().getCode());
        verify(stockRepository).findByWarehouseCode(WAREHOUSE_CODE);
        verifyNoMoreInteractions(stockRepository);
    }

    // ========== Combined Product and Warehouse Stock Operations Tests ==========

    @Test
    @DisplayName("Should return stock by product and warehouse if exists")
    void getStockByProductAndWarehouse_withExistingIds_shouldReturnStock() {
        // Arrange
        when(productRepository.findById(testProductUuid)).thenReturn(Optional.of(testProduct));
        when(warehouseRepository.findById(testWarehouseUuid)).thenReturn(Optional.of(testWarehouse));
        when(stockRepository.findByProductAndWarehouse(testProduct, testWarehouse)).thenReturn(Optional.of(testStock));

        // Act
        StockDTO result = stockService.getStockByProductAndWarehouse(testProductUuid, testWarehouseUuid);

        // Assert
        assertNotNull(result);
        assertEquals(testStockUuid, result.getUuid());
        assertEquals(testStock.getProduct().getCode(), result.getProduct().getCode());
        assertEquals(testStock.getWarehouse().getCode(), result.getWarehouse().getCode());
        verify(productRepository).findById(testProductUuid);
        verify(warehouseRepository).findById(testWarehouseUuid);
        verify(stockRepository).findByProductAndWarehouse(testProduct, testWarehouse);
        verifyNoMoreInteractions(productRepository, warehouseRepository, stockRepository);
    }

    @Test
    @DisplayName("Should throw exception if product does not exist when getting stock by product and warehouse")
    void getStockByProductAndWarehouse_withNonExistingProductId_shouldThrowException() {
        // Arrange
        UUID nonExistingProductId = UUID.randomUUID();
        when(productRepository.findById(nonExistingProductId)).thenReturn(Optional.empty());

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> stockService.getStockByProductAndWarehouse(nonExistingProductId, testWarehouseUuid)
        );

        assertEquals("Product not found with id: " + nonExistingProductId, exception.getMessage());
        assertEquals("PRODUCT_NOT_FOUND", exception.getCode());
        verify(productRepository).findById(nonExistingProductId);
        verify(warehouseRepository, never()).findById(any(UUID.class));
        verify(stockRepository, never()).findByProductAndWarehouse(any(Product.class), any(Warehouse.class));
        verifyNoMoreInteractions(productRepository, warehouseRepository, stockRepository);
    }

    @Test
    @DisplayName("Should throw exception if warehouse does not exist when getting stock by product and warehouse")
    void getStockByProductAndWarehouse_withNonExistingWarehouseId_shouldThrowException() {
        // Arrange
        UUID nonExistingWarehouseId = UUID.randomUUID();
        when(productRepository.findById(testProductUuid)).thenReturn(Optional.of(testProduct));
        when(warehouseRepository.findById(nonExistingWarehouseId)).thenReturn(Optional.empty());

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> stockService.getStockByProductAndWarehouse(testProductUuid, nonExistingWarehouseId)
        );

        assertEquals("Warehouse not found with id: " + nonExistingWarehouseId, exception.getMessage());
        assertEquals("WAREHOUSE_NOT_FOUND", exception.getCode());
        verify(productRepository).findById(testProductUuid);
        verify(warehouseRepository).findById(nonExistingWarehouseId);
        verify(stockRepository, never()).findByProductAndWarehouse(any(Product.class), any(Warehouse.class));
        verifyNoMoreInteractions(productRepository, warehouseRepository, stockRepository);
    }

    @Test
    @DisplayName("Should throw exception if stock does not exist for product and warehouse")
    void getStockByProductAndWarehouse_withNonExistingStock_shouldThrowException() {
        // Arrange
        when(productRepository.findById(testProductUuid)).thenReturn(Optional.of(testProduct));
        when(warehouseRepository.findById(testWarehouseUuid)).thenReturn(Optional.of(testWarehouse));
        when(stockRepository.findByProductAndWarehouse(testProduct, testWarehouse)).thenReturn(Optional.empty());

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> stockService.getStockByProductAndWarehouse(testProductUuid, testWarehouseUuid)
        );

        assertEquals("Stock not found for product id: " + testProductUuid + " and warehouse id: " + testWarehouseUuid, exception.getMessage());
        assertEquals("STOCK_NOT_FOUND", exception.getCode());
        verify(productRepository).findById(testProductUuid);
        verify(warehouseRepository).findById(testWarehouseUuid);
        verify(stockRepository).findByProductAndWarehouse(testProduct, testWarehouse);
        verifyNoMoreInteractions(productRepository, warehouseRepository, stockRepository);
    }

    // ========== Stock CRUD Operations Tests ==========

    @Test
    @DisplayName("Should create and return stock with valid data")
    void createStock_withValidData_shouldCreateAndReturnStock() {
        // Arrange
        when(productRepository.findById(testProductUuid)).thenReturn(Optional.of(testProduct));
        when(warehouseRepository.findById(testWarehouseUuid)).thenReturn(Optional.of(testWarehouse));
        when(stockRepository.findByProductAndWarehouse(testProduct, testWarehouse)).thenReturn(Optional.empty());

        Stock savedStock = createTestStock(testStockUuid, testProduct, testWarehouse);
        when(stockRepository.save(any(Stock.class))).thenReturn(savedStock);

        // Act
        StockDTO result = stockService.createStock(testStockDTO);

        // Assert
        assertNotNull(result);
        assertEquals(savedStock.getUuid(), result.getUuid());
        assertEquals(testStockDTO.getProduct().getCode(), result.getProduct().getCode());
        assertEquals(testStockDTO.getWarehouse().getCode(), result.getWarehouse().getCode());
        assertEquals(testStockDTO.getQuantity(), result.getQuantity());

        // Verify stock was saved with correct data
        ArgumentCaptor<Stock> stockCaptor = ArgumentCaptor.forClass(Stock.class);
        verify(stockRepository).save(stockCaptor.capture());
        Stock capturedStock = stockCaptor.getValue();

        assertEquals(testProduct, capturedStock.getProduct());
        assertEquals(testWarehouse, capturedStock.getWarehouse());
        assertEquals(testStockDTO.getQuantity(), capturedStock.getQuantity());
        assertEquals(testStockDTO.getMinStockLevel(), capturedStock.getMinStockLevel());
        assertEquals(testStockDTO.getMaxStockLevel(), capturedStock.getMaxStockLevel());
        assertEquals(testStockDTO.getLastRestockDate(), capturedStock.getLastRestockDate());

        verify(productRepository).findById(testProductUuid);
        verify(warehouseRepository).findById(testWarehouseUuid);
        verify(stockRepository).findByProductAndWarehouse(testProduct, testWarehouse);
        verifyNoMoreInteractions(productRepository, warehouseRepository, stockRepository);
    }

    @Test
    @DisplayName("Should throw exception when creating stock with existing product and warehouse")
    void createStock_withExistingProductAndWarehouse_shouldThrowException() {
        // Arrange
        when(productRepository.findById(testProductUuid)).thenReturn(Optional.of(testProduct));
        when(warehouseRepository.findById(testWarehouseUuid)).thenReturn(Optional.of(testWarehouse));
        when(stockRepository.findByProductAndWarehouse(testProduct, testWarehouse)).thenReturn(Optional.of(testStock));

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> stockService.createStock(testStockDTO)
        );

        assertEquals("Stock already exists for product code: " + PRODUCT_CODE + " and warehouse code: " + WAREHOUSE_CODE, exception.getMessage());
        assertEquals("STOCK_ALREADY_EXISTS", exception.getCode());
        verify(productRepository).findById(testProductUuid);
        verify(warehouseRepository).findById(testWarehouseUuid);
        verify(stockRepository).findByProductAndWarehouse(testProduct, testWarehouse);
        verify(stockRepository, never()).save(any(Stock.class));
        verifyNoMoreInteractions(productRepository, warehouseRepository, stockRepository);
    }

    @Test
    @DisplayName("Should update and return stock with valid data")
    void updateStock_withValidData_shouldUpdateAndReturnStock() {
        // Arrange
        when(stockRepository.findById(testStockUuid)).thenReturn(Optional.of(testStock));
        when(productRepository.findById(testProductUuid)).thenReturn(Optional.of(testProduct));
        when(warehouseRepository.findById(testWarehouseUuid)).thenReturn(Optional.of(testWarehouse));

        // Updated stock DTO with different values
        int updatedQuantity = 150;
        int updatedMinLevel = 30;
        int updatedMaxLevel = 200;
        LocalDateTime updatedRestockDate = LocalDateTime.now();

        StockDTO updateDTO = new StockDTO(
                testStockUuid,
                testProductDTO,
                testWarehouseDTO,
                updatedQuantity,
                updatedMinLevel,
                updatedMaxLevel,
                updatedRestockDate,
                testStockDTO.getCreatedAt(),
                null
        );

        // Create updated stock entity
        Stock updatedStock = new Stock(testProduct, testWarehouse, updatedQuantity);
        updatedStock.setUuid(testStockUuid);
        updatedStock.setMinStockLevel(updatedMinLevel);
        updatedStock.setMaxStockLevel(updatedMaxLevel);
        updatedStock.setLastRestockDate(updatedRestockDate);
        updatedStock.setCreatedAt(testStock.getCreatedAt());
        updatedStock.setUpdatedAt(LocalDateTime.now());

        when(stockRepository.save(any(Stock.class))).thenReturn(updatedStock);

        // Act
        StockDTO result = stockService.updateStock(testStockUuid, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testStockUuid, result.getUuid());
        assertEquals(updatedQuantity, result.getQuantity());
        assertEquals(updatedMinLevel, result.getMinStockLevel());
        assertEquals(updatedMaxLevel, result.getMaxStockLevel());

        // Verify stock was saved with correct data
        ArgumentCaptor<Stock> stockCaptor = ArgumentCaptor.forClass(Stock.class);
        verify(stockRepository).save(stockCaptor.capture());
        Stock capturedStock = stockCaptor.getValue();

        assertEquals(testProduct, capturedStock.getProduct());
        assertEquals(testWarehouse, capturedStock.getWarehouse());
        assertEquals(updatedQuantity, capturedStock.getQuantity());
        assertEquals(updatedMinLevel, capturedStock.getMinStockLevel());
        assertEquals(updatedMaxLevel, capturedStock.getMaxStockLevel());

        verify(stockRepository).findById(testStockUuid);
        verify(productRepository).findById(testProductUuid);
        verify(warehouseRepository).findById(testWarehouseUuid);
        verifyNoMoreInteractions(stockRepository, productRepository, warehouseRepository);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existing stock")
    void updateStock_withNonExistingId_shouldThrowException() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        when(stockRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> stockService.updateStock(nonExistingId, testStockDTO)
        );

        assertEquals("Stock not found with id: " + nonExistingId, exception.getMessage());
        assertEquals("STOCK_NOT_FOUND", exception.getCode());
        verify(stockRepository).findById(nonExistingId);
        verify(stockRepository, never()).save(any(Stock.class));
        verifyNoMoreInteractions(stockRepository, productRepository, warehouseRepository);
    }

    @Test
    @DisplayName("Should delete stock with existing id")
    void deleteStock_withExistingId_shouldDeleteStock() {
        // Arrange
        when(stockRepository.existsById(testStockUuid)).thenReturn(true);

        // Act
        stockService.deleteStock(testStockUuid);

        // Assert
        verify(stockRepository).existsById(testStockUuid);
        verify(stockRepository).deleteById(testStockUuid);
        verifyNoMoreInteractions(stockRepository);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existing stock")
    void deleteStock_withNonExistingId_shouldThrowException() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        when(stockRepository.existsById(nonExistingId)).thenReturn(false);

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> stockService.deleteStock(nonExistingId)
        );

        assertEquals("Stock not found with id: " + nonExistingId, exception.getMessage());
        assertEquals("STOCK_NOT_FOUND", exception.getCode());
        verify(stockRepository).existsById(nonExistingId);
        verify(stockRepository, never()).deleteById(any(UUID.class));
        verifyNoMoreInteractions(stockRepository);
    }

    // ========== Stock Quantity Operations Tests ==========

    @Test
    @DisplayName("Should add stock and return updated stock with valid data")
    void addStock_withValidData_shouldAddStockAndReturnUpdatedStock() {
        // Arrange
        when(stockRepository.findById(testStockUuid)).thenReturn(Optional.of(testStock));

        int quantityToAdd = 50;
        int newQuantity = testStock.getQuantity() + quantityToAdd;

        Stock updatedStock = new Stock(testProduct, testWarehouse, newQuantity);
        updatedStock.setUuid(testStockUuid);
        updatedStock.setMinStockLevel(testStock.getMinStockLevel());
        updatedStock.setMaxStockLevel(testStock.getMaxStockLevel());
        updatedStock.setLastRestockDate(LocalDateTime.now());
        updatedStock.setCreatedAt(testStock.getCreatedAt());
        updatedStock.setUpdatedAt(LocalDateTime.now());

        when(stockRepository.save(any(Stock.class))).thenReturn(updatedStock);

        // Act
        StockDTO result = stockService.addStock(testStockUuid, quantityToAdd);

        // Assert
        assertNotNull(result);
        assertEquals(newQuantity, result.getQuantity());

        // Verify stock was saved with correct quantity
        ArgumentCaptor<Stock> stockCaptor = ArgumentCaptor.forClass(Stock.class);
        verify(stockRepository).save(stockCaptor.capture());
        Stock capturedStock = stockCaptor.getValue();

        assertEquals(newQuantity, capturedStock.getQuantity());
        verify(stockRepository).findById(testStockUuid);
        verifyNoMoreInteractions(stockRepository, productRepository, warehouseRepository);
    }

    @Test
    @DisplayName("Should throw exception when adding negative stock quantity")
    void addStock_withNegativeQuantity_shouldThrowException() {
        // Arrange
        int negativeQuantity = -10;

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> stockService.addStock(testStockUuid, negativeQuantity)
        );

        assertEquals("Cannot add negative stock amount", exception.getMessage());
        assertEquals("INVALID_QUANTITY", exception.getCode());
        verify(stockRepository, never()).findById(any(UUID.class));
        verify(stockRepository, never()).save(any(Stock.class));
        verifyNoMoreInteractions(stockRepository, productRepository, warehouseRepository);
    }

    @Test
    @DisplayName("Should throw exception when adding stock to non-existing id")
    void addStock_withNonExistingId_shouldThrowException() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        when(stockRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> stockService.addStock(nonExistingId, 50)
        );

        assertEquals("Stock not found with id: " + nonExistingId, exception.getMessage());
        assertEquals("STOCK_NOT_FOUND", exception.getCode());
        verify(stockRepository).findById(nonExistingId);
        verify(stockRepository, never()).save(any(Stock.class));
        verifyNoMoreInteractions(stockRepository, productRepository, warehouseRepository);
    }

    @Test
    @DisplayName("Should remove stock and return updated stock with valid data")
    void removeStock_withValidData_shouldRemoveStockAndReturnUpdatedStock() {
        // Arrange
        when(stockRepository.findById(testStockUuid)).thenReturn(Optional.of(testStock));

        int quantityToRemove = 30;
        int newQuantity = testStock.getQuantity() - quantityToRemove;

        Stock updatedStock = new Stock(testProduct, testWarehouse, newQuantity);
        updatedStock.setUuid(testStockUuid);
        updatedStock.setMinStockLevel(testStock.getMinStockLevel());
        updatedStock.setMaxStockLevel(testStock.getMaxStockLevel());
        updatedStock.setLastRestockDate(testStock.getLastRestockDate());
        updatedStock.setCreatedAt(testStock.getCreatedAt());
        updatedStock.setUpdatedAt(LocalDateTime.now());

        when(stockRepository.save(any(Stock.class))).thenReturn(updatedStock);

        // Act
        StockDTO result = stockService.removeStock(testStockUuid, quantityToRemove);

        // Assert
        assertNotNull(result);
        assertEquals(newQuantity, result.getQuantity());

        // Verify stock was saved with correct quantity
        ArgumentCaptor<Stock> stockCaptor = ArgumentCaptor.forClass(Stock.class);
        verify(stockRepository).save(stockCaptor.capture());
        Stock capturedStock = stockCaptor.getValue();

        assertEquals(newQuantity, capturedStock.getQuantity());
        verify(stockRepository).findById(testStockUuid);
        verifyNoMoreInteractions(stockRepository, productRepository, warehouseRepository);
    }

    @Test
    @DisplayName("Should throw exception when removing negative stock quantity")
    void removeStock_withNegativeQuantity_shouldThrowException() {
        // Arrange
        int negativeQuantity = -10;

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> stockService.removeStock(testStockUuid, negativeQuantity)
        );

        assertEquals("Cannot remove negative stock amount", exception.getMessage());
        assertEquals("INVALID_QUANTITY", exception.getCode());
        verify(stockRepository, never()).findById(any(UUID.class));
        verify(stockRepository, never()).save(any(Stock.class));
        verifyNoMoreInteractions(stockRepository, productRepository, warehouseRepository);
    }

    @Test
    @DisplayName("Should throw exception when removing more stock than available")
    void removeStock_withInsufficientStock_shouldThrowException() {
        // Arrange
        when(stockRepository.findById(testStockUuid)).thenReturn(Optional.of(testStock));
        int excessiveQuantity = testStock.getQuantity() + 10;

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> stockService.removeStock(testStockUuid, excessiveQuantity)
        );

        assertEquals("Not enough stock available", exception.getMessage());
        assertEquals("INSUFFICIENT_STOCK", exception.getCode());
        verify(stockRepository).findById(testStockUuid);
        verify(stockRepository, never()).save(any(Stock.class));
        verifyNoMoreInteractions(stockRepository, productRepository, warehouseRepository);
    }

    @Test
    @DisplayName("Should throw exception when removing stock from non-existing id")
    void removeStock_withNonExistingId_shouldThrowException() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        when(stockRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> stockService.removeStock(nonExistingId, 30)
        );

        assertEquals("Stock not found with id: " + nonExistingId, exception.getMessage());
        assertEquals("STOCK_NOT_FOUND", exception.getCode());
        verify(stockRepository).findById(nonExistingId);
        verify(stockRepository, never()).save(any(Stock.class));
        verifyNoMoreInteractions(stockRepository, productRepository, warehouseRepository);
    }

    // ========== Special Stock Queries Tests ==========

    @Test
    @DisplayName("Should return stocks with quantity below minimum level")
    void getLowStocks_shouldReturnLowStocks() {
        // Arrange
        when(stockRepository.findLowStock()).thenReturn(Arrays.asList(testStock));

        // Act
        List<StockDTO> result = stockService.getLowStocks();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testStock.getUuid(), result.get(0).getUuid());
        verify(stockRepository).findLowStock();
        verifyNoMoreInteractions(stockRepository, productRepository, warehouseRepository);
    }

    @Test
    @DisplayName("Should return stocks with quantity above maximum level")
    void getOverStocks_shouldReturnOverStocks() {
        // Arrange
        when(stockRepository.findOverStock()).thenReturn(Arrays.asList(testStock));

        // Act
        List<StockDTO> result = stockService.getOverStocks();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testStock.getUuid(), result.get(0).getUuid());
        verify(stockRepository).findOverStock();
        verifyNoMoreInteractions(stockRepository, productRepository, warehouseRepository);
    }

    @Test
    @DisplayName("Should return stocks with zero quantity")
    void getZeroStocks_shouldReturnZeroStocks() {
        // Arrange
        when(stockRepository.findByQuantity(0)).thenReturn(Arrays.asList(testStock));

        // Act
        List<StockDTO> result = stockService.getZeroStocks();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testStock.getUuid(), result.get(0).getUuid());
        verify(stockRepository).findByQuantity(0);
        verifyNoMoreInteractions(stockRepository, productRepository, warehouseRepository);
    }
}
