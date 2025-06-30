package com.nexora.repository.inventory;

import com.nexora.model.inventory.Warehouse;
import com.nexora.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the WarehouseRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
public class WarehouseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Test
    public void testSaveWarehouse() {
        // Create a sample warehouse
        Warehouse warehouse = TestDataFactory.createSampleWarehouse(null);
        
        // Save the warehouse
        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        
        // Flush the changes to the database
        entityManager.flush();
        
        // Clear the persistence context to force a fetch from the database
        entityManager.clear();
        
        // Retrieve the warehouse from the database
        Warehouse retrievedWarehouse = entityManager.find(Warehouse.class, savedWarehouse.getId());
        
        // Verify the warehouse was saved correctly
        assertThat(retrievedWarehouse).isNotNull();
        assertThat(retrievedWarehouse.getCode()).isEqualTo(warehouse.getCode());
        assertThat(retrievedWarehouse.getName()).isEqualTo(warehouse.getName());
        assertThat(retrievedWarehouse.getDescription()).isEqualTo(warehouse.getDescription());
        assertThat(retrievedWarehouse.getAddress()).isEqualTo(warehouse.getAddress());
        assertThat(retrievedWarehouse.getCity()).isEqualTo(warehouse.getCity());
        assertThat(retrievedWarehouse.getStateProvince()).isEqualTo(warehouse.getStateProvince());
        assertThat(retrievedWarehouse.getPostalCode()).isEqualTo(warehouse.getPostalCode());
        assertThat(retrievedWarehouse.getCountry()).isEqualTo(warehouse.getCountry());
        assertThat(retrievedWarehouse.isActive()).isEqualTo(warehouse.isActive());
    }

    @Test
    public void testFindByCode() {
        // Create and persist a sample warehouse
        Warehouse warehouse = TestDataFactory.createSampleWarehouse(null);
        entityManager.persist(warehouse);
        entityManager.flush();
        
        // Find the warehouse by code
        Optional<Warehouse> foundWarehouse = warehouseRepository.findByCode(warehouse.getCode());
        
        // Verify the warehouse was found
        assertThat(foundWarehouse).isPresent();
        assertThat(foundWarehouse.get().getName()).isEqualTo(warehouse.getName());
    }

    @Test
    public void testFindByActiveTrue() {
        // Create and persist active and inactive warehouses
        Warehouse activeWarehouse1 = TestDataFactory.createSampleWarehouse(null);
        activeWarehouse1.setName("Active Warehouse 1");
        
        Warehouse activeWarehouse2 = TestDataFactory.createSampleWarehouse(null);
        activeWarehouse2.setName("Active Warehouse 2");
        
        Warehouse inactiveWarehouse = TestDataFactory.createSampleWarehouse(null);
        inactiveWarehouse.setName("Inactive Warehouse");
        inactiveWarehouse.setActive(false);
        
        entityManager.persist(activeWarehouse1);
        entityManager.persist(activeWarehouse2);
        entityManager.persist(inactiveWarehouse);
        entityManager.flush();
        
        // Find active warehouses
        List<Warehouse> activeWarehouses = warehouseRepository.findByActiveTrue();
        
        // Verify only active warehouses were returned
        assertThat(activeWarehouses).hasSize(2);
        assertThat(activeWarehouses).extracting(Warehouse::getName)
                                   .containsExactlyInAnyOrder("Active Warehouse 1", "Active Warehouse 2");
    }

    @Test
    public void testFindByCity() {
        // Create and persist warehouses in different cities
        Warehouse warehouse1 = TestDataFactory.createSampleWarehouse(null);
        warehouse1.setCity("New York");
        
        Warehouse warehouse2 = TestDataFactory.createSampleWarehouse(null);
        warehouse2.setCity("New York");
        
        Warehouse warehouse3 = TestDataFactory.createSampleWarehouse(null);
        warehouse3.setCity("Los Angeles");
        
        entityManager.persist(warehouse1);
        entityManager.persist(warehouse2);
        entityManager.persist(warehouse3);
        entityManager.flush();
        
        // Find warehouses by city
        List<Warehouse> newYorkWarehouses = warehouseRepository.findByCity("New York");
        
        // Verify correct warehouses were returned
        assertThat(newYorkWarehouses).hasSize(2);
        assertThat(newYorkWarehouses).extracting(Warehouse::getCity)
                                    .containsOnly("New York");
    }

    @Test
    public void testFindByCountry() {
        // Create and persist warehouses in different countries
        Warehouse warehouse1 = TestDataFactory.createSampleWarehouse(null);
        warehouse1.setCountry("USA");
        
        Warehouse warehouse2 = TestDataFactory.createSampleWarehouse(null);
        warehouse2.setCountry("USA");
        
        Warehouse warehouse3 = TestDataFactory.createSampleWarehouse(null);
        warehouse3.setCountry("Canada");
        
        entityManager.persist(warehouse1);
        entityManager.persist(warehouse2);
        entityManager.persist(warehouse3);
        entityManager.flush();
        
        // Find warehouses by country
        List<Warehouse> usaWarehouses = warehouseRepository.findByCountry("USA");
        
        // Verify correct warehouses were returned
        assertThat(usaWarehouses).hasSize(2);
        assertThat(usaWarehouses).extracting(Warehouse::getCountry)
                                .containsOnly("USA");
    }

    @Test
    public void testFindByNameContainingIgnoreCase() {
        // Create and persist warehouses with different names
        Warehouse warehouse1 = TestDataFactory.createSampleWarehouse(null);
        warehouse1.setName("Distribution Center North");
        
        Warehouse warehouse2 = TestDataFactory.createSampleWarehouse(null);
        warehouse2.setName("North Regional Warehouse");
        
        Warehouse warehouse3 = TestDataFactory.createSampleWarehouse(null);
        warehouse3.setName("South Storage Facility");
        
        entityManager.persist(warehouse1);
        entityManager.persist(warehouse2);
        entityManager.persist(warehouse3);
        entityManager.flush();
        
        // Find warehouses by name containing "north" (case insensitive)
        List<Warehouse> northWarehouses = warehouseRepository.findByNameContainingIgnoreCase("north");
        
        // Verify correct warehouses were returned
        assertThat(northWarehouses).hasSize(2);
        assertThat(northWarehouses).extracting(Warehouse::getName)
                                  .containsExactlyInAnyOrder("Distribution Center North", "North Regional Warehouse");
    }

    @Test
    public void testExistsByCode() {
        // Create and persist a sample warehouse
        Warehouse warehouse = TestDataFactory.createSampleWarehouse(null);
        entityManager.persist(warehouse);
        entityManager.flush();
        
        // Check if warehouse exists by code
        boolean exists = warehouseRepository.existsByCode(warehouse.getCode());
        boolean notExists = warehouseRepository.existsByCode("NONEXISTENT-CODE");
        
        // Verify results
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    public void testUpdateWarehouse() {
        // Create and persist a sample warehouse
        Warehouse warehouse = TestDataFactory.createSampleWarehouse(null);
        entityManager.persist(warehouse);
        entityManager.flush();
        
        // Update the warehouse
        warehouse.setName("Updated Name");
        warehouse.setAddress("456 New Address");
        warehouseRepository.save(warehouse);
        entityManager.flush();
        entityManager.clear();
        
        // Retrieve the updated warehouse
        Warehouse updatedWarehouse = entityManager.find(Warehouse.class, warehouse.getId());
        
        // Verify the warehouse was updated correctly
        assertThat(updatedWarehouse.getName()).isEqualTo("Updated Name");
        assertThat(updatedWarehouse.getAddress()).isEqualTo("456 New Address");
    }

    @Test
    public void testDeleteWarehouse() {
        // Create and persist a sample warehouse
        Warehouse warehouse = TestDataFactory.createSampleWarehouse(null);
        entityManager.persist(warehouse);
        entityManager.flush();
        
        // Delete the warehouse
        warehouseRepository.delete(warehouse);
        entityManager.flush();
        
        // Try to find the deleted warehouse
        Warehouse deletedWarehouse = entityManager.find(Warehouse.class, warehouse.getId());
        
        // Verify the warehouse was deleted
        assertThat(deletedWarehouse).isNull();
    }
}