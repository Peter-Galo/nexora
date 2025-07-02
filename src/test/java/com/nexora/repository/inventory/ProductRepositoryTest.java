package com.nexora.repository.inventory;

import com.nexora.model.inventory.Product;
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
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Product product1;
    private Product product2;
    private Product product3;

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
        product3.setSku("SKU001"); // Same SKU as product1 to test findBySku
        product3.setActive(false); // Inactive product

        // Persist test products
        entityManager.persist(product1);
        entityManager.persist(product2);
        entityManager.persist(product3);
        entityManager.flush();
    }

    @Test
    void findByCode_shouldReturnProduct_whenCodeExists() {
        // Act
        Optional<Product> foundProduct = productRepository.findByCode("P001");

        // Assert
        assertTrue(foundProduct.isPresent());
        assertEquals(product1.getUuid(), foundProduct.get().getUuid());
        assertEquals("Laptop", foundProduct.get().getName());
    }

    @Test
    void findByCode_shouldReturnEmpty_whenCodeDoesNotExist() {
        // Act
        Optional<Product> foundProduct = productRepository.findByCode("NONEXISTENT");

        // Assert
        assertFalse(foundProduct.isPresent());
    }

    @Test
    void existsByCode_shouldReturnTrue_whenCodeExists() {
        // Act
        boolean exists = productRepository.existsByCode("P001");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByCode_shouldReturnFalse_whenCodeDoesNotExist() {
        // Act
        boolean exists = productRepository.existsByCode("NONEXISTENT");

        // Assert
        assertFalse(exists);
    }

    @Test
    void findByActiveTrue_shouldReturnOnlyActiveProducts() {
        // Act
        List<Product> activeProducts = productRepository.findByActiveTrue();

        // Assert
        assertEquals(2, activeProducts.size());
        assertTrue(activeProducts.stream().allMatch(Product::isActive));
        assertTrue(activeProducts.stream().anyMatch(p -> p.getCode().equals("P001")));
        assertTrue(activeProducts.stream().anyMatch(p -> p.getCode().equals("P002")));
    }

    @Test
    void findByCategory_shouldReturnProductsInCategory() {
        // Act
        List<Product> electronicsProducts = productRepository.findByCategory("Electronics");
        List<Product> accessoriesProducts = productRepository.findByCategory("Accessories");
        List<Product> nonExistentCategoryProducts = productRepository.findByCategory("NonExistent");

        // Assert
        assertEquals(2, electronicsProducts.size());
        assertEquals(1, accessoriesProducts.size());
        assertEquals(0, nonExistentCategoryProducts.size());

        assertTrue(electronicsProducts.stream().anyMatch(p -> p.getCode().equals("P001")));
        assertTrue(electronicsProducts.stream().anyMatch(p -> p.getCode().equals("P002")));
        assertTrue(accessoriesProducts.stream().anyMatch(p -> p.getCode().equals("P003")));
    }

    @Test
    void findByBrand_shouldReturnProductsOfBrand() {
        // Act
        List<Product> techBrandProducts = productRepository.findByBrand("TechBrand");
        List<Product> mobileBrandProducts = productRepository.findByBrand("MobileBrand");
        List<Product> audioBrandProducts = productRepository.findByBrand("AudioBrand");
        List<Product> nonExistentBrandProducts = productRepository.findByBrand("NonExistent");

        // Assert
        assertEquals(1, techBrandProducts.size());
        assertEquals(1, mobileBrandProducts.size());
        assertEquals(1, audioBrandProducts.size());
        assertEquals(0, nonExistentBrandProducts.size());

        assertEquals("P001", techBrandProducts.get(0).getCode());
        assertEquals("P002", mobileBrandProducts.get(0).getCode());
        assertEquals("P003", audioBrandProducts.get(0).getCode());
    }

    @Test
    void findBySku_shouldReturnProductsWithSku() {
        // Act
        List<Product> sku001Products = productRepository.findBySku("SKU001");
        List<Product> sku002Products = productRepository.findBySku("SKU002");
        List<Product> nonExistentSkuProducts = productRepository.findBySku("NONEXISTENT");

        // Assert
        assertEquals(2, sku001Products.size());
        assertEquals(1, sku002Products.size());
        assertEquals(0, nonExistentSkuProducts.size());

        assertTrue(sku001Products.stream().anyMatch(p -> p.getCode().equals("P001")));
        assertTrue(sku001Products.stream().anyMatch(p -> p.getCode().equals("P003")));
        assertEquals("P002", sku002Products.get(0).getCode());
    }

    @Test
    void findByNameContainingIgnoreCase_shouldReturnProductsWithNameContainingText() {
        // Act
        List<Product> laptopProducts = productRepository.findByNameContainingIgnoreCase("laptop");
        List<Product> phoneProducts = productRepository.findByNameContainingIgnoreCase("phone");
        List<Product> nonExistentNameProducts = productRepository.findByNameContainingIgnoreCase("nonexistent");

        // Assert
        assertEquals(1, laptopProducts.size());
        assertEquals(2, phoneProducts.size()); // Should match "Smartphone" and "Headphones"
        assertEquals(0, nonExistentNameProducts.size());

        assertEquals("P001", laptopProducts.get(0).getCode());
        assertTrue(phoneProducts.stream().anyMatch(p -> p.getCode().equals("P002")));
        assertTrue(phoneProducts.stream().anyMatch(p -> p.getCode().equals("P003")));
    }
}
