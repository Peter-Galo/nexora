package com.nexora.controller.inventory;

import com.nexora.dto.inventory.WarehouseDTO;
import com.nexora.exception.ApplicationException;
import com.nexora.service.inventory.WarehouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class WarehouseControllerTest {

    @Mock
    private WarehouseService warehouseService;

    @InjectMocks
    private WarehouseController warehouseController;

    private WarehouseDTO testWarehouseDTO;
    private UUID warehouseUuid;

    @BeforeEach
    void setUp() {
        warehouseUuid = UUID.randomUUID();
        
        testWarehouseDTO = new WarehouseDTO();
        testWarehouseDTO.setUuid(warehouseUuid);
        testWarehouseDTO.setCode("WH001");
        testWarehouseDTO.setName("Main Distribution Center");
        testWarehouseDTO.setDescription("Primary distribution center");
        testWarehouseDTO.setAddress("123 Main St");
        testWarehouseDTO.setCity("Boston");
        testWarehouseDTO.setStateProvince("Massachusetts");
        testWarehouseDTO.setPostalCode("02108");
        testWarehouseDTO.setCountry("USA");
        testWarehouseDTO.setActive(true);
    }

    @Test
    void testGetAllWarehouses_ShouldReturnWarehouseList() {
        // Given
        List<WarehouseDTO> warehouses = Arrays.asList(testWarehouseDTO);
        when(warehouseService.getAllWarehouses()).thenReturn(warehouses);

        // When
        ResponseEntity<List<WarehouseDTO>> response = warehouseController.getAllWarehouses();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getUuid()).isEqualTo(warehouseUuid);
        assertThat(response.getBody().get(0).getCode()).isEqualTo("WH001");
        assertThat(response.getBody().get(0).getName()).isEqualTo("Main Distribution Center");
        verify(warehouseService).getAllWarehouses();
    }

    @Test
    void testGetActiveWarehouses_ShouldReturnActiveWarehouseList() {
        // Given
        List<WarehouseDTO> activeWarehouses = Arrays.asList(testWarehouseDTO);
        when(warehouseService.getActiveWarehouses()).thenReturn(activeWarehouses);

        // When
        ResponseEntity<List<WarehouseDTO>> response = warehouseController.getActiveWarehouses();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).isActive()).isTrue();
        verify(warehouseService).getActiveWarehouses();
    }

    @Test
    void testGetWarehouseById_WhenWarehouseExists_ShouldReturnWarehouse() {
        // Given
        when(warehouseService.getWarehouseById(warehouseUuid)).thenReturn(testWarehouseDTO);

        // When
        ResponseEntity<WarehouseDTO> response = warehouseController.getWarehouseById(warehouseUuid);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getUuid()).isEqualTo(warehouseUuid);
        assertThat(response.getBody().getCode()).isEqualTo("WH001");
        verify(warehouseService).getWarehouseById(warehouseUuid);
    }

    @Test
    void testGetWarehouseById_WhenWarehouseNotFound_ShouldThrowException() {
        // Given
        when(warehouseService.getWarehouseById(warehouseUuid))
                .thenThrow(new ApplicationException("Warehouse not found with id: " + warehouseUuid, "WAREHOUSE_NOT_FOUND"));

        // When & Then
        assertThatThrownBy(() -> warehouseController.getWarehouseById(warehouseUuid))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Warehouse not found with id: " + warehouseUuid);
        verify(warehouseService).getWarehouseById(warehouseUuid);
    }

    @Test
    void testGetWarehouseByCode_WhenWarehouseExists_ShouldReturnWarehouse() {
        // Given
        when(warehouseService.getWarehouseByCode("WH001")).thenReturn(testWarehouseDTO);

        // When
        ResponseEntity<WarehouseDTO> response = warehouseController.getWarehouseByCode("WH001");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCode()).isEqualTo("WH001");
        verify(warehouseService).getWarehouseByCode("WH001");
    }

    @Test
    void testCreateWarehouse_WithValidData_ShouldReturnCreatedWarehouse() {
        // Given
        WarehouseDTO createDTO = new WarehouseDTO();
        createDTO.setCode("WH002");
        createDTO.setName("Secondary Warehouse");
        createDTO.setCity("New York");
        createDTO.setActive(true);

        WarehouseDTO createdWarehouse = new WarehouseDTO();
        createdWarehouse.setUuid(UUID.randomUUID());
        createdWarehouse.setCode("WH002");
        createdWarehouse.setName("Secondary Warehouse");
        createdWarehouse.setCity("New York");
        createdWarehouse.setActive(true);

        when(warehouseService.createWarehouse(any(WarehouseDTO.class))).thenReturn(createdWarehouse);

        // When
        ResponseEntity<WarehouseDTO> response = warehouseController.createWarehouse(createDTO);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getCode()).isEqualTo("WH002");
        assertThat(response.getBody().getName()).isEqualTo("Secondary Warehouse");
        verify(warehouseService).createWarehouse(createDTO);
    }

    @Test
    void testCreateWarehouse_WithDuplicateCode_ShouldThrowException() {
        // Given
        WarehouseDTO createDTO = new WarehouseDTO();
        createDTO.setCode("WH001");
        createDTO.setName("Duplicate Warehouse");

        when(warehouseService.createWarehouse(any(WarehouseDTO.class)))
                .thenThrow(new ApplicationException("Warehouse with code WH001 already exists", "WAREHOUSE_CODE_EXISTS"));

        // When & Then
        assertThatThrownBy(() -> warehouseController.createWarehouse(createDTO))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Warehouse with code WH001 already exists");
        verify(warehouseService).createWarehouse(createDTO);
    }

    @Test
    void testUpdateWarehouse_WithValidData_ShouldReturnUpdatedWarehouse() {
        // Given
        WarehouseDTO updateDTO = new WarehouseDTO();
        updateDTO.setCode("WH001");
        updateDTO.setName("Updated Warehouse Name");
        updateDTO.setCity("Updated City");

        WarehouseDTO updatedWarehouse = new WarehouseDTO();
        updatedWarehouse.setUuid(warehouseUuid);
        updatedWarehouse.setCode("WH001");
        updatedWarehouse.setName("Updated Warehouse Name");
        updatedWarehouse.setCity("Updated City");

        when(warehouseService.updateWarehouse(eq(warehouseUuid), any(WarehouseDTO.class))).thenReturn(updatedWarehouse);

        // When
        ResponseEntity<WarehouseDTO> response = warehouseController.updateWarehouse(warehouseUuid, updateDTO);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Updated Warehouse Name");
        assertThat(response.getBody().getCity()).isEqualTo("Updated City");
        verify(warehouseService).updateWarehouse(warehouseUuid, updateDTO);
    }

    @Test
    void testDeleteWarehouse_WhenWarehouseExists_ShouldReturn204() {
        // When
        ResponseEntity<Void> response = warehouseController.deleteWarehouse(warehouseUuid);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(warehouseService).deleteWarehouse(warehouseUuid);
    }

    @Test
    void testDeleteWarehouse_WhenWarehouseNotFound_ShouldThrowException() {
        // Given
        doThrow(new ApplicationException("Warehouse not found with id: " + warehouseUuid, "WAREHOUSE_NOT_FOUND"))
                .when(warehouseService).deleteWarehouse(warehouseUuid);

        // When & Then
        assertThatThrownBy(() -> warehouseController.deleteWarehouse(warehouseUuid))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Warehouse not found with id: " + warehouseUuid);
        verify(warehouseService).deleteWarehouse(warehouseUuid);
    }

    @Test
    void testActivateWarehouse_WhenWarehouseExists_ShouldReturnActivatedWarehouse() {
        // Given
        testWarehouseDTO.setActive(true);
        when(warehouseService.activateWarehouse(warehouseUuid)).thenReturn(testWarehouseDTO);

        // When
        ResponseEntity<WarehouseDTO> response = warehouseController.activateWarehouse(warehouseUuid);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isActive()).isTrue();
        verify(warehouseService).activateWarehouse(warehouseUuid);
    }

    @Test
    void testDeactivateWarehouse_WhenWarehouseExists_ShouldReturnDeactivatedWarehouse() {
        // Given
        testWarehouseDTO.setActive(false);
        when(warehouseService.deactivateWarehouse(warehouseUuid)).thenReturn(testWarehouseDTO);

        // When
        ResponseEntity<WarehouseDTO> response = warehouseController.deactivateWarehouse(warehouseUuid);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isActive()).isFalse();
        verify(warehouseService).deactivateWarehouse(warehouseUuid);
    }

    @Test
    void testGetWarehousesByCity_ShouldReturnWarehousesInCity() {
        // Given
        List<WarehouseDTO> warehouses = Arrays.asList(testWarehouseDTO);
        when(warehouseService.getWarehousesByCity("Boston")).thenReturn(warehouses);

        // When
        ResponseEntity<List<WarehouseDTO>> response = warehouseController.getWarehousesByCity("Boston");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getCity()).isEqualTo("Boston");
        verify(warehouseService).getWarehousesByCity("Boston");
    }

    @Test
    void testGetWarehousesByStateProvince_ShouldReturnWarehousesInState() {
        // Given
        List<WarehouseDTO> warehouses = Arrays.asList(testWarehouseDTO);
        when(warehouseService.getWarehousesByStateProvince("Massachusetts")).thenReturn(warehouses);

        // When
        ResponseEntity<List<WarehouseDTO>> response = warehouseController.getWarehousesByStateProvince("Massachusetts");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getStateProvince()).isEqualTo("Massachusetts");
        verify(warehouseService).getWarehousesByStateProvince("Massachusetts");
    }

    @Test
    void testGetWarehousesByCountry_ShouldReturnWarehousesInCountry() {
        // Given
        List<WarehouseDTO> warehouses = Arrays.asList(testWarehouseDTO);
        when(warehouseService.getWarehousesByCountry("USA")).thenReturn(warehouses);

        // When
        ResponseEntity<List<WarehouseDTO>> response = warehouseController.getWarehousesByCountry("USA");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getCountry()).isEqualTo("USA");
        verify(warehouseService).getWarehousesByCountry("USA");
    }

    @Test
    void testSearchWarehousesByName_ShouldReturnMatchingWarehouses() {
        // Given
        List<WarehouseDTO> warehouses = Arrays.asList(testWarehouseDTO);
        when(warehouseService.searchWarehousesByName("Distribution")).thenReturn(warehouses);

        // When
        ResponseEntity<List<WarehouseDTO>> response = warehouseController.searchWarehousesByName("Distribution");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getName()).contains("Distribution");
        verify(warehouseService).searchWarehousesByName("Distribution");
    }
}