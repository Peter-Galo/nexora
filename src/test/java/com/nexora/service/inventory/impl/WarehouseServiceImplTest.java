package com.nexora.service.inventory.impl;

import com.nexora.dto.inventory.WarehouseDTO;
import com.nexora.exception.ApplicationException;
import com.nexora.model.inventory.Warehouse;
import com.nexora.repository.inventory.WarehouseRepository;
import com.nexora.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the WarehouseServiceImpl class.
 */
@ExtendWith(MockitoExtension.class)
public class WarehouseServiceImplTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseServiceImpl warehouseService;

    private Warehouse warehouse1;
    private Warehouse warehouse2;
    private WarehouseDTO warehouseDTO;

    @BeforeEach
    public void setup() {
        // Create test warehouses
        warehouse1 = TestDataFactory.createSampleWarehouse(1L);
        warehouse2 = TestDataFactory.createSampleWarehouse(2L);
        warehouseDTO = TestDataFactory.createSampleWarehouseDTO(null);
    }

    @Test
    public void testGetAllWarehouses() {
        // Arrange
        when(warehouseRepository.findAll()).thenReturn(List.of(warehouse1, warehouse2));

        // Act
        List<WarehouseDTO> result = warehouseService.getAllWarehouses();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(warehouse1.getId());
        assertThat(result.get(1).getId()).isEqualTo(warehouse2.getId());
        verify(warehouseRepository, times(1)).findAll();
    }

    @Test
    public void testGetActiveWarehouses() {
        // Arrange
        when(warehouseRepository.findByActiveTrue()).thenReturn(List.of(warehouse1, warehouse2));

        // Act
        List<WarehouseDTO> result = warehouseService.getActiveWarehouses();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(warehouse1.getId());
        assertThat(result.get(1).getId()).isEqualTo(warehouse2.getId());
        verify(warehouseRepository, times(1)).findByActiveTrue();
    }

    @Test
    public void testGetWarehouseById_Success() {
        // Arrange
        when(warehouseRepository.findById(anyLong())).thenReturn(Optional.of(warehouse1));

        // Act
        WarehouseDTO result = warehouseService.getWarehouseById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(warehouse1.getId());
        verify(warehouseRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetWarehouseById_NotFound() {
        // Arrange
        when(warehouseRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApplicationException.class, () -> warehouseService.getWarehouseById(1L));
        verify(warehouseRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetWarehouseByCode_Success() {
        // Arrange
        when(warehouseRepository.findByCode(anyString())).thenReturn(Optional.of(warehouse1));

        // Act
        WarehouseDTO result = warehouseService.getWarehouseByCode("TEST-WH-1");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(warehouse1.getId());
        verify(warehouseRepository, times(1)).findByCode("TEST-WH-1");
    }

    @Test
    public void testGetWarehouseByCode_NotFound() {
        // Arrange
        when(warehouseRepository.findByCode(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApplicationException.class, () -> warehouseService.getWarehouseByCode("TEST-WH-1"));
        verify(warehouseRepository, times(1)).findByCode("TEST-WH-1");
    }

    @Test
    public void testCreateWarehouse_Success() {
        // Arrange
        when(warehouseRepository.existsByCode(anyString())).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse1);

        // Act
        WarehouseDTO result = warehouseService.createWarehouse(warehouseDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(warehouse1.getId());
        verify(warehouseRepository, times(1)).existsByCode(warehouseDTO.getCode());
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
    }

    @Test
    public void testCreateWarehouse_CodeExists() {
        // Arrange
        when(warehouseRepository.existsByCode(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(ApplicationException.class, () -> warehouseService.createWarehouse(warehouseDTO));
        verify(warehouseRepository, times(1)).existsByCode(warehouseDTO.getCode());
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    public void testUpdateWarehouse_Success() {
        // Arrange
        when(warehouseRepository.findById(anyLong())).thenReturn(Optional.of(warehouse1));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse1);

        // Create a DTO with updated values
        WarehouseDTO updateDTO = new WarehouseDTO();
        updateDTO.setId(1L);
        updateDTO.setCode("TEST-WH-1");
        updateDTO.setName("Updated Name");
        updateDTO.setAddress("456 New Address");

        // Act
        WarehouseDTO result = warehouseService.updateWarehouse(1L, updateDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(warehouse1.getId());
        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
    }

    @Test
    public void testUpdateWarehouse_NotFound() {
        // Arrange
        when(warehouseRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApplicationException.class, () -> warehouseService.updateWarehouse(1L, warehouseDTO));
        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    public void testUpdateWarehouse_CodeExists() {
        // Arrange
        Warehouse existingWarehouse = TestDataFactory.createSampleWarehouse(1L);
        existingWarehouse.setCode("EXISTING-CODE");
        
        when(warehouseRepository.findById(anyLong())).thenReturn(Optional.of(existingWarehouse));
        when(warehouseRepository.existsByCode(anyString())).thenReturn(true);

        // Create a DTO with a different code
        WarehouseDTO updateDTO = new WarehouseDTO();
        updateDTO.setId(1L);
        updateDTO.setCode("NEW-CODE");
        updateDTO.setName("Updated Name");

        // Act & Assert
        assertThrows(ApplicationException.class, () -> warehouseService.updateWarehouse(1L, updateDTO));
        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).existsByCode("NEW-CODE");
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    public void testDeleteWarehouse_Success() {
        // Arrange
        when(warehouseRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(warehouseRepository).deleteById(anyLong());

        // Act
        warehouseService.deleteWarehouse(1L);

        // Assert
        verify(warehouseRepository, times(1)).existsById(1L);
        verify(warehouseRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteWarehouse_NotFound() {
        // Arrange
        when(warehouseRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        assertThrows(ApplicationException.class, () -> warehouseService.deleteWarehouse(1L));
        verify(warehouseRepository, times(1)).existsById(1L);
        verify(warehouseRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testDeactivateWarehouse_Success() {
        // Arrange
        when(warehouseRepository.findById(anyLong())).thenReturn(Optional.of(warehouse1));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse1);

        // Act
        WarehouseDTO result = warehouseService.deactivateWarehouse(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isActive()).isFalse();
        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
    }

    @Test
    public void testDeactivateWarehouse_NotFound() {
        // Arrange
        when(warehouseRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApplicationException.class, () -> warehouseService.deactivateWarehouse(1L));
        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    public void testActivateWarehouse_Success() {
        // Arrange
        warehouse1.setActive(false);
        when(warehouseRepository.findById(anyLong())).thenReturn(Optional.of(warehouse1));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse1);

        // Act
        WarehouseDTO result = warehouseService.activateWarehouse(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isActive()).isTrue();
        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
    }

    @Test
    public void testActivateWarehouse_NotFound() {
        // Arrange
        when(warehouseRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApplicationException.class, () -> warehouseService.activateWarehouse(1L));
        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    public void testGetWarehousesByCity() {
        // Arrange
        when(warehouseRepository.findByCity(anyString())).thenReturn(List.of(warehouse1, warehouse2));

        // Act
        List<WarehouseDTO> result = warehouseService.getWarehousesByCity("Test City");

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(warehouse1.getId());
        assertThat(result.get(1).getId()).isEqualTo(warehouse2.getId());
        verify(warehouseRepository, times(1)).findByCity("Test City");
    }

    @Test
    public void testGetWarehousesByCountry() {
        // Arrange
        when(warehouseRepository.findByCountry(anyString())).thenReturn(List.of(warehouse1, warehouse2));

        // Act
        List<WarehouseDTO> result = warehouseService.getWarehousesByCountry("Test Country");

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(warehouse1.getId());
        assertThat(result.get(1).getId()).isEqualTo(warehouse2.getId());
        verify(warehouseRepository, times(1)).findByCountry("Test Country");
    }

    @Test
    public void testSearchWarehousesByName() {
        // Arrange
        when(warehouseRepository.findByNameContainingIgnoreCase(anyString())).thenReturn(List.of(warehouse1, warehouse2));

        // Act
        List<WarehouseDTO> result = warehouseService.searchWarehousesByName("Test");

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(warehouse1.getId());
        assertThat(result.get(1).getId()).isEqualTo(warehouse2.getId());
        verify(warehouseRepository, times(1)).findByNameContainingIgnoreCase("Test");
    }
}