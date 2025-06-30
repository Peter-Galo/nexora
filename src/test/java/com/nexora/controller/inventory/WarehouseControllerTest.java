package com.nexora.controller.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexora.dto.inventory.WarehouseDTO;
import com.nexora.exception.ApplicationException;
import com.nexora.security.SecurityConfig;
import com.nexora.security.TestSecurityConfig;
import com.nexora.service.inventory.WarehouseService;
import org.springframework.context.annotation.Import;
import com.nexora.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean; // TODO: Replace with non-deprecated alternative when available
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the WarehouseController class.
 */
@WebMvcTest(controllers = WarehouseController.class)
@Import(TestSecurityConfig.class)
public class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WarehouseService warehouseService;

    private WarehouseDTO warehouseDTO1;
    private WarehouseDTO warehouseDTO2;

    @BeforeEach
    public void setup() {
        // Create test warehouses
        warehouseDTO1 = TestDataFactory.createSampleWarehouseDTO(1L);
        warehouseDTO2 = TestDataFactory.createSampleWarehouseDTO(2L);
    }

    @Test
    public void testGetAllWarehouses() throws Exception {
        // Arrange
        when(warehouseService.getAllWarehouses()).thenReturn(List.of(warehouseDTO1, warehouseDTO2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventory/warehouses")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    public void testGetActiveWarehouses() throws Exception {
        // Arrange
        when(warehouseService.getActiveWarehouses()).thenReturn(List.of(warehouseDTO1, warehouseDTO2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventory/warehouses/active")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    public void testGetWarehouseById_Success() throws Exception {
        // Arrange
        when(warehouseService.getWarehouseById(anyLong())).thenReturn(warehouseDTO1);

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventory/warehouses/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(warehouseDTO1.getName())));
    }

    @Test
    public void testGetWarehouseById_NotFound() throws Exception {
        // Arrange
        when(warehouseService.getWarehouseById(anyLong())).thenThrow(new ApplicationException("Warehouse not found", "WAREHOUSE_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventory/warehouses/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetWarehouseByCode_Success() throws Exception {
        // Arrange
        when(warehouseService.getWarehouseByCode(anyString())).thenReturn(warehouseDTO1);

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventory/warehouses/code/TEST-WH-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.code", is(warehouseDTO1.getCode())));
    }

    @Test
    public void testGetWarehouseByCode_NotFound() throws Exception {
        // Arrange
        when(warehouseService.getWarehouseByCode(anyString())).thenThrow(new ApplicationException("Warehouse not found", "WAREHOUSE_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventory/warehouses/code/NONEXISTENT")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWarehouse_Success() throws Exception {
        // Arrange
        WarehouseDTO newWarehouseDTO = new WarehouseDTO();
        newWarehouseDTO.setCode("NEW-WH");
        newWarehouseDTO.setName("New Warehouse");

        when(warehouseService.createWarehouse(any(WarehouseDTO.class))).thenReturn(warehouseDTO1);

        // Act & Assert
        mockMvc.perform(post("/api/v1/inventory/warehouses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newWarehouseDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(warehouseDTO1.getName())));
    }

    @Test
    public void testCreateWarehouse_BadRequest() throws Exception {
        // Arrange
        WarehouseDTO invalidWarehouseDTO = new WarehouseDTO();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/api/v1/inventory/warehouses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidWarehouseDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWarehouse_Conflict() throws Exception {
        // Arrange
        WarehouseDTO existingWarehouseDTO = new WarehouseDTO();
        existingWarehouseDTO.setCode("EXISTING-CODE");
        existingWarehouseDTO.setName("Existing Warehouse");

        when(warehouseService.createWarehouse(any(WarehouseDTO.class))).thenThrow(new ApplicationException("Warehouse code already exists", "WAREHOUSE_CODE_EXISTS"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/inventory/warehouses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(existingWarehouseDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateWarehouse_Success() throws Exception {
        // Arrange
        WarehouseDTO updateWarehouseDTO = new WarehouseDTO();
        updateWarehouseDTO.setCode("UPDATE-WH");
        updateWarehouseDTO.setName("Updated Warehouse");
        updateWarehouseDTO.setAddress("456 New Address");

        when(warehouseService.updateWarehouse(anyLong(), any(WarehouseDTO.class))).thenReturn(warehouseDTO1);

        // Act & Assert
        mockMvc.perform(put("/api/v1/inventory/warehouses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateWarehouseDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(warehouseDTO1.getName())));
    }

    @Test
    public void testUpdateWarehouse_NotFound() throws Exception {
        // Arrange
        WarehouseDTO updateWarehouseDTO = new WarehouseDTO();
        updateWarehouseDTO.setCode("UPDATE-WH");
        updateWarehouseDTO.setName("Updated Warehouse");

        when(warehouseService.updateWarehouse(anyLong(), any(WarehouseDTO.class))).thenThrow(new ApplicationException("Warehouse not found", "WAREHOUSE_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/inventory/warehouses/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateWarehouseDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteWarehouse_Success() throws Exception {
        // Arrange
        doNothing().when(warehouseService).deleteWarehouse(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/inventory/warehouses/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteWarehouse_NotFound() throws Exception {
        // Arrange
        doThrow(new ApplicationException("Warehouse not found", "WAREHOUSE_NOT_FOUND")).when(warehouseService).deleteWarehouse(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/inventory/warehouses/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeactivateWarehouse_Success() throws Exception {
        // Arrange
        when(warehouseService.deactivateWarehouse(anyLong())).thenReturn(warehouseDTO1);

        // Act & Assert
        mockMvc.perform(put("/api/v1/inventory/warehouses/1/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(warehouseDTO1.getName())));
    }

    @Test
    public void testDeactivateWarehouse_NotFound() throws Exception {
        // Arrange
        when(warehouseService.deactivateWarehouse(anyLong())).thenThrow(new ApplicationException("Warehouse not found", "WAREHOUSE_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/inventory/warehouses/999/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testActivateWarehouse_Success() throws Exception {
        // Arrange
        when(warehouseService.activateWarehouse(anyLong())).thenReturn(warehouseDTO1);

        // Act & Assert
        mockMvc.perform(put("/api/v1/inventory/warehouses/1/activate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(warehouseDTO1.getName())));
    }

    @Test
    public void testActivateWarehouse_NotFound() throws Exception {
        // Arrange
        when(warehouseService.activateWarehouse(anyLong())).thenThrow(new ApplicationException("Warehouse not found", "WAREHOUSE_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/inventory/warehouses/999/activate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetWarehousesByCity() throws Exception {
        // Arrange
        when(warehouseService.getWarehousesByCity(anyString())).thenReturn(List.of(warehouseDTO1, warehouseDTO2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventory/warehouses/city/Test City")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    public void testGetWarehousesByCountry() throws Exception {
        // Arrange
        when(warehouseService.getWarehousesByCountry(anyString())).thenReturn(List.of(warehouseDTO1, warehouseDTO2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventory/warehouses/country/Test Country")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    public void testSearchWarehousesByName() throws Exception {
        // Arrange
        when(warehouseService.searchWarehousesByName(anyString())).thenReturn(List.of(warehouseDTO1, warehouseDTO2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventory/warehouses/search?name=Test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }
}
