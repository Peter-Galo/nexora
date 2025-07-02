package com.nexora.repository.inventory;

import com.nexora.model.inventory.Product;
import com.nexora.model.inventory.Stock;
import com.nexora.model.inventory.Warehouse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class StockRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StockRepository stockRepository;

    private Product product1;
    private Product product2;
    private Product product3;
    private Warehouse warehouse1;
    private Warehouse warehouse2;
    private Stock stock1;
    private Stock stock2;
    private Stock stock3;
    private Stock stock4;
    private Stock stock5;

    @BeforeEach
    void setUp() {
        // Create test products
        product1 = new Product("P001", "Laptop", new BigDecimal("999.99"));
        product1.setDescription("High-performance laptop");
        product1.setCategory("Electronics");
        product1.setBrand("TechBrand");
        product1.setSku("SKU001");
        product1.setActive(true);

        product2 = new Product("P002", "Smartphone", new BigDecimal("499.99"));
        product2.setDescription("Latest smartphone model");
        product2.setCategory("Electronics");
        product2.setBrand("MobileBrand");
        product2.setSku("SKU002");
        product2.setActive(true);

        product3 = new Product("P003", "Headphones", new BigDecimal("99.99"));
        product3.setDescription("Noise-cancelling headphones");
        product3.setCategory("Accessories");
        product3.setBrand("AudioBrand");
        product3.setSku("SKU003");
        product3.setActive(true);

        // Create test warehouses
        warehouse1 = new Warehouse("WH001", "Main Warehouse");
        warehouse1.setDescription("Main distribution center");
        warehouse1.setAddress("123 Main St");
        warehouse1.setCity("New York");
        warehouse1.setStateProvince("NY");
        warehouse1.setPostalCode("10001");
        warehouse1.setCountry("USA");
        warehouse1.setActive(true);

        warehouse2 = new Warehouse("WH002", "Secondary Warehouse");
        warehouse2.setDescription("Secondary distribution center");
        warehouse2.setAddress("456 Second St");
        warehouse2.setCity("Los Angeles");
        warehouse2.setStateProvince("CA");
        warehouse2.setPostalCode("90001");
        warehouse2.setCountry("USA");
        warehouse2.setActive(true);

        // Persist products and warehouses
        entityManager.persist(product1);
        entityManager.persist(product2);
        entityManager.persist(product3);
        entityManager.persist(warehouse1);
        entityManager.persist(warehouse2);
        entityManager.flush();

        // Create test stock records
        // Stock with normal quantity
        stock1 = new Stock(product1, warehouse1, 100);
        stock1.setMinStockLevel(20);
        stock1.setMaxStockLevel(150);
        stock1.setLastRestockDate(LocalDateTime.now().minusDays(10));

        // Stock with low quantity (below min stock level)
        stock2 = new Stock(product2, warehouse1, 5);
        stock2.setMinStockLevel(10);
        stock2.setMaxStockLevel(100);
        stock2.setLastRestockDate(LocalDateTime.now().minusDays(30));

        // Stock with over quantity (above max stock level)
        stock3 = new Stock(product3, warehouse1, 200);
        stock3.setMinStockLevel(20);
        stock3.setMaxStockLevel(150);
        stock3.setLastRestockDate(LocalDateTime.now().minusDays(5));

        // Stock with zero quantity
        stock4 = new Stock(product1, warehouse2, 0);
        stock4.setMinStockLevel(10);
        stock4.setMaxStockLevel(100);
        stock4.setLastRestockDate(LocalDateTime.now().minusDays(45));

        // Stock with null max stock level (should not be in over stock)
        stock5 = new Stock(product2, warehouse2, 50);
        stock5.setMinStockLevel(10);
        stock5.setMaxStockLevel(null);
        stock5.setLastRestockDate(LocalDateTime.now().minusDays(15));

        // Persist stock records
        entityManager.persist(stock1);
        entityManager.persist(stock2);
        entityManager.persist(stock3);
        entityManager.persist(stock4);
        entityManager.persist(stock5);
        entityManager.flush();
    }

    @Test
    void findByProductAndWarehouse_shouldReturnStock_whenExists() {
        // Act
        Optional<Stock> foundStock = stockRepository.findByProductAndWarehouse(product1, warehouse1);

        // Assert
        assertTrue(foundStock.isPresent());
        assertEquals(stock1.getUuid(), foundStock.get().getUuid());
        assertEquals(100, foundStock.get().getQuantity());
    }

    @Test
    void findByProductAndWarehouse_shouldReturnEmpty_whenDoesNotExist() {
        // Act
        Optional<Stock> foundStock = stockRepository.findByProductAndWarehouse(product3, warehouse2);

        // Assert
        assertFalse(foundStock.isPresent());
    }

    @Test
    void findByProduct_shouldReturnAllStockForProduct() {
        // Act
        List<Stock> product1Stocks = stockRepository.findByProduct(product1);
        List<Stock> product2Stocks = stockRepository.findByProduct(product2);
        List<Stock> product3Stocks = stockRepository.findByProduct(product3);

        // Assert
        assertEquals(2, product1Stocks.size());
        assertEquals(2, product2Stocks.size());
        assertEquals(1, product3Stocks.size());

        assertTrue(product1Stocks.stream().anyMatch(s -> s.getWarehouse().getCode().equals("WH001")));
        assertTrue(product1Stocks.stream().anyMatch(s -> s.getWarehouse().getCode().equals("WH002")));
    }

    @Test
    void findByWarehouse_shouldReturnAllStockInWarehouse() {
        // Act
        List<Stock> warehouse1Stocks = stockRepository.findByWarehouse(warehouse1);
        List<Stock> warehouse2Stocks = stockRepository.findByWarehouse(warehouse2);

        // Assert
        assertEquals(3, warehouse1Stocks.size());
        assertEquals(2, warehouse2Stocks.size());

        assertTrue(warehouse1Stocks.stream().anyMatch(s -> s.getProduct().getCode().equals("P001")));
        assertTrue(warehouse1Stocks.stream().anyMatch(s -> s.getProduct().getCode().equals("P002")));
        assertTrue(warehouse1Stocks.stream().anyMatch(s -> s.getProduct().getCode().equals("P003")));
    }

    @Test
    void findLowStock_shouldReturnStockBelowMinLevel() {
        // Act
        List<Stock> lowStocks = stockRepository.findLowStock();

        // Assert
        assertEquals(2, lowStocks.size());
        assertTrue(lowStocks.stream().anyMatch(s -> s.getUuid().equals(stock2.getUuid())));
        assertTrue(lowStocks.stream().anyMatch(s -> s.getUuid().equals(stock4.getUuid())));
    }

    @Test
    void findOverStock_shouldReturnStockAboveMaxLevel() {
        // Act
        List<Stock> overStocks = stockRepository.findOverStock();

        // Assert
        assertEquals(1, overStocks.size());
        assertEquals(stock3.getUuid(), overStocks.get(0).getUuid());
    }

    @Test
    void findByProductCode_shouldReturnAllStockForProductCode() {
        // Act
        List<Stock> p001Stocks = stockRepository.findByProductCode("P001");
        List<Stock> p002Stocks = stockRepository.findByProductCode("P002");
        List<Stock> nonExistentStocks = stockRepository.findByProductCode("NONEXISTENT");

        // Assert
        assertEquals(2, p001Stocks.size());
        assertEquals(2, p002Stocks.size());
        assertEquals(0, nonExistentStocks.size());

        assertTrue(p001Stocks.stream().anyMatch(s -> s.getWarehouse().getCode().equals("WH001")));
        assertTrue(p001Stocks.stream().anyMatch(s -> s.getWarehouse().getCode().equals("WH002")));
    }

    @Test
    void findByWarehouseCode_shouldReturnAllStockInWarehouseCode() {
        // Act
        List<Stock> wh001Stocks = stockRepository.findByWarehouseCode("WH001");
        List<Stock> wh002Stocks = stockRepository.findByWarehouseCode("WH002");
        List<Stock> nonExistentStocks = stockRepository.findByWarehouseCode("NONEXISTENT");

        // Assert
        assertEquals(3, wh001Stocks.size());
        assertEquals(2, wh002Stocks.size());
        assertEquals(0, nonExistentStocks.size());

        assertTrue(wh001Stocks.stream().anyMatch(s -> s.getProduct().getCode().equals("P001")));
        assertTrue(wh001Stocks.stream().anyMatch(s -> s.getProduct().getCode().equals("P002")));
        assertTrue(wh001Stocks.stream().anyMatch(s -> s.getProduct().getCode().equals("P003")));
    }

    @Test
    void findByQuantity_shouldReturnStockWithSpecificQuantity() {
        // Act
        List<Stock> zeroQuantityStocks = stockRepository.findByQuantity(0);
        List<Stock> hundredQuantityStocks = stockRepository.findByQuantity(100);
        List<Stock> nonExistentQuantityStocks = stockRepository.findByQuantity(999);

        // Assert
        assertEquals(1, zeroQuantityStocks.size());
        assertEquals(1, hundredQuantityStocks.size());
        assertEquals(0, nonExistentQuantityStocks.size());

        assertEquals(stock4.getUuid(), zeroQuantityStocks.get(0).getUuid());
        assertEquals(stock1.getUuid(), hundredQuantityStocks.get(0).getUuid());
    }
}