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
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private StockServiceImpl stockService;

    private Stock testStock1;
    private Stock testStock2;
    private StockDTO testStockDTO1;
    private StockDTO testStockDTO2;
    private Product testProduct1;
    private Product testProduct2;
    private Warehouse testWarehouse1;
    private Warehouse testWarehouse2;
    private ProductDTO testProductDTO1;
    private WarehouseDTO testWarehouseDTO1;
    private UUID stockUuid1;
    private UUID stockUuid2;
    private UUID productUuid1;
    private UUID productUuid2;
    private UUID warehouseUuid1;
    private UUID warehouseUuid2;

    @BeforeEach
    void setUp() {
        stockUuid1 = UUID.randomUUID();
        stockUuid2 = UUID.randomUUID();
        productUuid1 = UUID.randomUUID();
        productUuid2 = UUID.randomUUID();
        warehouseUuid1 = UUID.randomUUID();
        warehouseUuid2 = UUID.randomUUID();

        // Create test products
        testProduct1 = new Product();
        testProduct1.setUuid(productUuid1);
        testProduct1.setCode("PROD001");
        testProduct1.setName("Test Product 1");
        testProduct1.setPrice(new BigDecimal("99.99"));
        testProduct1.setActive(true);

        testProduct2 = new Product();
        testProduct2.setUuid(productUuid2);
        testProduct2.setCode("PROD002");
        testProduct2.setName("Test Product 2");
        testProduct2.setPrice(new BigDecimal("149.99"));
        testProduct2.setActive(true);

        // Create test warehouses
        testWarehouse1 = new Warehouse();
        testWarehouse1.setUuid(warehouseUuid1);
        testWarehouse1.setCode("WH001");
        testWarehouse1.setName("Main Warehouse");
        testWarehouse1.setCity("Boston");
        testWarehouse1.setActive(true);

        testWarehouse2 = new Warehouse();
        testWarehouse2.setUuid(warehouseUuid2);
        testWarehouse2.setCode("WH002");
        testWarehouse2.setName("Secondary Warehouse");
        testWarehouse2.setCity("New York");
        testWarehouse2.setActive(true);

        // Create test stock entities
        testStock1 = new Stock();
        testStock1.setUuid(stockUuid1);
        testStock1.setProduct(testProduct1);
        testStock1.setWarehouse(testWarehouse1);
        testStock1.setQuantity(50);
        testStock1.setMinStockLevel(10);
        testStock1.setMaxStockLevel(100);
        testStock1.setLastRestockDate(LocalDateTime.now().minusDays(5));
        testStock1.setCreatedAt(LocalDateTime.now());
        testStock1.setUpdatedAt(LocalDateTime.now());

        testStock2 = new Stock();
        testStock2.setUuid(stockUuid2);
        testStock2.setProduct(testProduct2);
        testStock2.setWarehouse(testWarehouse2);
        testStock2.setQuantity(5); // Low stock
        testStock2.setMinStockLevel(10);
        testStock2.setMaxStockLevel(50);
        testStock2.setLastRestockDate(LocalDateTime.now().minusDays(10));
        testStock2.setCreatedAt(LocalDateTime.now());
        testStock2.setUpdatedAt(LocalDateTime.now());

        // Create test DTOs
        testProductDTO1 = new ProductDTO();
        testProductDTO1.setUuid(productUuid1);
        testProductDTO1.setCode("PROD001");
        testProductDTO1.setName("Test Product 1");
        testProductDTO1.setPrice(new BigDecimal("99.99"));
        testProductDTO1.setActive(true);

        testWarehouseDTO1 = new WarehouseDTO();
        testWarehouseDTO1.setUuid(warehouseUuid1);
        testWarehouseDTO1.setCode("WH001");
        testWarehouseDTO1.setName("Main Warehouse");
        testWarehouseDTO1.setCity("Boston");
        testWarehouseDTO1.setActive(true);

        testStockDTO1 = new StockDTO();
        testStockDTO1.setUuid(stockUuid1);
        testStockDTO1.setProduct(testProductDTO1);
        testStockDTO1.setWarehouse(testWarehouseDTO1);
        testStockDTO1.setQuantity(50);
        testStockDTO1.setMinStockLevel(10);
        testStockDTO1.setMaxStockLevel(100);
        testStockDTO1.setLastRestockDate(LocalDateTime.now().minusDays(5));

        testStockDTO2 = new StockDTO();
        testStockDTO2.setUuid(stockUuid2);
        testStockDTO2.setQuantity(5);
        testStockDTO2.setMinStockLevel(10);
        testStockDTO2.setMaxStockLevel(50);
    }

    @Test
    void testGetAllStocks_ShouldReturnAllStocks() {
        // Given
        when(stockRepository.findAllWithProductAndWarehouse()).thenReturn(Arrays.asList(testStock1, testStock2));
        when(entityMapper.mapToDTO(testStock1, StockDTO.class)).thenReturn(testStockDTO1);
        when(entityMapper.mapToDTO(testStock2, StockDTO.class)).thenReturn(testStockDTO2);

        // When
        List<StockDTO> result = stockService.getAllStocks();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUuid()).isEqualTo(stockUuid1);
        assertThat(result.get(1).getUuid()).isEqualTo(stockUuid2);
        verify(stockRepository).findAllWithProductAndWarehouse();
        verify(entityMapper).mapToDTO(testStock1, StockDTO.class);
        verify(entityMapper).mapToDTO(testStock2, StockDTO.class);
    }

    @Test
    void testGetStockById_WhenStockExists_ShouldReturnStock() {
        // Given
        when(stockRepository.findById(stockUuid1)).thenReturn(Optional.of(testStock1));
        when(entityMapper.mapToDTO(testStock1, StockDTO.class)).thenReturn(testStockDTO1);

        // When
        StockDTO result = stockService.getStockById(stockUuid1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isEqualTo(stockUuid1);
        assertThat(result.getQuantity()).isEqualTo(50);
        verify(stockRepository).findById(stockUuid1);
        verify(entityMapper).mapToDTO(testStock1, StockDTO.class);
    }

    @Test
    void testGetStockById_WhenStockDoesNotExist_ShouldThrowException() {
        // Given
        when(stockRepository.findById(stockUuid1)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> stockService.getStockById(stockUuid1))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Stock not found with id: " + stockUuid1);
        verify(stockRepository).findById(stockUuid1);
        verify(entityMapper, never()).mapToDTO(any(Stock.class), eq(StockDTO.class));
    }

    @Test
    void testGetStocksByProductId_ShouldReturnStocksForProduct() {
        // Given
        when(productRepository.findById(productUuid1)).thenReturn(Optional.of(testProduct1));
        when(stockRepository.findByProduct(testProduct1)).thenReturn(Arrays.asList(testStock1));
        when(entityMapper.mapToDTO(testStock1, StockDTO.class)).thenReturn(testStockDTO1);

        // When
        List<StockDTO> result = stockService.getStocksByProductId(productUuid1);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUuid()).isEqualTo(stockUuid1);
        verify(productRepository).findById(productUuid1);
        verify(stockRepository).findByProduct(testProduct1);
        verify(entityMapper).mapToDTO(testStock1, StockDTO.class);
    }

    @Test
    void testGetStocksByProductId_WhenProductDoesNotExist_ShouldThrowException() {
        // Given
        when(productRepository.findById(productUuid1)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> stockService.getStocksByProductId(productUuid1))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Product not found with id: " + productUuid1);
        verify(productRepository).findById(productUuid1);
        verify(stockRepository, never()).findByProduct(any(Product.class));
    }

    @Test
    void testGetStocksByProductCode_ShouldReturnStocksForProductCode() {
        // Given
        when(stockRepository.findByProductCode("PROD001")).thenReturn(Arrays.asList(testStock1));
        when(entityMapper.mapToDTO(testStock1, StockDTO.class)).thenReturn(testStockDTO1);

        // When
        List<StockDTO> result = stockService.getStocksByProductCode("PROD001");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUuid()).isEqualTo(stockUuid1);
        verify(stockRepository).findByProductCode("PROD001");
        verify(entityMapper).mapToDTO(testStock1, StockDTO.class);
    }

    @Test
    void testGetStocksByWarehouseId_ShouldReturnStocksForWarehouse() {
        // Given
        when(warehouseRepository.findById(warehouseUuid1)).thenReturn(Optional.of(testWarehouse1));
        when(stockRepository.findByWarehouse(testWarehouse1)).thenReturn(Arrays.asList(testStock1));
        when(entityMapper.mapToDTO(testStock1, StockDTO.class)).thenReturn(testStockDTO1);

        // When
        List<StockDTO> result = stockService.getStocksByWarehouseId(warehouseUuid1);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUuid()).isEqualTo(stockUuid1);
        verify(warehouseRepository).findById(warehouseUuid1);
        verify(stockRepository).findByWarehouse(testWarehouse1);
        verify(entityMapper).mapToDTO(testStock1, StockDTO.class);
    }

    @Test
    void testGetStocksByWarehouseCode_ShouldReturnStocksForWarehouseCode() {
        // Given
        when(stockRepository.findByWarehouseCode("WH001")).thenReturn(Arrays.asList(testStock1));
        when(entityMapper.mapToDTO(testStock1, StockDTO.class)).thenReturn(testStockDTO1);

        // When
        List<StockDTO> result = stockService.getStocksByWarehouseCode("WH001");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUuid()).isEqualTo(stockUuid1);
        verify(stockRepository).findByWarehouseCode("WH001");
        verify(entityMapper).mapToDTO(testStock1, StockDTO.class);
    }

    @Test
    void testGetStockByProductAndWarehouse_WhenStockExists_ShouldReturnStock() {
        // Given
        when(productRepository.findById(productUuid1)).thenReturn(Optional.of(testProduct1));
        when(warehouseRepository.findById(warehouseUuid1)).thenReturn(Optional.of(testWarehouse1));
        when(stockRepository.findByProductAndWarehouse(testProduct1, testWarehouse1)).thenReturn(Optional.of(testStock1));
        when(entityMapper.mapToDTO(testStock1, StockDTO.class)).thenReturn(testStockDTO1);

        // When
        StockDTO result = stockService.getStockByProductAndWarehouse(productUuid1, warehouseUuid1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isEqualTo(stockUuid1);
        verify(productRepository).findById(productUuid1);
        verify(warehouseRepository).findById(warehouseUuid1);
        verify(stockRepository).findByProductAndWarehouse(testProduct1, testWarehouse1);
        verify(entityMapper).mapToDTO(testStock1, StockDTO.class);
    }

    @Test
    void testCreateStock_WhenStockDoesNotExist_ShouldCreateStock() {
        // Given
        when(productRepository.findById(productUuid1)).thenReturn(Optional.of(testProduct1));
        when(warehouseRepository.findById(warehouseUuid1)).thenReturn(Optional.of(testWarehouse1));
        when(stockRepository.findByProductAndWarehouse(any(Product.class), any(Warehouse.class))).thenReturn(Optional.empty());
        when(entityMapper.mapToEntity(testStockDTO1, Stock.class)).thenReturn(testStock1);
        when(stockRepository.save(any(Stock.class))).thenAnswer(invocation -> {
            Stock stock = invocation.getArgument(0);
            stock.setUuid(stockUuid1);
            return stock;
        });
        when(entityMapper.mapToDTO(any(Stock.class), eq(StockDTO.class))).thenReturn(testStockDTO1);

        // When
        StockDTO result = stockService.createStock(testStockDTO1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isEqualTo(stockUuid1);
        verify(stockRepository).findByProductAndWarehouse(any(Product.class), any(Warehouse.class));
        verify(entityMapper).mapToEntity(testStockDTO1, Stock.class);
        verify(stockRepository).save(any(Stock.class));
        verify(entityMapper).mapToDTO(any(Stock.class), eq(StockDTO.class));
    }

    @Test
    void testCreateStock_WhenStockAlreadyExists_ShouldThrowException() {
        // Given
        when(productRepository.findById(productUuid1)).thenReturn(Optional.of(testProduct1));
        when(warehouseRepository.findById(warehouseUuid1)).thenReturn(Optional.of(testWarehouse1));
        when(stockRepository.findByProductAndWarehouse(any(Product.class), any(Warehouse.class))).thenReturn(Optional.of(testStock1));

        // When & Then
        assertThatThrownBy(() -> stockService.createStock(testStockDTO1))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Stock already exists for this product and warehouse combination");
        verify(stockRepository).findByProductAndWarehouse(any(Product.class), any(Warehouse.class));
        verify(stockRepository, never()).save(any(Stock.class));
    }

    @Test
    void testUpdateStock_WhenStockExists_ShouldUpdateStock() {
        // Given
        when(stockRepository.findById(stockUuid1)).thenReturn(Optional.of(testStock1));
        when(productRepository.findById(productUuid1)).thenReturn(Optional.of(testProduct1));
        when(warehouseRepository.findById(warehouseUuid1)).thenReturn(Optional.of(testWarehouse1));
        when(stockRepository.save(any(Stock.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(entityMapper.mapToDTO(any(Stock.class), eq(StockDTO.class))).thenReturn(testStockDTO1);

        StockDTO updateDTO = new StockDTO();
        updateDTO.setProduct(testProductDTO1);
        updateDTO.setWarehouse(testWarehouseDTO1);
        updateDTO.setQuantity(75);
        updateDTO.setMinStockLevel(15);
        updateDTO.setMaxStockLevel(150);

        // When
        StockDTO result = stockService.updateStock(stockUuid1, updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(stockRepository).findById(stockUuid1);
        verify(entityMapper).mapToExistingEntity(updateDTO, testStock1);
        verify(stockRepository).save(any(Stock.class));
        verify(entityMapper).mapToDTO(any(Stock.class), eq(StockDTO.class));
    }

    @Test
    void testDeleteStock_WhenStockExists_ShouldDeleteStock() {
        // Given
        when(stockRepository.existsById(stockUuid1)).thenReturn(true);

        // When
        stockService.deleteStock(stockUuid1);

        // Then
        verify(stockRepository).existsById(stockUuid1);
        verify(stockRepository).deleteById(stockUuid1);
    }

    @Test
    void testDeleteStock_WhenStockDoesNotExist_ShouldThrowException() {
        // Given
        when(stockRepository.existsById(stockUuid1)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> stockService.deleteStock(stockUuid1))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Stock not found with id: " + stockUuid1);
        verify(stockRepository).existsById(stockUuid1);
        verify(stockRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void testAddStock_WhenStockExists_ShouldAddQuantity() {
        // Given
        when(stockRepository.findById(stockUuid1)).thenReturn(Optional.of(testStock1));
        when(stockRepository.save(any(Stock.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(entityMapper.mapToDTO(any(Stock.class), eq(StockDTO.class))).thenReturn(testStockDTO1);

        // When
        StockDTO result = stockService.addStock(stockUuid1, 25);

        // Then
        assertThat(result).isNotNull();
        verify(stockRepository).findById(stockUuid1);
        verify(stockRepository).save(any(Stock.class));
        verify(entityMapper).mapToDTO(any(Stock.class), eq(StockDTO.class));
    }

    @Test
    void testAddStock_WithNegativeQuantity_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> stockService.addStock(stockUuid1, -10))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Quantity to add must be positive");
        verify(stockRepository, never()).findById(any(UUID.class));
    }

    @Test
    void testRemoveStock_WhenStockExists_ShouldRemoveQuantity() {
        // Given
        when(stockRepository.findById(stockUuid1)).thenReturn(Optional.of(testStock1));
        when(stockRepository.save(any(Stock.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(entityMapper.mapToDTO(any(Stock.class), eq(StockDTO.class))).thenReturn(testStockDTO1);

        // When
        StockDTO result = stockService.removeStock(stockUuid1, 10);

        // Then
        assertThat(result).isNotNull();
        verify(stockRepository).findById(stockUuid1);
        verify(stockRepository).save(any(Stock.class));
        verify(entityMapper).mapToDTO(any(Stock.class), eq(StockDTO.class));
    }

    @Test
    void testRemoveStock_WithNegativeQuantity_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> stockService.removeStock(stockUuid1, -10))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Quantity to remove must be positive");
        verify(stockRepository, never()).findById(any(UUID.class));
    }

    @Test
    void testGetLowStocks_ShouldReturnLowStockRecords() {
        // Given
        when(stockRepository.findLowStock()).thenReturn(Arrays.asList(testStock2));
        when(entityMapper.mapToDTO(testStock2, StockDTO.class)).thenReturn(testStockDTO2);

        // When
        List<StockDTO> result = stockService.getLowStocks();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUuid()).isEqualTo(stockUuid2);
        verify(stockRepository).findLowStock();
        verify(entityMapper).mapToDTO(testStock2, StockDTO.class);
    }

    @Test
    void testGetOverStocks_ShouldReturnOverStockRecords() {
        // Given
        when(stockRepository.findOverStock()).thenReturn(Arrays.asList(testStock1));
        when(entityMapper.mapToDTO(testStock1, StockDTO.class)).thenReturn(testStockDTO1);

        // When
        List<StockDTO> result = stockService.getOverStocks();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUuid()).isEqualTo(stockUuid1);
        verify(stockRepository).findOverStock();
        verify(entityMapper).mapToDTO(testStock1, StockDTO.class);
    }

    @Test
    void testGetZeroStocks_ShouldReturnZeroQuantityStockRecords() {
        // Given
        when(stockRepository.findByQuantity(0)).thenReturn(Arrays.asList(testStock2));
        when(entityMapper.mapToDTO(testStock2, StockDTO.class)).thenReturn(testStockDTO2);

        // When
        List<StockDTO> result = stockService.getZeroStocks();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUuid()).isEqualTo(stockUuid2);
        verify(stockRepository).findByQuantity(0);
        verify(entityMapper).mapToDTO(testStock2, StockDTO.class);
    }

    @Test
    void testGetAllStocks_WhenNoStocks_ShouldReturnEmptyList() {
        // Given
        when(stockRepository.findAllWithProductAndWarehouse()).thenReturn(Arrays.asList());

        // When
        List<StockDTO> result = stockService.getAllStocks();

        // Then
        assertThat(result).isEmpty();
        verify(stockRepository).findAllWithProductAndWarehouse();
        verify(entityMapper, never()).mapToDTO(any(Stock.class), eq(StockDTO.class));
    }

    @Test
    void testGetStocksByProductCode_WhenNoStocks_ShouldReturnEmptyList() {
        // Given
        when(stockRepository.findByProductCode("NONEXISTENT")).thenReturn(Arrays.asList());

        // When
        List<StockDTO> result = stockService.getStocksByProductCode("NONEXISTENT");

        // Then
        assertThat(result).isEmpty();
        verify(stockRepository).findByProductCode("NONEXISTENT");
        verify(entityMapper, never()).mapToDTO(any(Stock.class), eq(StockDTO.class));
    }
}
