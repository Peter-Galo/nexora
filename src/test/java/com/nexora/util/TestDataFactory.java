package com.nexora.util;

import com.nexora.dto.inventory.ProductDTO;
import com.nexora.dto.inventory.WarehouseDTO;
import com.nexora.dto.inventory.StockDTO;
import com.nexora.model.inventory.Product;
import com.nexora.model.inventory.Warehouse;
import com.nexora.model.inventory.Stock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for creating test data for unit and integration tests.
 */
public class TestDataFactory {

    /**
     * Creates a sample Product entity for testing.
     *
     * @param id The ID to set for the product (can be null for new products)
     * @return A Product entity with sample data
     */
    // Counter to generate unique codes for test products
    private static long productCounter = 0;

    public static Product createSampleProduct(Long id) {
        Product product = new Product();
        product.setId(id);
        // Generate a unique code using a counter for new products
        String uniqueSuffix = id != null ? String.valueOf(id) : "NEW-" + System.currentTimeMillis() + "-" + (++productCounter);
        product.setCode("TEST-PROD-" + uniqueSuffix);
        product.setName("Test Product " + (id != null ? id : "New-" + productCounter));
        product.setDescription("This is a test product for unit testing");
        product.setPrice(new BigDecimal("99.99"));
        product.setActive(true);
        product.setCategory("Test Category");
        product.setBrand("Test Brand");
        product.setSku("TEST-SKU-" + uniqueSuffix);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    /**
     * Creates a sample ProductDTO for testing.
     *
     * @param id The ID to set for the product DTO (can be null for new products)
     * @return A ProductDTO with sample data
     */
    public static ProductDTO createSampleProductDTO(Long id) {
        ProductDTO dto = new ProductDTO();
        dto.setId(id);
        // Generate a unique code using a counter for new products
        String uniqueSuffix = id != null ? String.valueOf(id) : "NEW-" + System.currentTimeMillis() + "-" + (++productCounter);
        dto.setCode("TEST-PROD-" + uniqueSuffix);
        dto.setName("Test Product " + (id != null ? id : "New-" + productCounter));
        dto.setDescription("This is a test product for unit testing");
        dto.setPrice(new BigDecimal("99.99"));
        dto.setActive(true);
        dto.setCategory("Test Category");
        dto.setBrand("Test Brand");
        dto.setSku("TEST-SKU-" + uniqueSuffix);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }

    /**
     * Creates a list of sample Product entities for testing.
     *
     * @param count The number of products to create
     * @return A list of Product entities with sample data
     */
    public static List<Product> createSampleProductList(int count) {
        List<Product> products = new ArrayList<>();
        for (long i = 1; i <= count; i++) {
            products.add(createSampleProduct(i));
        }
        return products;
    }

    /**
     * Creates a list of sample ProductDTOs for testing.
     *
     * @param count The number of product DTOs to create
     * @return A list of ProductDTOs with sample data
     */
    public static List<ProductDTO> createSampleProductDTOList(int count) {
        List<ProductDTO> dtos = new ArrayList<>();
        for (long i = 1; i <= count; i++) {
            dtos.add(createSampleProductDTO(i));
        }
        return dtos;
    }

    /**
     * Creates a sample Warehouse entity for testing.
     *
     * @param id The ID to set for the warehouse (can be null for new warehouses)
     * @return A Warehouse entity with sample data
     */
    // Counter to generate unique codes for test warehouses
    private static long warehouseCounter = 0;

    public static Warehouse createSampleWarehouse(Long id) {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(id);
        // Generate a unique code using a counter for new warehouses
        String uniqueSuffix = id != null ? String.valueOf(id) : "NEW-" + System.currentTimeMillis() + "-" + (++warehouseCounter);
        warehouse.setCode("TEST-WH-" + uniqueSuffix);
        warehouse.setName("Test Warehouse " + (id != null ? id : "New-" + warehouseCounter));
        warehouse.setDescription("This is a test warehouse for unit testing");
        warehouse.setAddress("123 Test Street");
        warehouse.setCity("Test City");
        warehouse.setStateProvince("Test State");
        warehouse.setPostalCode("12345");
        warehouse.setCountry("Test Country");
        warehouse.setActive(true);
        warehouse.setCreatedAt(LocalDateTime.now());
        warehouse.setUpdatedAt(LocalDateTime.now());
        return warehouse;
    }

    /**
     * Creates a sample Stock entity for testing.
     *
     * @param id The ID to set for the stock (can be null for new stocks)
     * @param product The product for this stock
     * @param warehouse The warehouse for this stock
     * @return A Stock entity with sample data
     */
    public static Stock createSampleStock(Long id, Product product, Warehouse warehouse) {
        Stock stock = new Stock();
        stock.setId(id);
        stock.setProduct(product);
        stock.setWarehouse(warehouse);
        stock.setQuantity(100);
        stock.setMinStockLevel(10);
        stock.setMaxStockLevel(200);
        stock.setLastRestockDate(LocalDateTime.now());
        stock.setCreatedAt(LocalDateTime.now());
        stock.setUpdatedAt(LocalDateTime.now());
        return stock;
    }

    /**
     * Creates a sample WarehouseDTO for testing.
     *
     * @param id The ID to set for the warehouse DTO (can be null for new warehouses)
     * @return A WarehouseDTO with sample data
     */
    public static WarehouseDTO createSampleWarehouseDTO(Long id) {
        WarehouseDTO dto = new WarehouseDTO();
        dto.setId(id);
        // Generate a unique code using a counter for new warehouses
        String uniqueSuffix = id != null ? String.valueOf(id) : "NEW-" + System.currentTimeMillis() + "-" + (++warehouseCounter);
        dto.setCode("TEST-WH-" + uniqueSuffix);
        dto.setName("Test Warehouse " + (id != null ? id : "New-" + warehouseCounter));
        dto.setDescription("This is a test warehouse for unit testing");
        dto.setAddress("123 Test Street");
        dto.setCity("Test City");
        dto.setStateProvince("Test State");
        dto.setPostalCode("12345");
        dto.setCountry("Test Country");
        dto.setActive(true);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }

    /**
     * Creates a list of sample WarehouseDTO objects for testing.
     *
     * @param count The number of warehouse DTOs to create
     * @return A list of WarehouseDTO objects with sample data
     */
    public static List<WarehouseDTO> createSampleWarehouseDTOList(int count) {
        List<WarehouseDTO> dtos = new ArrayList<>();
        for (long i = 1; i <= count; i++) {
            dtos.add(createSampleWarehouseDTO(i));
        }
        return dtos;
    }

    /**
     * Creates a sample StockDTO for testing.
     *
     * @param id The ID to set for the stock DTO (can be null for new stocks)
     * @param productDTO The product DTO for this stock
     * @param warehouseDTO The warehouse DTO for this stock
     * @return A StockDTO with sample data
     */
    public static StockDTO createSampleStockDTO(Long id, ProductDTO productDTO, WarehouseDTO warehouseDTO) {
        StockDTO dto = new StockDTO();
        dto.setId(id);
        dto.setProduct(productDTO);
        dto.setWarehouse(warehouseDTO);
        dto.setQuantity(100);
        dto.setMinStockLevel(10);
        dto.setMaxStockLevel(200);
        dto.setLastRestockDate(LocalDateTime.now());
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }

    /**
     * Creates a list of sample StockDTO objects for testing.
     *
     * @param count The number of stock DTOs to create
     * @param productDTO The product DTO to use for all stocks
     * @param warehouseDTO The warehouse DTO to use for all stocks
     * @return A list of StockDTO objects with sample data
     */
    public static List<StockDTO> createSampleStockDTOList(int count, ProductDTO productDTO, WarehouseDTO warehouseDTO) {
        List<StockDTO> dtos = new ArrayList<>();
        for (long i = 1; i <= count; i++) {
            dtos.add(createSampleStockDTO(i, productDTO, warehouseDTO));
        }
        return dtos;
    }
}
