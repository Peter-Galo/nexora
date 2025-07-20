package com.nexora.service.inventory;

import com.nexora.dto.inventory.WarehouseDTO;
import com.nexora.exception.ApplicationException;
import com.nexora.model.inventory.Warehouse;
import com.nexora.repository.inventory.WarehouseRepository;
import com.nexora.service.inventory.impl.WarehouseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseServiceImpl warehouseService;

    private Warehouse testWarehouse1;
    private Warehouse testWarehouse2;
    private WarehouseDTO testWarehouseDTO1;
    private WarehouseDTO testWarehouseDTO2;
    private UUID testUuid1;
    private UUID testUuid2;

    @BeforeEach
    void setUp() {
        testUuid1 = UUID.randomUUID();
        testUuid2 = UUID.randomUUID();

        testWarehouse1 = new Warehouse();
        testWarehouse1.setUuid(testUuid1);
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
        testWarehouse2.setUuid(testUuid2);
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

        testWarehouseDTO1 = new WarehouseDTO();
        testWarehouseDTO1.setCode("WH001");
        testWarehouseDTO1.setName("Main Distribution Center");
        testWarehouseDTO1.setDescription("Primary distribution center");
        testWarehouseDTO1.setAddress("123 Main St");
        testWarehouseDTO1.setCity("Boston");
        testWarehouseDTO1.setStateProvince("Massachusetts");
        testWarehouseDTO1.setPostalCode("02108");
        testWarehouseDTO1.setCountry("USA");
        testWarehouseDTO1.setActive(true);

        testWarehouseDTO2 = new WarehouseDTO();
        testWarehouseDTO2.setCode("WH002");
        testWarehouseDTO2.setName("Secondary Warehouse");
        testWarehouseDTO2.setDescription("Secondary storage facility");
        testWarehouseDTO2.setAddress("456 Oak Ave");
        testWarehouseDTO2.setCity("New York");
        testWarehouseDTO2.setStateProvince("New York");
        testWarehouseDTO2.setPostalCode("10001");
        testWarehouseDTO2.setCountry("USA");
        testWarehouseDTO2.setActive(false);
    }

    @Test
    void testGetAllWarehouses_ShouldReturnAllWarehouses() {
        // Given
        when(warehouseRepository.findAllOrderedByActiveAndName()).thenReturn(Arrays.asList(testWarehouse1, testWarehouse2));

        // When
        List<WarehouseDTO> result = warehouseService.getAllWarehouses();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCode()).isEqualTo("WH001");
        assertThat(result.get(1).getCode()).isEqualTo("WH002");
        verify(warehouseRepository).findAllOrderedByActiveAndName();
    }

    @Test
    void testGetActiveWarehouses_ShouldReturnOnlyActiveWarehouses() {
        // Given
        when(warehouseRepository.findByActiveTrueOrderByName()).thenReturn(Arrays.asList(testWarehouse1));

        // When
        List<WarehouseDTO> result = warehouseService.getActiveWarehouses();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("WH001");
        assertThat(result.get(0).isActive()).isTrue();
        verify(warehouseRepository).findByActiveTrueOrderByName();
    }

    @Test
    void testGetWarehouseById_WhenWarehouseExists_ShouldReturnWarehouse() {
        // Given
        when(warehouseRepository.findById(testUuid1)).thenReturn(Optional.of(testWarehouse1));

        // When
        WarehouseDTO result = warehouseService.getWarehouseById(testUuid1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("WH001");
        assertThat(result.getName()).isEqualTo("Main Distribution Center");
        verify(warehouseRepository).findById(testUuid1);
    }

    @Test
    void testGetWarehouseById_WhenWarehouseDoesNotExist_ShouldThrowException() {
        // Given
        when(warehouseRepository.findById(testUuid1)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> warehouseService.getWarehouseById(testUuid1))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Warehouse not found with id: " + testUuid1);
        verify(warehouseRepository).findById(testUuid1);
    }

    @Test
    void testGetWarehouseByCode_WhenWarehouseExists_ShouldReturnWarehouse() {
        // Given
        when(warehouseRepository.findByCode("WH001")).thenReturn(Optional.of(testWarehouse1));

        // When
        WarehouseDTO result = warehouseService.getWarehouseByCode("WH001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("WH001");
        assertThat(result.getName()).isEqualTo("Main Distribution Center");
        verify(warehouseRepository).findByCode("WH001");
    }

    @Test
    void testGetWarehouseByCode_WhenWarehouseDoesNotExist_ShouldThrowException() {
        // Given
        when(warehouseRepository.findByCode("NONEXISTENT")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> warehouseService.getWarehouseByCode("NONEXISTENT"))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Warehouse not found with code: NONEXISTENT");
        verify(warehouseRepository).findByCode("NONEXISTENT");
    }

    @Test
    void testCreateWarehouse_WhenCodeDoesNotExist_ShouldCreateWarehouse() {
        // Given
        when(warehouseRepository.existsByCode("WH001")).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(invocation -> {
            Warehouse warehouse = invocation.getArgument(0);
            warehouse.setUuid(testUuid1);
            return warehouse;
        });

        // When
        WarehouseDTO result = warehouseService.createWarehouse(testWarehouseDTO1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("WH001");
        assertThat(result.getName()).isEqualTo("Main Distribution Center");
        verify(warehouseRepository).existsByCode("WH001");
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    void testCreateWarehouse_WhenCodeAlreadyExists_ShouldThrowException() {
        // Given
        when(warehouseRepository.existsByCode("WH001")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> warehouseService.createWarehouse(testWarehouseDTO1))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Warehouse with code WH001 already exists");
        verify(warehouseRepository).existsByCode("WH001");
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void testUpdateWarehouse_WhenWarehouseExists_ShouldUpdateWarehouse() {
        // Given
        when(warehouseRepository.findById(testUuid1)).thenReturn(Optional.of(testWarehouse1));
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WarehouseDTO updateDTO = new WarehouseDTO();
        updateDTO.setCode("WH001");
        updateDTO.setName("Updated Warehouse Name");
        updateDTO.setDescription("Updated description");
        updateDTO.setAddress("Updated Address");
        updateDTO.setCity("Updated City");
        updateDTO.setStateProvince("Updated State");
        updateDTO.setPostalCode("12345");
        updateDTO.setCountry("Updated Country");
        updateDTO.setActive(true);

        // When
        WarehouseDTO result = warehouseService.updateWarehouse(testUuid1, updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Warehouse Name");
        assertThat(result.getCity()).isEqualTo("Updated City");
        verify(warehouseRepository).findById(testUuid1);
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    void testUpdateWarehouse_WhenWarehouseDoesNotExist_ShouldThrowException() {
        // Given
        when(warehouseRepository.findById(testUuid1)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> warehouseService.updateWarehouse(testUuid1, testWarehouseDTO1))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Warehouse not found with id: " + testUuid1);
        verify(warehouseRepository).findById(testUuid1);
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void testDeleteWarehouse_WhenWarehouseExists_ShouldDeleteWarehouse() {
        // Given
        when(warehouseRepository.existsById(testUuid1)).thenReturn(true);

        // When
        warehouseService.deleteWarehouse(testUuid1);

        // Then
        verify(warehouseRepository).existsById(testUuid1);
        verify(warehouseRepository).deleteById(testUuid1);
    }

    @Test
    void testDeleteWarehouse_WhenWarehouseDoesNotExist_ShouldThrowException() {
        // Given
        when(warehouseRepository.existsById(testUuid1)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> warehouseService.deleteWarehouse(testUuid1))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Warehouse not found with id: " + testUuid1);
        verify(warehouseRepository).existsById(testUuid1);
        verify(warehouseRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void testDeactivateWarehouse_WhenWarehouseExists_ShouldDeactivateWarehouse() {
        // Given
        when(warehouseRepository.findById(testUuid1)).thenReturn(Optional.of(testWarehouse1));
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        WarehouseDTO result = warehouseService.deactivateWarehouse(testUuid1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isActive()).isFalse();
        verify(warehouseRepository).findById(testUuid1);
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    void testActivateWarehouse_WhenWarehouseExists_ShouldActivateWarehouse() {
        // Given
        testWarehouse2.setActive(false); // Start with inactive warehouse
        when(warehouseRepository.findById(testUuid2)).thenReturn(Optional.of(testWarehouse2));
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        WarehouseDTO result = warehouseService.activateWarehouse(testUuid2);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isActive()).isTrue();
        verify(warehouseRepository).findById(testUuid2);
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    void testGetWarehousesByCity_ShouldReturnWarehousesInCity() {
        // Given
        when(warehouseRepository.findByCity("Boston")).thenReturn(Arrays.asList(testWarehouse1));

        // When
        List<WarehouseDTO> result = warehouseService.getWarehousesByCity("Boston");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCity()).isEqualTo("Boston");
        verify(warehouseRepository).findByCity("Boston");
    }

    @Test
    void testGetWarehousesByStateProvince_ShouldReturnWarehousesInState() {
        // Given
        when(warehouseRepository.findByStateProvince("Massachusetts")).thenReturn(Arrays.asList(testWarehouse1));

        // When
        List<WarehouseDTO> result = warehouseService.getWarehousesByStateProvince("Massachusetts");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStateProvince()).isEqualTo("Massachusetts");
        verify(warehouseRepository).findByStateProvince("Massachusetts");
    }

    @Test
    void testGetWarehousesByCountry_ShouldReturnWarehousesInCountry() {
        // Given
        when(warehouseRepository.findByCountry("USA")).thenReturn(Arrays.asList(testWarehouse1, testWarehouse2));

        // When
        List<WarehouseDTO> result = warehouseService.getWarehousesByCountry("USA");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(dto -> "USA".equals(dto.getCountry()));
        verify(warehouseRepository).findByCountry("USA");
    }

    @Test
    void testSearchWarehousesByName_ShouldReturnMatchingWarehouses() {
        // Given
        when(warehouseRepository.findByNameContainingIgnoreCaseOrderByName("Distribution"))
                .thenReturn(Arrays.asList(testWarehouse1));

        // When
        List<WarehouseDTO> result = warehouseService.searchWarehousesByName("Distribution");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).contains("Distribution");
        verify(warehouseRepository).findByNameContainingIgnoreCaseOrderByName("Distribution");
    }

    @Test
    void testGetAllWarehouses_WhenNoWarehouses_ShouldReturnEmptyList() {
        // Given
        when(warehouseRepository.findAllOrderedByActiveAndName()).thenReturn(Arrays.asList());

        // When
        List<WarehouseDTO> result = warehouseService.getAllWarehouses();

        // Then
        assertThat(result).isEmpty();
        verify(warehouseRepository).findAllOrderedByActiveAndName();
    }

    @Test
    void testGetWarehousesByCity_WhenNoWarehousesInCity_ShouldReturnEmptyList() {
        // Given
        when(warehouseRepository.findByCity("NonExistentCity")).thenReturn(Arrays.asList());

        // When
        List<WarehouseDTO> result = warehouseService.getWarehousesByCity("NonExistentCity");

        // Then
        assertThat(result).isEmpty();
        verify(warehouseRepository).findByCity("NonExistentCity");
    }

    @Test
    void testCreateWarehouse_ShouldSetTimestamps() {
        // Given
        when(warehouseRepository.existsByCode("WH001")).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(invocation -> {
            Warehouse warehouse = invocation.getArgument(0);
            warehouse.setUuid(testUuid1);
            // Verify timestamps are set
            assertThat(warehouse.getCreatedAt()).isNotNull();
            assertThat(warehouse.getUpdatedAt()).isNotNull();
            return warehouse;
        });

        // When
        warehouseService.createWarehouse(testWarehouseDTO1);

        // Then
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    void testUpdateWarehouse_ShouldUpdateTimestamp() {
        // Given
        LocalDateTime originalUpdatedAt = testWarehouse1.getUpdatedAt();
        when(warehouseRepository.findById(testUuid1)).thenReturn(Optional.of(testWarehouse1));
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(invocation -> {
            Warehouse warehouse = invocation.getArgument(0);
            // Verify updated timestamp is changed
            assertThat(warehouse.getUpdatedAt()).isAfter(originalUpdatedAt);
            return warehouse;
        });

        // When
        warehouseService.updateWarehouse(testUuid1, testWarehouseDTO1);

        // Then
        verify(warehouseRepository).save(any(Warehouse.class));
    }
}
