package com.nexora.repository.inventory;

import com.nexora.model.inventory.Warehouse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class WarehouseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WarehouseRepository warehouseRepository;

    private Warehouse warehouse1;
    private Warehouse warehouse2;
    private Warehouse warehouse3;

    @BeforeEach
    void setUp() {
        // Create test warehouses
        warehouse1 = new Warehouse("WH001", "Main Warehouse");
        warehouse1.setDescription("Main distribution center");
        warehouse1.setAddress("123 Main St");
        warehouse1.setCity("New York");
        warehouse1.setStateProvince("NY");
        warehouse1.setPostalCode("10001");
        warehouse1.setCountry("USA");
        warehouse1.setActive(true);

        warehouse2 = new Warehouse("WH002", "West Coast Warehouse");
        warehouse2.setDescription("West coast distribution center");
        warehouse2.setAddress("456 Ocean Ave");
        warehouse2.setCity("Los Angeles");
        warehouse2.setStateProvince("CA");
        warehouse2.setPostalCode("90001");
        warehouse2.setCountry("USA");
        warehouse2.setActive(true);

        warehouse3 = new Warehouse("WH003", "European Warehouse");
        warehouse3.setDescription("European distribution center");
        warehouse3.setAddress("789 Euro Blvd");
        warehouse3.setCity("Berlin");
        warehouse3.setStateProvince("Berlin");
        warehouse3.setPostalCode("10115");
        warehouse3.setCountry("Germany");
        warehouse3.setActive(false); // Inactive warehouse

        // Persist test warehouses
        entityManager.persist(warehouse1);
        entityManager.persist(warehouse2);
        entityManager.persist(warehouse3);
        entityManager.flush();
    }

    @Test
    void findByCode_shouldReturnWarehouse_whenCodeExists() {
        // Act
        Optional<Warehouse> foundWarehouse = warehouseRepository.findByCode("WH001");

        // Assert
        assertTrue(foundWarehouse.isPresent());
        assertEquals(warehouse1.getUuid(), foundWarehouse.get().getUuid());
        assertEquals("Main Warehouse", foundWarehouse.get().getName());
    }

    @Test
    void findByCode_shouldReturnEmpty_whenCodeDoesNotExist() {
        // Act
        Optional<Warehouse> foundWarehouse = warehouseRepository.findByCode("NONEXISTENT");

        // Assert
        assertFalse(foundWarehouse.isPresent());
    }

    @Test
    void existsByCode_shouldReturnTrue_whenCodeExists() {
        // Act
        boolean exists = warehouseRepository.existsByCode("WH001");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByCode_shouldReturnFalse_whenCodeDoesNotExist() {
        // Act
        boolean exists = warehouseRepository.existsByCode("NONEXISTENT");

        // Assert
        assertFalse(exists);
    }

    @Test
    void findByActiveTrue_shouldReturnOnlyActiveWarehouses() {
        // Act
        List<Warehouse> activeWarehouses = warehouseRepository.findByActiveTrueOrderByName();

        // Assert
        assertEquals(2, activeWarehouses.size());
        assertTrue(activeWarehouses.stream().allMatch(Warehouse::isActive));
        assertTrue(activeWarehouses.stream().anyMatch(w -> w.getCode().equals("WH001")));
        assertTrue(activeWarehouses.stream().anyMatch(w -> w.getCode().equals("WH002")));
    }

    @Test
    void findByCity_shouldReturnWarehousesInCity() {
        // Act
        List<Warehouse> newYorkWarehouses = warehouseRepository.findByCity("New York");
        List<Warehouse> losAngelesWarehouses = warehouseRepository.findByCity("Los Angeles");
        List<Warehouse> berlinWarehouses = warehouseRepository.findByCity("Berlin");
        List<Warehouse> nonExistentCityWarehouses = warehouseRepository.findByCity("NonExistent");

        // Assert
        assertEquals(1, newYorkWarehouses.size());
        assertEquals(1, losAngelesWarehouses.size());
        assertEquals(1, berlinWarehouses.size());
        assertEquals(0, nonExistentCityWarehouses.size());

        assertEquals("WH001", newYorkWarehouses.get(0).getCode());
        assertEquals("WH002", losAngelesWarehouses.get(0).getCode());
        assertEquals("WH003", berlinWarehouses.get(0).getCode());
    }

    @Test
    void findByStateProvince_shouldReturnWarehousesInStateProvince() {
        // Act
        List<Warehouse> nyWarehouses = warehouseRepository.findByStateProvince("NY");
        List<Warehouse> caWarehouses = warehouseRepository.findByStateProvince("CA");
        List<Warehouse> berlinWarehouses = warehouseRepository.findByStateProvince("Berlin");
        List<Warehouse> nonExistentStateWarehouses = warehouseRepository.findByStateProvince("NonExistent");

        // Assert
        assertEquals(1, nyWarehouses.size());
        assertEquals(1, caWarehouses.size());
        assertEquals(1, berlinWarehouses.size());
        assertEquals(0, nonExistentStateWarehouses.size());

        assertEquals("WH001", nyWarehouses.get(0).getCode());
        assertEquals("WH002", caWarehouses.get(0).getCode());
        assertEquals("WH003", berlinWarehouses.get(0).getCode());
    }

    @Test
    void findByCountry_shouldReturnWarehousesInCountry() {
        // Act
        List<Warehouse> usaWarehouses = warehouseRepository.findByCountry("USA");
        List<Warehouse> germanyWarehouses = warehouseRepository.findByCountry("Germany");
        List<Warehouse> nonExistentCountryWarehouses = warehouseRepository.findByCountry("NonExistent");

        // Assert
        assertEquals(2, usaWarehouses.size());
        assertEquals(1, germanyWarehouses.size());
        assertEquals(0, nonExistentCountryWarehouses.size());

        assertTrue(usaWarehouses.stream().anyMatch(w -> w.getCode().equals("WH001")));
        assertTrue(usaWarehouses.stream().anyMatch(w -> w.getCode().equals("WH002")));
        assertEquals("WH003", germanyWarehouses.get(0).getCode());
    }

    @Test
    void findByNameContainingIgnoreCase_shouldReturnWarehousesWithNameContainingText() {
        // Act
        List<Warehouse> mainWarehouses = warehouseRepository.findByNameContainingIgnoreCase("main");
        List<Warehouse> warehouseWarehouses = warehouseRepository.findByNameContainingIgnoreCase("warehouse");
        List<Warehouse> westWarehouses = warehouseRepository.findByNameContainingIgnoreCase("west");
        List<Warehouse> nonExistentNameWarehouses = warehouseRepository.findByNameContainingIgnoreCase("nonexistent");

        // Assert
        assertEquals(1, mainWarehouses.size());
        assertEquals(3, warehouseWarehouses.size()); // Should match all warehouses
        assertEquals(1, westWarehouses.size());
        assertEquals(0, nonExistentNameWarehouses.size());

        assertEquals("WH001", mainWarehouses.get(0).getCode());
        assertEquals("WH002", westWarehouses.get(0).getCode());
        assertTrue(warehouseWarehouses.stream().anyMatch(w -> w.getCode().equals("WH001")));
        assertTrue(warehouseWarehouses.stream().anyMatch(w -> w.getCode().equals("WH002")));
        assertTrue(warehouseWarehouses.stream().anyMatch(w -> w.getCode().equals("WH003")));
    }
}