package com.nexora.service.inventory;

import com.nexora.dto.inventory.WarehouseDTO;
import com.nexora.exception.ApplicationException;
import com.nexora.model.inventory.Warehouse;
import com.nexora.repository.inventory.WarehouseRepository;
import com.nexora.service.inventory.impl.WarehouseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    private WarehouseService warehouseService;

    private Warehouse testWarehouse;
    private WarehouseDTO testWarehouseDTO;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        warehouseService = new WarehouseServiceImpl(warehouseRepository);
        
        // Create test data
        testUuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        
        // Create test warehouse entity
        testWarehouse = new Warehouse("WH001", "Test Warehouse");
        testWarehouse.setUuid(testUuid);
        testWarehouse.setDescription("Test description");
        testWarehouse.setAddress("123 Test St");
        testWarehouse.setCity("Test City");
        testWarehouse.setStateProvince("Test State");
        testWarehouse.setPostalCode("12345");
        testWarehouse.setCountry("Test Country");
        testWarehouse.setCreatedAt(now);
        testWarehouse.setUpdatedAt(now);
        testWarehouse.setActive(true);
        
        // Create test warehouse DTO
        testWarehouseDTO = new WarehouseDTO(
                testUuid,
                "WH001",
                "Test Warehouse",
                "Test description",
                "123 Test St",
                "Test City",
                "Test State",
                "12345",
                "Test Country",
                now,
                now,
                true
        );
    }

    @Test
    void getAllWarehouses_shouldReturnAllWarehouses() {
        // Arrange
        when(warehouseRepository.findAll()).thenReturn(Arrays.asList(testWarehouse));

        // Act
        List<WarehouseDTO> result = warehouseService.getAllWarehouses();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testWarehouse.getUuid(), result.get(0).getUuid());
        assertEquals(testWarehouse.getCode(), result.get(0).getCode());
        assertEquals(testWarehouse.getName(), result.get(0).getName());
        verify(warehouseRepository).findAll();
    }

    @Test
    void getActiveWarehouses_shouldReturnOnlyActiveWarehouses() {
        // Arrange
        when(warehouseRepository.findByActiveTrue()).thenReturn(Arrays.asList(testWarehouse));

        // Act
        List<WarehouseDTO> result = warehouseService.getActiveWarehouses();

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
        assertEquals(testWarehouse.getUuid(), result.get(0).getUuid());
        verify(warehouseRepository).findByActiveTrue();
    }

    @Test
    void getWarehouseById_withExistingId_shouldReturnWarehouse() {
        // Arrange
        when(warehouseRepository.findById(testUuid)).thenReturn(Optional.of(testWarehouse));

        // Act
        WarehouseDTO result = warehouseService.getWarehouseById(testUuid);

        // Assert
        assertNotNull(result);
        assertEquals(testUuid, result.getUuid());
        assertEquals(testWarehouse.getCode(), result.getCode());
        verify(warehouseRepository).findById(testUuid);
    }

    @Test
    void getWarehouseById_withNonExistingId_shouldThrowException() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        when(warehouseRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> warehouseService.getWarehouseById(nonExistingId)
        );

        assertEquals("Warehouse not found with id: " + nonExistingId, exception.getMessage());
        assertEquals("WAREHOUSE_NOT_FOUND", exception.getCode());
        verify(warehouseRepository).findById(nonExistingId);
    }

    @Test
    void getWarehouseByCode_withExistingCode_shouldReturnWarehouse() {
        // Arrange
        String code = "WH001";
        when(warehouseRepository.findByCode(code)).thenReturn(Optional.of(testWarehouse));

        // Act
        WarehouseDTO result = warehouseService.getWarehouseByCode(code);

        // Assert
        assertNotNull(result);
        assertEquals(code, result.getCode());
        assertEquals(testWarehouse.getUuid(), result.getUuid());
        verify(warehouseRepository).findByCode(code);
    }

    @Test
    void getWarehouseByCode_withNonExistingCode_shouldThrowException() {
        // Arrange
        String nonExistingCode = "NONEXISTENT";
        when(warehouseRepository.findByCode(nonExistingCode)).thenReturn(Optional.empty());

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> warehouseService.getWarehouseByCode(nonExistingCode)
        );

        assertEquals("Warehouse not found with code: " + nonExistingCode, exception.getMessage());
        assertEquals("WAREHOUSE_NOT_FOUND", exception.getCode());
        verify(warehouseRepository).findByCode(nonExistingCode);
    }

    @Test
    void createWarehouse_withValidData_shouldCreateAndReturnWarehouse() {
        // Arrange
        WarehouseDTO newWarehouseDTO = new WarehouseDTO("WH002", "New Warehouse");
        newWarehouseDTO.setDescription("New description");
        newWarehouseDTO.setAddress("456 New St");
        newWarehouseDTO.setCity("New City");
        newWarehouseDTO.setStateProvince("New State");
        newWarehouseDTO.setPostalCode("67890");
        newWarehouseDTO.setCountry("New Country");
        
        Warehouse savedWarehouse = new Warehouse("WH002", "New Warehouse");
        savedWarehouse.setUuid(UUID.randomUUID());
        savedWarehouse.setDescription("New description");
        savedWarehouse.setAddress("456 New St");
        savedWarehouse.setCity("New City");
        savedWarehouse.setStateProvince("New State");
        savedWarehouse.setPostalCode("67890");
        savedWarehouse.setCountry("New Country");
        savedWarehouse.setCreatedAt(LocalDateTime.now());
        savedWarehouse.setUpdatedAt(LocalDateTime.now());
        
        when(warehouseRepository.existsByCode(newWarehouseDTO.getCode())).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(savedWarehouse);

        // Act
        WarehouseDTO result = warehouseService.createWarehouse(newWarehouseDTO);

        // Assert
        assertNotNull(result);
        assertEquals(savedWarehouse.getUuid(), result.getUuid());
        assertEquals(newWarehouseDTO.getCode(), result.getCode());
        assertEquals(newWarehouseDTO.getName(), result.getName());
        
        // Verify warehouse was saved with correct data
        ArgumentCaptor<Warehouse> warehouseCaptor = ArgumentCaptor.forClass(Warehouse.class);
        verify(warehouseRepository).save(warehouseCaptor.capture());
        Warehouse capturedWarehouse = warehouseCaptor.getValue();
        
        assertEquals(newWarehouseDTO.getCode(), capturedWarehouse.getCode());
        assertEquals(newWarehouseDTO.getName(), capturedWarehouse.getName());
        assertEquals(newWarehouseDTO.getDescription(), capturedWarehouse.getDescription());
        assertEquals(newWarehouseDTO.getAddress(), capturedWarehouse.getAddress());
        assertEquals(newWarehouseDTO.getCity(), capturedWarehouse.getCity());
        assertEquals(newWarehouseDTO.getStateProvince(), capturedWarehouse.getStateProvince());
        assertEquals(newWarehouseDTO.getPostalCode(), capturedWarehouse.getPostalCode());
        assertEquals(newWarehouseDTO.getCountry(), capturedWarehouse.getCountry());
        assertNotNull(capturedWarehouse.getCreatedAt());
        assertNotNull(capturedWarehouse.getUpdatedAt());
    }

    @Test
    void createWarehouse_withExistingCode_shouldThrowException() {
        // Arrange
        WarehouseDTO newWarehouseDTO = new WarehouseDTO("WH001", "New Warehouse");
        when(warehouseRepository.existsByCode(newWarehouseDTO.getCode())).thenReturn(true);

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> warehouseService.createWarehouse(newWarehouseDTO)
        );

        assertEquals("Warehouse with code WH001 already exists", exception.getMessage());
        assertEquals("WAREHOUSE_CODE_EXISTS", exception.getCode());
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void updateWarehouse_withValidData_shouldUpdateAndReturnWarehouse() {
        // Arrange
        WarehouseDTO updateDTO = new WarehouseDTO(
                testUuid,
                "WH001",
                "Updated Warehouse",
                "Updated description",
                "456 Updated St",
                "Updated City",
                "Updated State",
                "67890",
                "Updated Country",
                null,
                null,
                true
        );
        
        Warehouse updatedWarehouse = new Warehouse("WH001", "Updated Warehouse");
        updatedWarehouse.setUuid(testUuid);
        updatedWarehouse.setDescription("Updated description");
        updatedWarehouse.setAddress("456 Updated St");
        updatedWarehouse.setCity("Updated City");
        updatedWarehouse.setStateProvince("Updated State");
        updatedWarehouse.setPostalCode("67890");
        updatedWarehouse.setCountry("Updated Country");
        updatedWarehouse.setCreatedAt(testWarehouse.getCreatedAt());
        updatedWarehouse.setUpdatedAt(LocalDateTime.now());
        
        when(warehouseRepository.findById(testUuid)).thenReturn(Optional.of(testWarehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(updatedWarehouse);

        // Act
        WarehouseDTO result = warehouseService.updateWarehouse(testUuid, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testUuid, result.getUuid());
        assertEquals(updateDTO.getName(), result.getName());
        assertEquals(updateDTO.getDescription(), result.getDescription());
        assertEquals(updateDTO.getAddress(), result.getAddress());
        assertEquals(updateDTO.getCity(), result.getCity());
        assertEquals(updateDTO.getStateProvince(), result.getStateProvince());
        assertEquals(updateDTO.getPostalCode(), result.getPostalCode());
        assertEquals(updateDTO.getCountry(), result.getCountry());
        
        // Verify warehouse was saved with correct data
        ArgumentCaptor<Warehouse> warehouseCaptor = ArgumentCaptor.forClass(Warehouse.class);
        verify(warehouseRepository).save(warehouseCaptor.capture());
        Warehouse capturedWarehouse = warehouseCaptor.getValue();
        
        assertEquals(updateDTO.getName(), capturedWarehouse.getName());
        assertEquals(updateDTO.getDescription(), capturedWarehouse.getDescription());
        assertEquals(updateDTO.getAddress(), capturedWarehouse.getAddress());
        assertEquals(updateDTO.getCity(), capturedWarehouse.getCity());
        assertEquals(updateDTO.getStateProvince(), capturedWarehouse.getStateProvince());
        assertEquals(updateDTO.getPostalCode(), capturedWarehouse.getPostalCode());
        assertEquals(updateDTO.getCountry(), capturedWarehouse.getCountry());
    }

    @Test
    void updateWarehouse_withNonExistingId_shouldThrowException() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        WarehouseDTO updateDTO = new WarehouseDTO("WH001", "Updated Warehouse");
        when(warehouseRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> warehouseService.updateWarehouse(nonExistingId, updateDTO)
        );

        assertEquals("Warehouse not found with id: " + nonExistingId, exception.getMessage());
        assertEquals("WAREHOUSE_NOT_FOUND", exception.getCode());
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void updateWarehouse_withExistingCode_shouldThrowException() {
        // Arrange
        String existingCode = "WH002";
        WarehouseDTO updateDTO = new WarehouseDTO(
                testUuid,
                existingCode, // Different from original code
                "Updated Warehouse",
                "Updated description",
                "456 Updated St",
                "Updated City",
                "Updated State",
                "67890",
                "Updated Country",
                null,
                null,
                true
        );
        
        when(warehouseRepository.findById(testUuid)).thenReturn(Optional.of(testWarehouse));
        when(warehouseRepository.existsByCode(existingCode)).thenReturn(true);

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> warehouseService.updateWarehouse(testUuid, updateDTO)
        );

        assertEquals("Warehouse with code WH002 already exists", exception.getMessage());
        assertEquals("WAREHOUSE_CODE_EXISTS", exception.getCode());
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void deleteWarehouse_withExistingId_shouldDeleteWarehouse() {
        // Arrange
        when(warehouseRepository.existsById(testUuid)).thenReturn(true);

        // Act
        warehouseService.deleteWarehouse(testUuid);

        // Assert
        verify(warehouseRepository).deleteById(testUuid);
    }

    @Test
    void deleteWarehouse_withNonExistingId_shouldThrowException() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        when(warehouseRepository.existsById(nonExistingId)).thenReturn(false);

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> warehouseService.deleteWarehouse(nonExistingId)
        );

        assertEquals("Warehouse not found with id: " + nonExistingId, exception.getMessage());
        assertEquals("WAREHOUSE_NOT_FOUND", exception.getCode());
        verify(warehouseRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void deactivateWarehouse_withExistingId_shouldDeactivateAndReturnWarehouse() {
        // Arrange
        when(warehouseRepository.findById(testUuid)).thenReturn(Optional.of(testWarehouse));
        
        Warehouse deactivatedWarehouse = new Warehouse(testWarehouse.getCode(), testWarehouse.getName());
        deactivatedWarehouse.setUuid(testUuid);
        deactivatedWarehouse.setDescription(testWarehouse.getDescription());
        deactivatedWarehouse.setAddress(testWarehouse.getAddress());
        deactivatedWarehouse.setCity(testWarehouse.getCity());
        deactivatedWarehouse.setStateProvince(testWarehouse.getStateProvince());
        deactivatedWarehouse.setPostalCode(testWarehouse.getPostalCode());
        deactivatedWarehouse.setCountry(testWarehouse.getCountry());
        deactivatedWarehouse.setCreatedAt(testWarehouse.getCreatedAt());
        deactivatedWarehouse.setUpdatedAt(LocalDateTime.now());
        deactivatedWarehouse.setActive(false);
        
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(deactivatedWarehouse);

        // Act
        WarehouseDTO result = warehouseService.deactivateWarehouse(testUuid);

        // Assert
        assertNotNull(result);
        assertFalse(result.isActive());
        
        // Verify warehouse was saved with active=false
        ArgumentCaptor<Warehouse> warehouseCaptor = ArgumentCaptor.forClass(Warehouse.class);
        verify(warehouseRepository).save(warehouseCaptor.capture());
        Warehouse capturedWarehouse = warehouseCaptor.getValue();
        
        assertFalse(capturedWarehouse.isActive());
    }

    @Test
    void activateWarehouse_withExistingId_shouldActivateAndReturnWarehouse() {
        // Arrange
        testWarehouse.setActive(false); // Start with inactive warehouse
        when(warehouseRepository.findById(testUuid)).thenReturn(Optional.of(testWarehouse));
        
        Warehouse activatedWarehouse = new Warehouse(testWarehouse.getCode(), testWarehouse.getName());
        activatedWarehouse.setUuid(testUuid);
        activatedWarehouse.setDescription(testWarehouse.getDescription());
        activatedWarehouse.setAddress(testWarehouse.getAddress());
        activatedWarehouse.setCity(testWarehouse.getCity());
        activatedWarehouse.setStateProvince(testWarehouse.getStateProvince());
        activatedWarehouse.setPostalCode(testWarehouse.getPostalCode());
        activatedWarehouse.setCountry(testWarehouse.getCountry());
        activatedWarehouse.setCreatedAt(testWarehouse.getCreatedAt());
        activatedWarehouse.setUpdatedAt(LocalDateTime.now());
        activatedWarehouse.setActive(true);
        
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(activatedWarehouse);

        // Act
        WarehouseDTO result = warehouseService.activateWarehouse(testUuid);

        // Assert
        assertNotNull(result);
        assertTrue(result.isActive());
        
        // Verify warehouse was saved with active=true
        ArgumentCaptor<Warehouse> warehouseCaptor = ArgumentCaptor.forClass(Warehouse.class);
        verify(warehouseRepository).save(warehouseCaptor.capture());
        Warehouse capturedWarehouse = warehouseCaptor.getValue();
        
        assertTrue(capturedWarehouse.isActive());
    }

    @Test
    void getWarehousesByCity_shouldReturnWarehousesInCity() {
        // Arrange
        String city = "Test City";
        when(warehouseRepository.findByCity(city)).thenReturn(Arrays.asList(testWarehouse));

        // Act
        List<WarehouseDTO> result = warehouseService.getWarehousesByCity(city);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testWarehouse.getUuid(), result.get(0).getUuid());
        assertEquals(city, result.get(0).getCity());
        verify(warehouseRepository).findByCity(city);
    }

    @Test
    void getWarehousesByStateProvince_shouldReturnWarehousesInStateProvince() {
        // Arrange
        String stateProvince = "Test State";
        when(warehouseRepository.findByStateProvince(stateProvince)).thenReturn(Arrays.asList(testWarehouse));

        // Act
        List<WarehouseDTO> result = warehouseService.getWarehousesByStateProvince(stateProvince);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testWarehouse.getUuid(), result.get(0).getUuid());
        assertEquals(stateProvince, result.get(0).getStateProvince());
        verify(warehouseRepository).findByStateProvince(stateProvince);
    }

    @Test
    void getWarehousesByCountry_shouldReturnWarehousesInCountry() {
        // Arrange
        String country = "Test Country";
        when(warehouseRepository.findByCountry(country)).thenReturn(Arrays.asList(testWarehouse));

        // Act
        List<WarehouseDTO> result = warehouseService.getWarehousesByCountry(country);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testWarehouse.getUuid(), result.get(0).getUuid());
        assertEquals(country, result.get(0).getCountry());
        verify(warehouseRepository).findByCountry(country);
    }

    @Test
    void searchWarehousesByName_shouldReturnWarehousesWithNameContainingText() {
        // Arrange
        String searchText = "Test";
        when(warehouseRepository.findByNameContainingIgnoreCase(searchText)).thenReturn(Arrays.asList(testWarehouse));

        // Act
        List<WarehouseDTO> result = warehouseService.searchWarehousesByName(searchText);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testWarehouse.getUuid(), result.get(0).getUuid());
        assertTrue(result.get(0).getName().contains(searchText));
        verify(warehouseRepository).findByNameContainingIgnoreCase(searchText);
    }
}