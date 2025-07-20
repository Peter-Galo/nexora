package com.nexora.repository.inventory;

import com.nexora.model.inventory.Warehouse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class WarehouseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WarehouseRepository warehouseRepository;

    private Warehouse testWarehouse1;
    private Warehouse testWarehouse2;
    private Warehouse testWarehouse3;

    @BeforeEach
    void setUp() {
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
        testWarehouse2.setActive(false);
        testWarehouse2.setCreatedAt(LocalDateTime.now());
        testWarehouse2.setUpdatedAt(LocalDateTime.now());

        testWarehouse3 = new Warehouse();
        testWarehouse3.setCode("WH003");
        testWarehouse3.setName("Boston Regional Center");
        testWarehouse3.setDescription("Regional distribution center");
        testWarehouse3.setAddress("789 Pine St");
        testWarehouse3.setCity("Boston");
        testWarehouse3.setStateProvince("Massachusetts");
        testWarehouse3.setPostalCode("02109");
        testWarehouse3.setCountry("USA");
        testWarehouse3.setActive(true);
        testWarehouse3.setCreatedAt(LocalDateTime.now());
        testWarehouse3.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testFindByCode_WhenWarehouseExists_ShouldReturnWarehouse() {
        // Given
        entityManager.persistAndFlush(testWarehouse1);

        // When
        Optional<Warehouse> foundWarehouse = warehouseRepository.findByCode("WH001");

        // Then
        assertThat(foundWarehouse).isPresent();
        assertThat(foundWarehouse.get().getCode()).isEqualTo("WH001");
        assertThat(foundWarehouse.get().getName()).isEqualTo("Main Distribution Center");
        assertThat(foundWarehouse.get().getCity()).isEqualTo("Boston");
    }

    @Test
    void testFindByCode_WhenWarehouseDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<Warehouse> foundWarehouse = warehouseRepository.findByCode("NONEXISTENT");

        // Then
        assertThat(foundWarehouse).isEmpty();
    }

    @Test
    void testExistsByCode_WhenWarehouseExists_ShouldReturnTrue() {
        // Given
        entityManager.persistAndFlush(testWarehouse1);

        // When
        boolean exists = warehouseRepository.existsByCode("WH001");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByCode_WhenWarehouseDoesNotExist_ShouldReturnFalse() {
        // When
        boolean exists = warehouseRepository.existsByCode("NONEXISTENT");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void testFindByActiveTrueOrderByName_ShouldReturnOnlyActiveWarehousesOrderedByName() {
        // Given
        entityManager.persistAndFlush(testWarehouse1); // active = true, name = "Main Distribution Center"
        entityManager.persistAndFlush(testWarehouse2); // active = false, name = "Secondary Warehouse"
        entityManager.persistAndFlush(testWarehouse3); // active = true, name = "Boston Regional Center"

        // When
        List<Warehouse> activeWarehouses = warehouseRepository.findByActiveTrueOrderByName();

        // Then
        assertThat(activeWarehouses).hasSize(2);
        assertThat(activeWarehouses).extracting(Warehouse::getCode)
                .containsExactly("WH003", "WH001"); // Ordered by name: "Boston Regional Center", "Main Distribution Center"
        assertThat(activeWarehouses).allMatch(Warehouse::isActive);
    }

    @Test
    void testFindByCity_ShouldReturnWarehousesInSpecifiedCity() {
        // Given
        entityManager.persistAndFlush(testWarehouse1); // Boston
        entityManager.persistAndFlush(testWarehouse2); // New York
        entityManager.persistAndFlush(testWarehouse3); // Boston

        // When
        List<Warehouse> bostonWarehouses = warehouseRepository.findByCity("Boston");

        // Then
        assertThat(bostonWarehouses).hasSize(2);
        assertThat(bostonWarehouses).extracting(Warehouse::getCode)
                .containsExactlyInAnyOrder("WH001", "WH003");
        assertThat(bostonWarehouses).allMatch(w -> "Boston".equals(w.getCity()));
    }

    @Test
    void testFindByStateProvince_ShouldReturnWarehousesInSpecifiedState() {
        // Given
        entityManager.persistAndFlush(testWarehouse1); // Massachusetts
        entityManager.persistAndFlush(testWarehouse2); // New York
        entityManager.persistAndFlush(testWarehouse3); // Massachusetts

        // When
        List<Warehouse> massachusettsWarehouses = warehouseRepository.findByStateProvince("Massachusetts");

        // Then
        assertThat(massachusettsWarehouses).hasSize(2);
        assertThat(massachusettsWarehouses).extracting(Warehouse::getCode)
                .containsExactlyInAnyOrder("WH001", "WH003");
        assertThat(massachusettsWarehouses).allMatch(w -> "Massachusetts".equals(w.getStateProvince()));
    }

    @Test
    void testFindByCountry_ShouldReturnWarehousesInSpecifiedCountry() {
        // Given
        entityManager.persistAndFlush(testWarehouse1); // USA
        entityManager.persistAndFlush(testWarehouse2); // USA
        entityManager.persistAndFlush(testWarehouse3); // USA

        // When
        List<Warehouse> usaWarehouses = warehouseRepository.findByCountry("USA");

        // Then
        assertThat(usaWarehouses).hasSize(3);
        assertThat(usaWarehouses).extracting(Warehouse::getCode)
                .containsExactlyInAnyOrder("WH001", "WH002", "WH003");
        assertThat(usaWarehouses).allMatch(w -> "USA".equals(w.getCountry()));
    }

    @Test
    void testFindByNameContainingIgnoreCaseOrderByName_ShouldReturnMatchingWarehousesOrderedByName() {
        // Given
        entityManager.persistAndFlush(testWarehouse1); // "Main Distribution Center"
        entityManager.persistAndFlush(testWarehouse2); // "Secondary Warehouse"
        entityManager.persistAndFlush(testWarehouse3); // "Boston Regional Center"

        // When
        List<Warehouse> centerWarehouses = warehouseRepository.findByNameContainingIgnoreCaseOrderByName("center");

        // Then
        assertThat(centerWarehouses).hasSize(2);
        assertThat(centerWarehouses).extracting(Warehouse::getCode)
                .containsExactly("WH003", "WH001"); // Ordered by name: "Boston Regional Center", "Main Distribution Center"
    }

    @Test
    void testFindByNameContainingIgnoreCaseOrderByName_WithDifferentCase_ShouldReturnMatchingWarehouses() {
        // Given
        entityManager.persistAndFlush(testWarehouse1); // "Main Distribution Center"
        entityManager.persistAndFlush(testWarehouse2); // "Secondary Warehouse"

        // When
        List<Warehouse> warehouseResults = warehouseRepository.findByNameContainingIgnoreCaseOrderByName("WAREHOUSE");

        // Then
        assertThat(warehouseResults).hasSize(1);
        assertThat(warehouseResults.get(0).getCode()).isEqualTo("WH002");
    }

    @Test
    void testFindAllOrderedByActiveAndName_ShouldReturnWarehousesOrderedByActiveStatusThenName() {
        // Given
        entityManager.persistAndFlush(testWarehouse1); // active = true, name = "Main Distribution Center"
        entityManager.persistAndFlush(testWarehouse2); // active = false, name = "Secondary Warehouse"
        entityManager.persistAndFlush(testWarehouse3); // active = true, name = "Boston Regional Center"

        // When
        List<Warehouse> orderedWarehouses = warehouseRepository.findAllOrderedByActiveAndName();

        // Then
        assertThat(orderedWarehouses).hasSize(3);
        // Should be ordered by active DESC (true first), then by name ASC
        assertThat(orderedWarehouses.get(0).getCode()).isEqualTo("WH003"); // active=true, "Boston Regional Center"
        assertThat(orderedWarehouses.get(1).getCode()).isEqualTo("WH001"); // active=true, "Main Distribution Center"
        assertThat(orderedWarehouses.get(2).getCode()).isEqualTo("WH002"); // active=false, "Secondary Warehouse"
    }

    @Test
    void testSaveWarehouse_ShouldPersistWarehouse() {
        // When
        Warehouse savedWarehouse = warehouseRepository.save(testWarehouse1);

        // Then
        assertThat(savedWarehouse.getUuid()).isNotNull();
        assertThat(savedWarehouse.getCode()).isEqualTo("WH001");
        assertThat(savedWarehouse.getName()).isEqualTo("Main Distribution Center");
        assertThat(savedWarehouse.getCity()).isEqualTo("Boston");

        // Verify it's actually persisted
        Warehouse foundWarehouse = entityManager.find(Warehouse.class, savedWarehouse.getUuid());
        assertThat(foundWarehouse).isNotNull();
        assertThat(foundWarehouse.getCode()).isEqualTo("WH001");
    }

    @Test
    void testDeleteWarehouse_ShouldRemoveWarehouse() {
        // Given
        Warehouse savedWarehouse = entityManager.persistAndFlush(testWarehouse1);

        // When
        warehouseRepository.deleteById(savedWarehouse.getUuid());
        entityManager.flush();

        // Then
        Warehouse deletedWarehouse = entityManager.find(Warehouse.class, savedWarehouse.getUuid());
        assertThat(deletedWarehouse).isNull();
    }

    @Test
    void testCodeUniqueness_ShouldEnforceUniqueConstraint() {
        // Given
        entityManager.persistAndFlush(testWarehouse1);

        Warehouse duplicateCodeWarehouse = new Warehouse();
        duplicateCodeWarehouse.setCode("WH001"); // Same code
        duplicateCodeWarehouse.setName("Different Warehouse");
        duplicateCodeWarehouse.setDescription("Different description");
        duplicateCodeWarehouse.setAddress("Different Address");
        duplicateCodeWarehouse.setCity("Different City");
        duplicateCodeWarehouse.setStateProvince("Different State");
        duplicateCodeWarehouse.setPostalCode("12345");
        duplicateCodeWarehouse.setCountry("USA");
        duplicateCodeWarehouse.setActive(true);
        duplicateCodeWarehouse.setCreatedAt(LocalDateTime.now());
        duplicateCodeWarehouse.setUpdatedAt(LocalDateTime.now());

        // When & Then
        try {
            entityManager.persistAndFlush(duplicateCodeWarehouse);
            entityManager.flush();
            // If we reach here, the test should fail
            assertThat(false).as("Expected constraint violation for duplicate code").isTrue();
        } catch (Exception e) {
            // Expected behavior - constraint violation
            assertThat(e.getMessage()).contains("could not execute statement");
        }
    }

    @Test
    void testFindAll_ShouldReturnAllWarehouses() {
        // Given
        entityManager.persistAndFlush(testWarehouse1);
        entityManager.persistAndFlush(testWarehouse2);
        entityManager.persistAndFlush(testWarehouse3);

        // When
        List<Warehouse> allWarehouses = warehouseRepository.findAll();

        // Then
        assertThat(allWarehouses).hasSize(3);
        assertThat(allWarehouses).extracting(Warehouse::getCode)
                .containsExactlyInAnyOrder("WH001", "WH002", "WH003");
    }

    @Test
    void testFindByCity_WithNonExistentCity_ShouldReturnEmptyList() {
        // Given
        entityManager.persistAndFlush(testWarehouse1);

        // When
        List<Warehouse> warehouses = warehouseRepository.findByCity("NonExistentCity");

        // Then
        assertThat(warehouses).isEmpty();
    }

    @Test
    void testFindByStateProvince_WithNonExistentState_ShouldReturnEmptyList() {
        // Given
        entityManager.persistAndFlush(testWarehouse1);

        // When
        List<Warehouse> warehouses = warehouseRepository.findByStateProvince("NonExistentState");

        // Then
        assertThat(warehouses).isEmpty();
    }

    @Test
    void testFindByCountry_WithNonExistentCountry_ShouldReturnEmptyList() {
        // Given
        entityManager.persistAndFlush(testWarehouse1);

        // When
        List<Warehouse> warehouses = warehouseRepository.findByCountry("NonExistentCountry");

        // Then
        assertThat(warehouses).isEmpty();
    }
}
