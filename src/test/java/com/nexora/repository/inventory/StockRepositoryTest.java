package com.nexora.repository.inventory;

import com.nexora.model.inventory.Product;
import com.nexora.model.inventory.Stock;
import com.nexora.model.inventory.Warehouse;
import com.nexora.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the StockRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
public class StockRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StockRepository stockRepository;

    private Product persistProduct() {
        Product product = TestDataFactory.createSampleProduct(null);
        entityManager.persist(product);
        return product;
    }

    private Warehouse persistWarehouse() {
        Warehouse warehouse = TestDataFactory.createSampleWarehouse(null);
        entityManager.persist(warehouse);
        return warehouse;
    }

    @Test
    public void testSaveStock() {
        // Create and persist a product and warehouse
        Product product = persistProduct();
        Warehouse warehouse = persistWarehouse();

        // Create a sample stock
        Stock stock = TestDataFactory.createSampleStock(null, product, warehouse);

        // Save the stock
        Stock savedStock = stockRepository.save(stock);

        // Flush the changes to the database
        entityManager.flush();

        // Clear the persistence context to force a fetch from the database
        entityManager.clear();

        // Retrieve the stock from the database
        Stock retrievedStock = entityManager.find(Stock.class, savedStock.getId());

        // Verify the stock was saved correctly
        assertThat(retrievedStock).isNotNull();
        assertThat(retrievedStock.getProduct().getId()).isEqualTo(product.getId());
        assertThat(retrievedStock.getWarehouse().getId()).isEqualTo(warehouse.getId());
        assertThat(retrievedStock.getQuantity()).isEqualTo(stock.getQuantity());
        assertThat(retrievedStock.getMinStockLevel()).isEqualTo(stock.getMinStockLevel());
        assertThat(retrievedStock.getMaxStockLevel()).isEqualTo(stock.getMaxStockLevel());
    }

    @Test
    public void testFindByProduct() {
        // Create and persist products, warehouses, and stocks
        Product product1 = persistProduct();
        Product product2 = persistProduct();

        Warehouse warehouse1 = persistWarehouse();
        Warehouse warehouse2 = persistWarehouse();

        Stock stock1 = TestDataFactory.createSampleStock(null, product1, warehouse1);
        Stock stock2 = TestDataFactory.createSampleStock(null, product1, warehouse2);
        Stock stock3 = TestDataFactory.createSampleStock(null, product2, warehouse1);

        entityManager.persist(stock1);
        entityManager.persist(stock2);
        entityManager.persist(stock3);
        entityManager.flush();

        // Find stocks by product
        List<Stock> product1Stocks = stockRepository.findByProduct(product1);

        // Verify correct stocks were returned
        assertThat(product1Stocks).hasSize(2);
        assertThat(product1Stocks).extracting(Stock::getProduct)
                                 .containsOnly(product1);
    }

    @Test
    public void testFindByWarehouse() {
        // Create and persist products, warehouses, and stocks
        Product product1 = persistProduct();
        Product product2 = persistProduct();

        Warehouse warehouse1 = persistWarehouse();
        Warehouse warehouse2 = persistWarehouse();

        Stock stock1 = TestDataFactory.createSampleStock(null, product1, warehouse1);
        Stock stock2 = TestDataFactory.createSampleStock(null, product2, warehouse1);
        Stock stock3 = TestDataFactory.createSampleStock(null, product1, warehouse2);

        entityManager.persist(stock1);
        entityManager.persist(stock2);
        entityManager.persist(stock3);
        entityManager.flush();

        // Find stocks by warehouse
        List<Stock> warehouse1Stocks = stockRepository.findByWarehouse(warehouse1);

        // Verify correct stocks were returned
        assertThat(warehouse1Stocks).hasSize(2);
        assertThat(warehouse1Stocks).extracting(Stock::getWarehouse)
                                   .containsOnly(warehouse1);
    }

    @Test
    public void testFindByProductAndWarehouse() {
        // Create and persist products, warehouses, and stocks
        Product product1 = persistProduct();
        Product product2 = persistProduct();

        Warehouse warehouse1 = persistWarehouse();
        Warehouse warehouse2 = persistWarehouse();

        Stock stock1 = TestDataFactory.createSampleStock(null, product1, warehouse1);
        Stock stock2 = TestDataFactory.createSampleStock(null, product2, warehouse1);
        Stock stock3 = TestDataFactory.createSampleStock(null, product1, warehouse2);

        entityManager.persist(stock1);
        entityManager.persist(stock2);
        entityManager.persist(stock3);
        entityManager.flush();

        // Find stock by product and warehouse
        Optional<Stock> foundStock = stockRepository.findByProductAndWarehouse(product1, warehouse1);

        // Verify the stock was found
        assertThat(foundStock).isPresent();
        assertThat(foundStock.get().getProduct()).isEqualTo(product1);
        assertThat(foundStock.get().getWarehouse()).isEqualTo(warehouse1);
    }

    @Test
    public void testFindLowStock() {
        // Create and persist products, warehouses, and stocks
        Product product1 = persistProduct();
        Product product2 = persistProduct();

        Warehouse warehouse1 = persistWarehouse();
        Warehouse warehouse2 = persistWarehouse();

        // Stock with quantity equal to minStockLevel
        Stock stock1 = TestDataFactory.createSampleStock(null, product1, warehouse1);
        stock1.setQuantity(10);
        stock1.setMinStockLevel(10);

        // Stock with quantity less than minStockLevel
        Stock stock2 = TestDataFactory.createSampleStock(null, product2, warehouse1);
        stock2.setQuantity(5);
        stock2.setMinStockLevel(10);

        // Stock with quantity greater than minStockLevel
        Stock stock3 = TestDataFactory.createSampleStock(null, product1, warehouse2);
        stock3.setQuantity(20);
        stock3.setMinStockLevel(10);

        entityManager.persist(stock1);
        entityManager.persist(stock2);
        entityManager.persist(stock3);
        entityManager.flush();

        // Find stocks with low quantity
        List<Stock> lowStocks = stockRepository.findLowStock();

        // Verify correct stocks were returned
        assertThat(lowStocks).hasSize(2);
        assertThat(lowStocks).extracting(Stock::getId)
                            .containsExactlyInAnyOrder(stock1.getId(), stock2.getId());
    }

    @Test
    public void testFindOverStock() {
        // Create and persist products, warehouses, and stocks
        Product product1 = persistProduct();
        Product product2 = persistProduct();

        Warehouse warehouse1 = persistWarehouse();
        Warehouse warehouse2 = persistWarehouse();

        // Stock with quantity equal to maxStockLevel
        Stock stock1 = TestDataFactory.createSampleStock(null, product1, warehouse1);
        stock1.setQuantity(200);
        stock1.setMaxStockLevel(200);

        // Stock with quantity greater than maxStockLevel
        Stock stock2 = TestDataFactory.createSampleStock(null, product2, warehouse1);
        stock2.setQuantity(250);
        stock2.setMaxStockLevel(200);

        // Stock with quantity less than maxStockLevel
        Stock stock3 = TestDataFactory.createSampleStock(null, product1, warehouse2);
        stock3.setQuantity(150);
        stock3.setMaxStockLevel(200);

        entityManager.persist(stock1);
        entityManager.persist(stock2);
        entityManager.persist(stock3);
        entityManager.flush();

        // Find stocks with high quantity
        List<Stock> highStocks = stockRepository.findOverStock();

        // Verify correct stocks were returned
        assertThat(highStocks).hasSize(2);
        assertThat(highStocks).extracting(Stock::getId)
                             .containsExactlyInAnyOrder(stock1.getId(), stock2.getId());
    }

    @Test
    public void testFindByProductCode() {
        // Create and persist products, warehouses, and stocks
        Product product1 = persistProduct();
        product1.setCode("PROD-001");
        entityManager.persist(product1);

        Product product2 = persistProduct();
        product2.setCode("PROD-002");
        entityManager.persist(product2);

        Warehouse warehouse1 = persistWarehouse();
        Warehouse warehouse2 = persistWarehouse();

        Stock stock1 = TestDataFactory.createSampleStock(null, product1, warehouse1);
        Stock stock2 = TestDataFactory.createSampleStock(null, product1, warehouse2);
        Stock stock3 = TestDataFactory.createSampleStock(null, product2, warehouse1);

        entityManager.persist(stock1);
        entityManager.persist(stock2);
        entityManager.persist(stock3);
        entityManager.flush();

        // Find stocks by product code
        List<Stock> product1Stocks = stockRepository.findByProductCode("PROD-001");

        // Verify correct stocks were returned
        assertThat(product1Stocks).hasSize(2);
        assertThat(product1Stocks).extracting(s -> s.getProduct().getCode())
                                 .containsOnly("PROD-001");
    }

    @Test
    public void testFindByWarehouseCode() {
        // Create and persist products, warehouses, and stocks
        Product product1 = persistProduct();
        Product product2 = persistProduct();

        Warehouse warehouse1 = persistWarehouse();
        warehouse1.setCode("WH-001");
        entityManager.persist(warehouse1);

        Warehouse warehouse2 = persistWarehouse();
        warehouse2.setCode("WH-002");
        entityManager.persist(warehouse2);

        Stock stock1 = TestDataFactory.createSampleStock(null, product1, warehouse1);
        Stock stock2 = TestDataFactory.createSampleStock(null, product2, warehouse1);
        Stock stock3 = TestDataFactory.createSampleStock(null, product1, warehouse2);

        entityManager.persist(stock1);
        entityManager.persist(stock2);
        entityManager.persist(stock3);
        entityManager.flush();

        // Find stocks by warehouse code
        List<Stock> warehouse1Stocks = stockRepository.findByWarehouseCode("WH-001");

        // Verify correct stocks were returned
        assertThat(warehouse1Stocks).hasSize(2);
        assertThat(warehouse1Stocks).extracting(s -> s.getWarehouse().getCode())
                                   .containsOnly("WH-001");
    }

    @Test
    public void testFindByQuantity() {
        // Create and persist products, warehouses, and stocks
        Product product1 = persistProduct();
        Product product2 = persistProduct();
        Product product3 = persistProduct();

        Warehouse warehouse1 = persistWarehouse();
        Warehouse warehouse2 = persistWarehouse();

        // Stock with zero quantity
        Stock stock1 = TestDataFactory.createSampleStock(null, product1, warehouse1);
        stock1.setQuantity(0);

        // Stock with zero quantity
        Stock stock2 = TestDataFactory.createSampleStock(null, product2, warehouse1);
        stock2.setQuantity(0);

        // Stock with non-zero quantity
        Stock stock3 = TestDataFactory.createSampleStock(null, product3, warehouse2);
        stock3.setQuantity(100);

        entityManager.persist(stock1);
        entityManager.persist(stock2);
        entityManager.persist(stock3);
        entityManager.flush();

        // Find stocks with zero quantity
        List<Stock> zeroStocks = stockRepository.findByQuantity(0);

        // Verify correct stocks were returned
        assertThat(zeroStocks).hasSize(2);
        assertThat(zeroStocks).extracting(Stock::getQuantity)
                             .containsOnly(0);
    }

    @Test
    public void testUpdateStock() {
        // Create and persist a product, warehouse, and stock
        Product product = persistProduct();
        Warehouse warehouse = persistWarehouse();

        Stock stock = TestDataFactory.createSampleStock(null, product, warehouse);
        entityManager.persist(stock);
        entityManager.flush();

        // Update the stock
        stock.setQuantity(150);
        stock.setMinStockLevel(20);
        stock.setMaxStockLevel(300);
        stockRepository.save(stock);
        entityManager.flush();
        entityManager.clear();

        // Retrieve the updated stock
        Stock updatedStock = entityManager.find(Stock.class, stock.getId());

        // Verify the stock was updated correctly
        assertThat(updatedStock.getQuantity()).isEqualTo(150);
        assertThat(updatedStock.getMinStockLevel()).isEqualTo(20);
        assertThat(updatedStock.getMaxStockLevel()).isEqualTo(300);
    }

    @Test
    public void testDeleteStock() {
        // Create and persist a product, warehouse, and stock
        Product product = persistProduct();
        Warehouse warehouse = persistWarehouse();

        Stock stock = TestDataFactory.createSampleStock(null, product, warehouse);
        entityManager.persist(stock);
        entityManager.flush();

        // Delete the stock
        stockRepository.delete(stock);
        entityManager.flush();

        // Try to find the deleted stock
        Stock deletedStock = entityManager.find(Stock.class, stock.getId());

        // Verify the stock was deleted
        assertThat(deletedStock).isNull();
    }
}
