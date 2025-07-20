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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct1;
    private Product testProduct2;
    private Product testProduct3;

    @BeforeEach
    void setUp() {
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
        testProduct2.setName("Another Product");
        testProduct2.setDescription("Description for another product");
        testProduct2.setPrice(new BigDecimal("149.99"));
        testProduct2.setActive(false);
        testProduct2.setCategory("Clothing");
        testProduct2.setBrand("AnotherBrand");
        testProduct2.setSku("SKU002");
        testProduct2.setCreatedAt(LocalDateTime.now());
        testProduct2.setUpdatedAt(LocalDateTime.now());

        testProduct3 = new Product();
        testProduct3.setCode("PROD003");
        testProduct3.setName("Test Electronics Item");
        testProduct3.setDescription("Another electronics item");
        testProduct3.setPrice(new BigDecimal("199.99"));
        testProduct3.setActive(true);
        testProduct3.setCategory("Electronics");
        testProduct3.setBrand("TestBrand");
        testProduct3.setSku("SKU003");
        testProduct3.setCreatedAt(LocalDateTime.now());
        testProduct3.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testFindByCode_WhenProductExists_ShouldReturnProduct() {
        // Given
        entityManager.persistAndFlush(testProduct1);

        // When
        Optional<Product> foundProduct = productRepository.findByCode("PROD001");

        // Then
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getCode()).isEqualTo("PROD001");
        assertThat(foundProduct.get().getName()).isEqualTo("Test Product 1");
        assertThat(foundProduct.get().getPrice()).isEqualTo(new BigDecimal("99.99"));
    }

    @Test
    void testFindByCode_WhenProductDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<Product> foundProduct = productRepository.findByCode("NONEXISTENT");

        // Then
        assertThat(foundProduct).isEmpty();
    }

    @Test
    void testExistsByCode_WhenProductExists_ShouldReturnTrue() {
        // Given
        entityManager.persistAndFlush(testProduct1);

        // When
        boolean exists = productRepository.existsByCode("PROD001");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByCode_WhenProductDoesNotExist_ShouldReturnFalse() {
        // When
        boolean exists = productRepository.existsByCode("NONEXISTENT");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void testFindByActiveTrue_ShouldReturnOnlyActiveProducts() {
        // Given
        entityManager.persistAndFlush(testProduct1); // active = true
        entityManager.persistAndFlush(testProduct2); // active = false
        entityManager.persistAndFlush(testProduct3); // active = true

        // When
        List<Product> activeProducts = productRepository.findByActiveTrue();

        // Then
        assertThat(activeProducts).hasSize(2);
        assertThat(activeProducts).extracting(Product::getCode)
                .containsExactlyInAnyOrder("PROD001", "PROD003");
        assertThat(activeProducts).allMatch(Product::isActive);
    }

    @Test
    void testFindByCategory_ShouldReturnProductsInCategory() {
        // Given
        entityManager.persistAndFlush(testProduct1); // Electronics
        entityManager.persistAndFlush(testProduct2); // Clothing
        entityManager.persistAndFlush(testProduct3); // Electronics

        // When
        List<Product> electronicsProducts = productRepository.findByCategory("Electronics");

        // Then
        assertThat(electronicsProducts).hasSize(2);
        assertThat(electronicsProducts).extracting(Product::getCode)
                .containsExactlyInAnyOrder("PROD001", "PROD003");
        assertThat(electronicsProducts).allMatch(p -> "Electronics".equals(p.getCategory()));
    }

    @Test
    void testFindByBrand_ShouldReturnProductsOfBrand() {
        // Given
        entityManager.persistAndFlush(testProduct1); // TestBrand
        entityManager.persistAndFlush(testProduct2); // AnotherBrand
        entityManager.persistAndFlush(testProduct3); // TestBrand

        // When
        List<Product> testBrandProducts = productRepository.findByBrand("TestBrand");

        // Then
        assertThat(testBrandProducts).hasSize(2);
        assertThat(testBrandProducts).extracting(Product::getCode)
                .containsExactlyInAnyOrder("PROD001", "PROD003");
        assertThat(testBrandProducts).allMatch(p -> "TestBrand".equals(p.getBrand()));
    }

    @Test
    void testFindBySku_ShouldReturnProductsWithSku() {
        // Given
        entityManager.persistAndFlush(testProduct1); // SKU001
        entityManager.persistAndFlush(testProduct2); // SKU002
        entityManager.persistAndFlush(testProduct3); // SKU003

        // When
        List<Product> skuProducts = productRepository.findBySku("SKU001");

        // Then
        assertThat(skuProducts).hasSize(1);
        assertThat(skuProducts.get(0).getCode()).isEqualTo("PROD001");
        assertThat(skuProducts.get(0).getSku()).isEqualTo("SKU001");
    }

    @Test
    void testFindByNameContainingIgnoreCase_ShouldReturnMatchingProducts() {
        // Given
        entityManager.persistAndFlush(testProduct1); // "Test Product 1"
        entityManager.persistAndFlush(testProduct2); // "Another Product"
        entityManager.persistAndFlush(testProduct3); // "Test Electronics Item"

        // When
        List<Product> testProducts = productRepository.findByNameContainingIgnoreCase("test");

        // Then
        assertThat(testProducts).hasSize(2);
        assertThat(testProducts).extracting(Product::getCode)
                .containsExactlyInAnyOrder("PROD001", "PROD003");
    }

    @Test
    void testFindByNameContainingIgnoreCase_WithDifferentCase_ShouldReturnMatchingProducts() {
        // Given
        entityManager.persistAndFlush(testProduct1); // "Test Product 1"
        entityManager.persistAndFlush(testProduct2); // "Another Product"

        // When
        List<Product> productProducts = productRepository.findByNameContainingIgnoreCase("PRODUCT");

        // Then
        assertThat(productProducts).hasSize(2);
        assertThat(productProducts).extracting(Product::getCode)
                .containsExactlyInAnyOrder("PROD001", "PROD002");
    }

    @Test
    void testSaveProduct_ShouldPersistProduct() {
        // When
        Product savedProduct = productRepository.save(testProduct1);

        // Then
        assertThat(savedProduct.getUuid()).isNotNull();
        assertThat(savedProduct.getCode()).isEqualTo("PROD001");
        assertThat(savedProduct.getName()).isEqualTo("Test Product 1");
        assertThat(savedProduct.getPrice()).isEqualTo(new BigDecimal("99.99"));

        // Verify it's actually persisted
        Product foundProduct = entityManager.find(Product.class, savedProduct.getUuid());
        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.getCode()).isEqualTo("PROD001");
    }

    @Test
    void testDeleteProduct_ShouldRemoveProduct() {
        // Given
        Product savedProduct = entityManager.persistAndFlush(testProduct1);

        // When
        productRepository.deleteById(savedProduct.getUuid());
        entityManager.flush();

        // Then
        Product deletedProduct = entityManager.find(Product.class, savedProduct.getUuid());
        assertThat(deletedProduct).isNull();
    }

    @Test
    void testCodeUniqueness_ShouldEnforceUniqueConstraint() {
        // Given
        entityManager.persistAndFlush(testProduct1);

        Product duplicateCodeProduct = new Product();
        duplicateCodeProduct.setCode("PROD001"); // Same code
        duplicateCodeProduct.setName("Different Product");
        duplicateCodeProduct.setPrice(new BigDecimal("199.99"));

        // When & Then
        try {
            entityManager.persistAndFlush(duplicateCodeProduct);
            entityManager.flush();
            // If we reach here, the test should fail
            assertThat(false).as("Expected constraint violation for duplicate code").isTrue();
        } catch (Exception e) {
            // Expected behavior - constraint violation
            assertThat(e.getMessage()).contains("could not execute statement");
        }
    }

    @Test
    void testFindAll_ShouldReturnAllProducts() {
        // Given
        entityManager.persistAndFlush(testProduct1);
        entityManager.persistAndFlush(testProduct2);
        entityManager.persistAndFlush(testProduct3);

        // When
        List<Product> allProducts = productRepository.findAll();

        // Then
        assertThat(allProducts).hasSize(3);
        assertThat(allProducts).extracting(Product::getCode)
                .containsExactlyInAnyOrder("PROD001", "PROD002", "PROD003");
    }

    @Test
    void testFindByCategory_WithNonExistentCategory_ShouldReturnEmptyList() {
        // Given
        entityManager.persistAndFlush(testProduct1);

        // When
        List<Product> products = productRepository.findByCategory("NonExistentCategory");

        // Then
        assertThat(products).isEmpty();
    }

    @Test
    void testFindByBrand_WithNonExistentBrand_ShouldReturnEmptyList() {
        // Given
        entityManager.persistAndFlush(testProduct1);

        // When
        List<Product> products = productRepository.findByBrand("NonExistentBrand");

        // Then
        assertThat(products).isEmpty();
    }
}