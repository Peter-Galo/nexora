package com.nexora.service.inventory.impl;

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
import com.nexora.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the StockServiceImpl class.
 */
@ExtendWith(MockitoExtension.class)
public class StockServiceImplTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private StockServiceImpl stockService;

    private Product product;
    private Warehouse warehouse;
    private Stock stock;
    private ProductDTO productDTO;
    private WarehouseDTO warehouseDTO;
    private StockDTO stockDTO;

    @BeforeEach
    public void setup() {
        // Create test data
        product = TestDataFactory.createSampleProduct(1L);
        warehouse = TestDataFactory.createSampleWarehouse(1L);
        stock = TestDataFactory.createSampleStock(1L, product, warehouse);

        productDTO = TestDataFactory.createSampleProductDTO(1L);
        warehouseDTO = TestDataFactory.createSampleWarehouseDTO(1L);
        stockDTO = TestDataFactory.createSampleStockDTO(null, productDTO, warehouseDTO);
    }

    @Test
    public void testGetAllStocks() {
        // Arrange
        when(stockRepository.findAll()).thenReturn(List.of(stock));

        // Act
        List<StockDTO> result = stockService.getAllStocks();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProduct().getId()).isEqualTo(product.getId());
        assertThat(result.get(0).getWarehouse().getId()).isEqualTo(warehouse.getId());
        verify(stockRepository, times(1)).findAll();
    }

    @Test
    public void testGetStockById_Success() {
        // Arrange
        when(stockRepository.findById(anyLong())).thenReturn(Optional.of(stock));

        // Act
        StockDTO result = stockService.getStockById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(stock.getId());
        verify(stockRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetStockById_NotFound() {
        // Arrange
        when(stockRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApplicationException.class, () -> stockService.getStockById(1L));
        verify(stockRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetStockByProductAndWarehouse_Success() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(anyLong())).thenReturn(Optional.of(warehouse));
        when(stockRepository.findByProductAndWarehouse(any(Product.class), any(Warehouse.class)))
                .thenReturn(Optional.of(stock));

        // Act
        StockDTO result = stockService.getStockByProductAndWarehouse(1L, 1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProduct().getId()).isEqualTo(product.getId());
        assertThat(result.getWarehouse().getId()).isEqualTo(warehouse.getId());
        verify(productRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).findById(1L);
        verify(stockRepository, times(1)).findByProductAndWarehouse(product, warehouse);
    }

    @Test
    public void testGetStockByProductAndWarehouse_ProductNotFound() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApplicationException.class, () -> stockService.getStockByProductAndWarehouse(1L, 1L));
        verify(productRepository, times(1)).findById(1L);
        verify(warehouseRepository, never()).findById(anyLong());
        verify(stockRepository, never()).findByProductAndWarehouse(any(), any());
    }

    @Test
    public void testGetStockByProductAndWarehouse_WarehouseNotFound() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApplicationException.class, () -> stockService.getStockByProductAndWarehouse(1L, 1L));
        verify(productRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).findById(1L);
        verify(stockRepository, never()).findByProductAndWarehouse(any(), any());
    }

    @Test
    public void testGetStockByProductAndWarehouse_StockNotFound() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(anyLong())).thenReturn(Optional.of(warehouse));
        when(stockRepository.findByProductAndWarehouse(any(Product.class), any(Warehouse.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApplicationException.class, () -> stockService.getStockByProductAndWarehouse(1L, 1L));
        verify(productRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).findById(1L);
        verify(stockRepository, times(1)).findByProductAndWarehouse(product, warehouse);
    }

    @Test
    public void testGetStocksByProductId() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(stockRepository.findByProduct(any(Product.class))).thenReturn(List.of(stock));

        // Act
        List<StockDTO> result = stockService.getStocksByProductId(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProduct().getId()).isEqualTo(product.getId());
        verify(productRepository, times(1)).findById(1L);
        verify(stockRepository, times(1)).findByProduct(product);
    }

    @Test
    public void testGetStocksByWarehouseId() {
        // Arrange
        when(warehouseRepository.findById(anyLong())).thenReturn(Optional.of(warehouse));
        when(stockRepository.findByWarehouse(any(Warehouse.class))).thenReturn(List.of(stock));

        // Act
        List<StockDTO> result = stockService.getStocksByWarehouseId(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWarehouse().getId()).isEqualTo(warehouse.getId());
        verify(warehouseRepository, times(1)).findById(1L);
        verify(stockRepository, times(1)).findByWarehouse(warehouse);
    }

    @Test
    public void testGetStocksByProductCode() {
        // Arrange
        when(stockRepository.findByProductCode(anyString())).thenReturn(List.of(stock));

        // Act
        List<StockDTO> result = stockService.getStocksByProductCode("TEST-PROD-1");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProduct().getId()).isEqualTo(product.getId());
        verify(stockRepository, times(1)).findByProductCode("TEST-PROD-1");
    }

    @Test
    public void testGetStocksByWarehouseCode() {
        // Arrange
        when(stockRepository.findByWarehouseCode(anyString())).thenReturn(List.of(stock));

        // Act
        List<StockDTO> result = stockService.getStocksByWarehouseCode("TEST-WH-1");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWarehouse().getId()).isEqualTo(warehouse.getId());
        verify(stockRepository, times(1)).findByWarehouseCode("TEST-WH-1");
    }

    @Test
    public void testGetLowStocks() {
        // Arrange
        when(stockRepository.findLowStock()).thenReturn(List.of(stock));

        // Act
        List<StockDTO> result = stockService.getLowStocks();

        // Assert
        assertThat(result).hasSize(1);
        verify(stockRepository, times(1)).findLowStock();
    }

    @Test
    public void testGetOverStocks() {
        // Arrange
        when(stockRepository.findOverStock()).thenReturn(List.of(stock));

        // Act
        List<StockDTO> result = stockService.getOverStocks();

        // Assert
        assertThat(result).hasSize(1);
        verify(stockRepository, times(1)).findOverStock();
    }

    @Test
    public void testGetZeroStocks() {
        // Arrange
        when(stockRepository.findByQuantity(0)).thenReturn(List.of(stock));

        // Act
        List<StockDTO> result = stockService.getZeroStocks();

        // Assert
        assertThat(result).hasSize(1);
        verify(stockRepository, times(1)).findByQuantity(0);
    }

    @Test
    public void testCreateStock_Success() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(anyLong())).thenReturn(Optional.of(warehouse));
        when(stockRepository.findByProductAndWarehouse(any(Product.class), any(Warehouse.class)))
                .thenReturn(Optional.empty());
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);

        // Act
        StockDTO result = stockService.createStock(stockDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(stock.getId());
        verify(productRepository, times(1)).findById(stockDTO.getProduct().getId());
        verify(warehouseRepository, times(1)).findById(stockDTO.getWarehouse().getId());
        verify(stockRepository, times(1)).findByProductAndWarehouse(product, warehouse);
        verify(stockRepository, times(1)).save(any(Stock.class));
    }

    @Test
    public void testCreateStock_AlreadyExists() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(anyLong())).thenReturn(Optional.of(warehouse));
        when(stockRepository.findByProductAndWarehouse(any(Product.class), any(Warehouse.class)))
                .thenReturn(Optional.of(stock));

        // Act & Assert
        assertThrows(ApplicationException.class, () -> stockService.createStock(stockDTO));
        verify(productRepository, times(1)).findById(stockDTO.getProduct().getId());
        verify(warehouseRepository, times(1)).findById(stockDTO.getWarehouse().getId());
        verify(stockRepository, times(1)).findByProductAndWarehouse(product, warehouse);
        verify(stockRepository, never()).save(any(Stock.class));
    }

    @Test
    public void testUpdateStock_Success() {
        // Arrange
        when(stockRepository.findById(anyLong())).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(anyLong())).thenReturn(Optional.of(warehouse));

        // Create a DTO with updated values
        StockDTO updateDTO = new StockDTO();
        updateDTO.setId(1L);
        updateDTO.setProduct(productDTO);
        updateDTO.setWarehouse(warehouseDTO);
        updateDTO.setQuantity(150);
        updateDTO.setMinStockLevel(20);
        updateDTO.setMaxStockLevel(300);

        // Act
        StockDTO result = stockService.updateStock(1L, updateDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(stock.getId());
        verify(stockRepository, times(1)).findById(1L);
        verify(stockRepository, times(1)).save(any(Stock.class));
    }

    @Test
    public void testUpdateStock_NotFound() {
        // Arrange
        when(stockRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApplicationException.class, () -> stockService.updateStock(1L, stockDTO));
        verify(stockRepository, times(1)).findById(1L);
        verify(stockRepository, never()).save(any(Stock.class));
    }

    @Test
    public void testDeleteStock_Success() {
        // Arrange
        when(stockRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(stockRepository).deleteById(anyLong());

        // Act
        stockService.deleteStock(1L);

        // Assert
        verify(stockRepository, times(1)).existsById(1L);
        verify(stockRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteStock_NotFound() {
        // Arrange
        when(stockRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        assertThrows(ApplicationException.class, () -> stockService.deleteStock(1L));
        verify(stockRepository, times(1)).existsById(1L);
        verify(stockRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testAddStock_Success() {
        // Arrange
        when(stockRepository.findById(anyLong())).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);

        // Act
        StockDTO result = stockService.addStock(1L, 50);

        // Assert
        assertThat(result).isNotNull();
        verify(stockRepository, times(1)).findById(1L);
        verify(stockRepository, times(1)).save(any(Stock.class));
    }

    @Test
    public void testAddStock_NotFound() {
        // Arrange
        when(stockRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApplicationException.class, () -> stockService.addStock(1L, 50));
        verify(stockRepository, times(1)).findById(1L);
        verify(stockRepository, never()).save(any(Stock.class));
    }

    @Test
    public void testRemoveStock_Success() {
        // Arrange
        stock.setQuantity(100);
        when(stockRepository.findById(anyLong())).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);

        // Act
        StockDTO result = stockService.removeStock(1L, 50);

        // Assert
        assertThat(result).isNotNull();
        verify(stockRepository, times(1)).findById(1L);
        verify(stockRepository, times(1)).save(any(Stock.class));
    }

    @Test
    public void testRemoveStock_NotFound() {
        // Arrange
        when(stockRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApplicationException.class, () -> stockService.removeStock(1L, 50));
        verify(stockRepository, times(1)).findById(1L);
        verify(stockRepository, never()).save(any(Stock.class));
    }

    @Test
    public void testRemoveStock_InsufficientStock() {
        // Arrange
        stock.setQuantity(30);
        when(stockRepository.findById(anyLong())).thenReturn(Optional.of(stock));

        // Act & Assert
        assertThrows(ApplicationException.class, () -> stockService.removeStock(1L, 50));
        verify(stockRepository, times(1)).findById(1L);
        verify(stockRepository, never()).save(any(Stock.class));
    }
}
