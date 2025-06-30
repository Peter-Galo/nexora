package com.nexora.repository.inventory;

import com.nexora.model.inventory.Product;
import com.nexora.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the ProductRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
public class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void testSaveProduct() {
        // Create a sample product
        Product product = TestDataFactory.createSampleProduct(null);
        
        // Save the product
        Product savedProduct = productRepository.save(product);
        
        // Flush the changes to the database
        entityManager.flush();
        
        // Clear the persistence context to force a fetch from the database
        entityManager.clear();
        
        // Retrieve the product from the database
        Product retrievedProduct = entityManager.find(Product.class, savedProduct.getId());
        
        // Verify the product was saved correctly
        assertThat(retrievedProduct).isNotNull();
        assertThat(retrievedProduct.getCode()).isEqualTo(product.getCode());
        assertThat(retrievedProduct.getName()).isEqualTo(product.getName());
        assertThat(retrievedProduct.getDescription()).isEqualTo(product.getDescription());
        assertThat(retrievedProduct.getPrice()).isEqualByComparingTo(product.getPrice());
        assertThat(retrievedProduct.isActive()).isEqualTo(product.isActive());
        assertThat(retrievedProduct.getCategory()).isEqualTo(product.getCategory());
        assertThat(retrievedProduct.getBrand()).isEqualTo(product.getBrand());
        assertThat(retrievedProduct.getSku()).isEqualTo(product.getSku());
    }

    @Test
    public void testFindByCode() {
        // Create and persist a sample product
        Product product = TestDataFactory.createSampleProduct(null);
        entityManager.persist(product);
        entityManager.flush();
        
        // Find the product by code
        Optional<Product> foundProduct = productRepository.findByCode(product.getCode());
        
        // Verify the product was found
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo(product.getName());
    }

    @Test
    public void testFindByActiveTrue() {
        // Create and persist active and inactive products
        Product activeProduct1 = TestDataFactory.createSampleProduct(null);
        activeProduct1.setName("Active Product 1");
        
        Product activeProduct2 = TestDataFactory.createSampleProduct(null);
        activeProduct2.setName("Active Product 2");
        
        Product inactiveProduct = TestDataFactory.createSampleProduct(null);
        inactiveProduct.setName("Inactive Product");
        inactiveProduct.setActive(false);
        
        entityManager.persist(activeProduct1);
        entityManager.persist(activeProduct2);
        entityManager.persist(inactiveProduct);
        entityManager.flush();
        
        // Find active products
        List<Product> activeProducts = productRepository.findByActiveTrue();
        
        // Verify only active products were returned
        assertThat(activeProducts).hasSize(2);
        assertThat(activeProducts).extracting(Product::getName)
                                 .containsExactlyInAnyOrder("Active Product 1", "Active Product 2");
    }

    @Test
    public void testFindByCategory() {
        // Create and persist products with different categories
        Product product1 = TestDataFactory.createSampleProduct(null);
        product1.setCategory("Electronics");
        
        Product product2 = TestDataFactory.createSampleProduct(null);
        product2.setCategory("Electronics");
        
        Product product3 = TestDataFactory.createSampleProduct(null);
        product3.setCategory("Furniture");
        
        entityManager.persist(product1);
        entityManager.persist(product2);
        entityManager.persist(product3);
        entityManager.flush();
        
        // Find products by category
        List<Product> electronicsProducts = productRepository.findByCategory("Electronics");
        
        // Verify correct products were returned
        assertThat(electronicsProducts).hasSize(2);
        assertThat(electronicsProducts).extracting(Product::getCategory)
                                      .containsOnly("Electronics");
    }

    @Test
    public void testFindByBrand() {
        // Create and persist products with different brands
        Product product1 = TestDataFactory.createSampleProduct(null);
        product1.setBrand("TechBrand");
        
        Product product2 = TestDataFactory.createSampleProduct(null);
        product2.setBrand("TechBrand");
        
        Product product3 = TestDataFactory.createSampleProduct(null);
        product3.setBrand("HomeBrand");
        
        entityManager.persist(product1);
        entityManager.persist(product2);
        entityManager.persist(product3);
        entityManager.flush();
        
        // Find products by brand
        List<Product> techBrandProducts = productRepository.findByBrand("TechBrand");
        
        // Verify correct products were returned
        assertThat(techBrandProducts).hasSize(2);
        assertThat(techBrandProducts).extracting(Product::getBrand)
                                    .containsOnly("TechBrand");
    }

    @Test
    public void testFindByNameContainingIgnoreCase() {
        // Create and persist products with different names
        Product product1 = TestDataFactory.createSampleProduct(null);
        product1.setName("Smartphone X");
        
        Product product2 = TestDataFactory.createSampleProduct(null);
        product2.setName("Smart Watch");
        
        Product product3 = TestDataFactory.createSampleProduct(null);
        product3.setName("Laptop Pro");
        
        entityManager.persist(product1);
        entityManager.persist(product2);
        entityManager.persist(product3);
        entityManager.flush();
        
        // Find products by name containing "smart" (case insensitive)
        List<Product> smartProducts = productRepository.findByNameContainingIgnoreCase("smart");
        
        // Verify correct products were returned
        assertThat(smartProducts).hasSize(2);
        assertThat(smartProducts).extracting(Product::getName)
                                .containsExactlyInAnyOrder("Smartphone X", "Smart Watch");
    }

    @Test
    public void testExistsByCode() {
        // Create and persist a sample product
        Product product = TestDataFactory.createSampleProduct(null);
        entityManager.persist(product);
        entityManager.flush();
        
        // Check if product exists by code
        boolean exists = productRepository.existsByCode(product.getCode());
        boolean notExists = productRepository.existsByCode("NONEXISTENT-CODE");
        
        // Verify results
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    public void testUpdateProduct() {
        // Create and persist a sample product
        Product product = TestDataFactory.createSampleProduct(null);
        entityManager.persist(product);
        entityManager.flush();
        
        // Update the product
        product.setName("Updated Name");
        product.setPrice(new BigDecimal("149.99"));
        productRepository.save(product);
        entityManager.flush();
        entityManager.clear();
        
        // Retrieve the updated product
        Product updatedProduct = entityManager.find(Product.class, product.getId());
        
        // Verify the product was updated correctly
        assertThat(updatedProduct.getName()).isEqualTo("Updated Name");
        assertThat(updatedProduct.getPrice()).isEqualByComparingTo(new BigDecimal("149.99"));
    }

    @Test
    public void testDeleteProduct() {
        // Create and persist a sample product
        Product product = TestDataFactory.createSampleProduct(null);
        entityManager.persist(product);
        entityManager.flush();
        
        // Delete the product
        productRepository.delete(product);
        entityManager.flush();
        
        // Try to find the deleted product
        Product deletedProduct = entityManager.find(Product.class, product.getId());
        
        // Verify the product was deleted
        assertThat(deletedProduct).isNull();
    }
}