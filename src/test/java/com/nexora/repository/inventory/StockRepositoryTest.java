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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class StockRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StockRepository stockRepository;

    private Product testProduct1;
    private Product testProduct2;
    private Warehouse testWarehouse1;
    private Warehouse testWarehouse2;
    private Stock testStock1;
    private Stock testStock2;
    private Stock testStock3;
    private Stock testStock4;

    @BeforeEach
    void setUp() {
        // Create test products
        testProduct1 = new Product();
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
        testProduct2.setCode("PROD002");
        testProduct2.setName("Test Product 2");
        testProduct2.setDescription("Description for test product 2");
        testProduct2.setPrice(new BigDecimal("149.99"));
        testProduct2.setActive(true);
        testProduct2.setCategory("Clothing");
        testProduct2.setBrand("AnotherBrand");
        testProduct2.setSku("SKU002");
        testProduct2.setCreatedAt(LocalDateTime.now());
        testProduct2.setUpdatedAt(LocalDateTime.now());

        // Create test warehouses
        testWarehouse1 = new Warehouse();
        testWarehouse1.setCode("WH001");
        testWarehouse1.setName("Main Distribution Center");
        testWarehouse1.setDescription("Primary distribution center");
        testWarehouse1.setAddress("123 Main St");
        testWarehouse1.setCity("Boston");
        testWarehouse1.setStateProvince("Massachusetts");
        testWarehouse1.setPostalCode("02108");
        testWarehouse1.setCountry("USA");
        testWarehouse1.setActive(true);
        testWarehouse1.setCreatedAt(LocalDateTime.now());
        testWarehouse1.setUpdatedAt(LocalDateTime.now());

        testWarehouse2 = new Warehouse();
        testWarehouse2.setCode("WH002");
        testWarehouse2.setName("Secondary Warehouse");
        testWarehouse2.setDescription("Secondary storage facility");
        testWarehouse2.setAddress("456 Oak Ave");
        testWarehouse2.setCity("New York");
        testWarehouse2.setStateProvince("New York");
        testWarehouse2.setPostalCode("10001");
        testWarehouse2.setCountry("USA");
        testWarehouse2.setActive(true);
        testWarehouse2.setCreatedAt(LocalDateTime.now());
        testWarehouse2.setUpdatedAt(LocalDateTime.now());

        // Persist products and warehouses first
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testProduct2 = entityManager.persistAndFlush(testProduct2);
        testWarehouse1 = entityManager.persistAndFlush(testWarehouse1);
        testWarehouse2 = entityManager.persistAndFlush(testWarehouse2);

        // Create test stock records
        testStock1 = new Stock();
        testStock1.setProduct(testProduct1);
        testStock1.setWarehouse(testWarehouse1);
        testStock1.setQuantity(50);
        testStock1.setMinStockLevel(10);
        testStock1.setMaxStockLevel(100);
        testStock1.setLastRestockDate(LocalDateTime.now().minusDays(5));
        testStock1.setCreatedAt(LocalDateTime.now());
        testStock1.setUpdatedAt(LocalDateTime.now());

        testStock2 = new Stock();
        testStock2.setProduct(testProduct1);
        testStock2.setWarehouse(testWarehouse2);
        testStock2.setQuantity(5); // Low stock
        testStock2.setMinStockLevel(10);
        testStock2.setMaxStockLevel(50);
        testStock2.setLastRestockDate(LocalDateTime.now().minusDays(10));
        testStock2.setCreatedAt(LocalDateTime.now());
        testStock2.setUpdatedAt(LocalDateTime.now());

        testStock3 = new Stock();
        testStock3.setProduct(testProduct2);
        testStock3.setWarehouse(testWarehouse1);
        testStock3.setQuantity(150); // Over stock
        testStock3.setMinStockLevel(20);
        testStock3.setMaxStockLevel(100);
        testStock3.setLastRestockDate(LocalDateTime.now().minusDays(2));
        testStock3.setCreatedAt(LocalDateTime.now());
        testStock3.setUpdatedAt(LocalDateTime.now());

        testStock4 = new Stock();
        testStock4.setProduct(testProduct2);
        testStock4.setWarehouse(testWarehouse2);
        testStock4.setQuantity(0); // Zero stock
        testStock4.setMinStockLevel(5);
        testStock4.setMaxStockLevel(25);
        testStock4.setLastRestockDate(LocalDateTime.now().minusDays(15));
        testStock4.setCreatedAt(LocalDateTime.now());
        testStock4.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testFindByProductAndWarehouse_WhenStockExists_ShouldReturnStock() {
        // Given
        entityManager.persistAndFlush(testStock1);

        // When
        Optional<Stock> foundStock = stockRepository.findByProductAndWarehouse(testProduct1, testWarehouse1);

        // Then
        assertThat(foundStock).isPresent();
        assertThat(foundStock.get().getProduct().getCode()).isEqualTo("PROD001");
        assertThat(foundStock.get().getWarehouse().getCode()).isEqualTo("WH001");
        assertThat(foundStock.get().getQuantity()).isEqualTo(50);
    }

    @Test
    void testFindByProductAndWarehouse_WhenStockDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<Stock> foundStock = stockRepository.findByProductAndWarehouse(testProduct1, testWarehouse1);

        // Then
        assertThat(foundStock).isEmpty();
    }

    @Test
    void testFindByProduct_ShouldReturnAllStockRecordsForProduct() {
        // Given
        entityManager.persistAndFlush(testStock1); // Product1 in Warehouse1
        entityManager.persistAndFlush(testStock2); // Product1 in Warehouse2
        entityManager.persistAndFlush(testStock3); // Product2 in Warehouse1

        // When
        List<Stock> product1Stocks = stockRepository.findByProduct(testProduct1);

        // Then
        assertThat(product1Stocks).hasSize(2);
        assertThat(product1Stocks).allMatch(stock -> stock.getProduct().getCode().equals("PROD001"));
        assertThat(product1Stocks).extracting(stock -> stock.getWarehouse().getCode())
                .containsExactlyInAnyOrder("WH001", "WH002");
    }

    @Test
    void testFindByWarehouse_ShouldReturnAllStockRecordsForWarehouse() {
        // Given
        entityManager.persistAndFlush(testStock1); // Product1 in Warehouse1
        entityManager.persistAndFlush(testStock2); // Product1 in Warehouse2
        entityManager.persistAndFlush(testStock3); // Product2 in Warehouse1

        // When
        List<Stock> warehouse1Stocks = stockRepository.findByWarehouse(testWarehouse1);

        // Then
        assertThat(warehouse1Stocks).hasSize(2);
        assertThat(warehouse1Stocks).allMatch(stock -> stock.getWarehouse().getCode().equals("WH001"));
        assertThat(warehouse1Stocks).extracting(stock -> stock.getProduct().getCode())
                .containsExactlyInAnyOrder("PROD001", "PROD002");
    }

    @Test
    void testFindLowStock_ShouldReturnStockRecordsWithLowQuantity() {
        // Given
        entityManager.persistAndFlush(testStock1); // quantity=50, minLevel=10 (not low)
        entityManager.persistAndFlush(testStock2); // quantity=5, minLevel=10 (low stock)
        entityManager.persistAndFlush(testStock3); // quantity=150, minLevel=20 (not low)
        entityManager.persistAndFlush(testStock4); // quantity=0, minLevel=5 (low stock)

        // When
        List<Stock> lowStocks = stockRepository.findLowStock();

        // Then
        assertThat(lowStocks).hasSize(2);
        assertThat(lowStocks).extracting(Stock::getQuantity)
                .containsExactlyInAnyOrder(5, 0);
        assertThat(lowStocks).allMatch(stock -> stock.getQuantity() <= stock.getMinStockLevel());
    }

    @Test
    void testFindOverStock_ShouldReturnStockRecordsWithExcessiveQuantity() {
        // Given
        entityManager.persistAndFlush(testStock1); // quantity=50, maxLevel=100 (not over)
        entityManager.persistAndFlush(testStock2); // quantity=5, maxLevel=50 (not over)
        entityManager.persistAndFlush(testStock3); // quantity=150, maxLevel=100 (over stock)
        entityManager.persistAndFlush(testStock4); // quantity=0, maxLevel=25 (not over)

        // When
        List<Stock> overStocks = stockRepository.findOverStock();

        // Then
        assertThat(overStocks).hasSize(1);
        assertThat(overStocks.get(0).getQuantity()).isEqualTo(150);
        assertThat(overStocks.get(0).getMaxStockLevel()).isEqualTo(100);
        assertThat(overStocks).allMatch(stock -> 
                stock.getMaxStockLevel() != null && stock.getQuantity() >= stock.getMaxStockLevel());
    }

    @Test
    void testFindByProductCode_ShouldReturnStockRecordsForProductWithSpecifiedCode() {
        // Given
        entityManager.persistAndFlush(testStock1); // Product1 (PROD001) in Warehouse1
        entityManager.persistAndFlush(testStock2); // Product1 (PROD001) in Warehouse2
        entityManager.persistAndFlush(testStock3); // Product2 (PROD002) in Warehouse1

        // When
        List<Stock> product1Stocks = stockRepository.findByProductCode("PROD001");

        // Then
        assertThat(product1Stocks).hasSize(2);
        assertThat(product1Stocks).allMatch(stock -> stock.getProduct().getCode().equals("PROD001"));
        assertThat(product1Stocks).extracting(stock -> stock.getWarehouse().getCode())
                .containsExactlyInAnyOrder("WH001", "WH002");
    }

    @Test
    void testFindByWarehouseCode_ShouldReturnStockRecordsForWarehouseWithSpecifiedCode() {
        // Given
        entityManager.persistAndFlush(testStock1); // Product1 in Warehouse1 (WH001)
        entityManager.persistAndFlush(testStock2); // Product1 in Warehouse2 (WH002)
        entityManager.persistAndFlush(testStock3); // Product2 in Warehouse1 (WH001)

        // When
        List<Stock> warehouse1Stocks = stockRepository.findByWarehouseCode("WH001");

        // Then
        assertThat(warehouse1Stocks).hasSize(2);
        assertThat(warehouse1Stocks).allMatch(stock -> stock.getWarehouse().getCode().equals("WH001"));
        assertThat(warehouse1Stocks).extracting(stock -> stock.getProduct().getCode())
                .containsExactlyInAnyOrder("PROD001", "PROD002");
    }

    @Test
    void testFindByQuantity_ShouldReturnStockRecordsWithSpecifiedQuantity() {
        // Given
        entityManager.persistAndFlush(testStock1); // quantity=50
        entityManager.persistAndFlush(testStock2); // quantity=5
        entityManager.persistAndFlush(testStock3); // quantity=150
        entityManager.persistAndFlush(testStock4); // quantity=0

        // When
        List<Stock> zeroQuantityStocks = stockRepository.findByQuantity(0);

        // Then
        assertThat(zeroQuantityStocks).hasSize(1);
        assertThat(zeroQuantityStocks.get(0).getQuantity()).isEqualTo(0);
        assertThat(zeroQuantityStocks.get(0).getProduct().getCode()).isEqualTo("PROD002");
        assertThat(zeroQuantityStocks.get(0).getWarehouse().getCode()).isEqualTo("WH002");
    }

    @Test
    void testFindAllWithProductAndWarehouse_ShouldReturnStockRecordsWithFetchedRelations() {
        // Given
        entityManager.persistAndFlush(testStock1);
        entityManager.persistAndFlush(testStock2);
        entityManager.persistAndFlush(testStock3);

        // When
        List<Stock> allStocks = stockRepository.findAllWithProductAndWarehouse();

        // Then
        assertThat(allStocks).hasSize(3);
        // Verify that product and warehouse are fetched (no lazy loading exceptions)
        assertThat(allStocks).allMatch(stock -> stock.getProduct() != null);
        assertThat(allStocks).allMatch(stock -> stock.getWarehouse() != null);
        assertThat(allStocks).allMatch(stock -> stock.getProduct().getName() != null);
        assertThat(allStocks).allMatch(stock -> stock.getWarehouse().getName() != null);
    }

    @Test
    void testSaveStock_ShouldPersistStock() {
        // When
        Stock savedStock = stockRepository.save(testStock1);

        // Then
        assertThat(savedStock.getUuid()).isNotNull();
        assertThat(savedStock.getProduct().getCode()).isEqualTo("PROD001");
        assertThat(savedStock.getWarehouse().getCode()).isEqualTo("WH001");
        assertThat(savedStock.getQuantity()).isEqualTo(50);

        // Verify it's actually persisted
        Stock foundStock = entityManager.find(Stock.class, savedStock.getUuid());
        assertThat(foundStock).isNotNull();
        assertThat(foundStock.getQuantity()).isEqualTo(50);
    }

    @Test
    void testDeleteStock_ShouldRemoveStock() {
        // Given
        Stock savedStock = entityManager.persistAndFlush(testStock1);

        // When
        stockRepository.deleteById(savedStock.getUuid());
        entityManager.flush();

        // Then
        Stock deletedStock = entityManager.find(Stock.class, savedStock.getUuid());
        assertThat(deletedStock).isNull();
    }

    @Test
    void testUniqueConstraint_ShouldEnforceUniqueProductWarehouseCombination() {
        // Given
        entityManager.persistAndFlush(testStock1);

        Stock duplicateStock = new Stock();
        duplicateStock.setProduct(testProduct1); // Same product
        duplicateStock.setWarehouse(testWarehouse1); // Same warehouse
        duplicateStock.setQuantity(100);
        duplicateStock.setMinStockLevel(5);

        // When & Then
        try {
            entityManager.persistAndFlush(duplicateStock);
            entityManager.flush();
            // If we reach here, the test should fail
            assertThat(false).as("Expected constraint violation for duplicate product-warehouse combination").isTrue();
        } catch (Exception e) {
            // Expected behavior - constraint violation
            assertThat(e.getMessage()).contains("could not execute statement");
        }
    }

    @Test
    void testFindAll_ShouldReturnAllStockRecords() {
        // Given
        entityManager.persistAndFlush(testStock1);
        entityManager.persistAndFlush(testStock2);
        entityManager.persistAndFlush(testStock3);
        entityManager.persistAndFlush(testStock4);

        // When
        List<Stock> allStocks = stockRepository.findAll();

        // Then
        assertThat(allStocks).hasSize(4);
        assertThat(allStocks).extracting(Stock::getQuantity)
                .containsExactlyInAnyOrder(50, 5, 150, 0);
    }

    @Test
    void testFindByProductCode_WithNonExistentCode_ShouldReturnEmptyList() {
        // Given
        entityManager.persistAndFlush(testStock1);

        // When
        List<Stock> stocks = stockRepository.findByProductCode("NONEXISTENT");

        // Then
        assertThat(stocks).isEmpty();
    }

    @Test
    void testFindByWarehouseCode_WithNonExistentCode_ShouldReturnEmptyList() {
        // Given
        entityManager.persistAndFlush(testStock1);

        // When
        List<Stock> stocks = stockRepository.findByWarehouseCode("NONEXISTENT");

        // Then
        assertThat(stocks).isEmpty();
    }

    @Test
    void testFindByQuantity_WithNonExistentQuantity_ShouldReturnEmptyList() {
        // Given
        entityManager.persistAndFlush(testStock1); // quantity=50

        // When
        List<Stock> stocks = stockRepository.findByQuantity(999);

        // Then
        assertThat(stocks).isEmpty();
    }

    @Test
    void testStockBusinessLogic_IsLowStock_ShouldReturnCorrectValue() {
        // Given
        entityManager.persistAndFlush(testStock2); // quantity=5, minLevel=10

        // When
        Stock stock = stockRepository.findById(testStock2.getUuid()).orElseThrow();

        // Then
        assertThat(stock.isLowStock()).isTrue();
    }

    @Test
    void testStockBusinessLogic_IsOverStock_ShouldReturnCorrectValue() {
        // Given
        entityManager.persistAndFlush(testStock3); // quantity=150, maxLevel=100

        // When
        Stock stock = stockRepository.findById(testStock3.getUuid()).orElseThrow();

        // Then
        assertThat(stock.isOverStock()).isTrue();
    }
}