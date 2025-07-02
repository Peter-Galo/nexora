package com.nexora.controller.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexora.dto.inventory.WarehouseDTO;
import com.nexora.exception.ApplicationException;
import com.nexora.exception.GlobalExceptionHandler;
import com.nexora.service.inventory.WarehouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WarehouseControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WarehouseService warehouseService;

    @InjectMocks
    private WarehouseController warehouseController;

    private ObjectMapper objectMapper;
    private UUID testUuid;
    private WarehouseDTO testWarehouseDTO;
    private List<WarehouseDTO> testWarehouseDTOList;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(warehouseController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        testUuid = UUID.randomUUID();
        now = LocalDateTime.now();

        testWarehouseDTO = buildWarehouseDTO(testUuid, "WH001", "Test Warehouse");
        testWarehouseDTOList = Arrays.asList(
                testWarehouseDTO,
                buildWarehouseDTO(UUID.randomUUID(), "WH002", "Another Warehouse")
        );
    }

    private WarehouseDTO buildWarehouseDTO(UUID uuid, String code, String name) {
        return new WarehouseDTO(
                uuid,
                code,
                name,
                "Test description",
                "123 Test St",
                "Test City",         // <-- Use "Test City"
                "Test State",        // <-- Use "Test State"
                "12345",
                "Test Country",      // <-- Use "Test Country"
                now,
                now,
                true
        );
    }

    private void assertBadRequest(RequestBuilder request, String errorCode, String messagePart) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(errorCode)))
                .andExpect(jsonPath("$.messages[0]", containsString(messagePart)));
    }

    @Test
    void getAllWarehouses_shouldReturnAllWarehouses() throws Exception {
        when(warehouseService.getAllWarehouses()).thenReturn(testWarehouseDTOList);

        mockMvc.perform(get("/api/v1/inventory/warehouses"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].uuid", is(testWarehouseDTO.getUuid().toString())))
                .andExpect(jsonPath("$[0].code", is(testWarehouseDTO.getCode())))
                .andExpect(jsonPath("$[0].name", is(testWarehouseDTO.getName())))
                .andExpect(jsonPath("$[1].code", is("WH002")));

        verify(warehouseService).getAllWarehouses();
    }

    @Test
    void getActiveWarehouses_shouldReturnActiveWarehouses() throws Exception {
        when(warehouseService.getActiveWarehouses()).thenReturn(testWarehouseDTOList);

        mockMvc.perform(get("/api/v1/inventory/warehouses/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].active", is(true)))
                .andExpect(jsonPath("$[1].active", is(true)));

        verify(warehouseService).getActiveWarehouses();
    }

    @Test
    void getWarehouseById_withExistingId_shouldReturnWarehouse() throws Exception {
        when(warehouseService.getWarehouseById(testUuid)).thenReturn(testWarehouseDTO);

        mockMvc.perform(get("/api/v1/inventory/warehouses/{id}", testUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid", is(testWarehouseDTO.getUuid().toString())))
                .andExpect(jsonPath("$.code", is(testWarehouseDTO.getCode())))
                .andExpect(jsonPath("$.name", is(testWarehouseDTO.getName())));

        verify(warehouseService).getWarehouseById(testUuid);
    }

    @Test
    void getWarehouseById_withNonExistingId_shouldReturnBadRequest() throws Exception {
        when(warehouseService.getWarehouseById(testUuid))
                .thenThrow(new ApplicationException("Warehouse not found with id: " + testUuid, "WAREHOUSE_NOT_FOUND"));

        assertBadRequest(get("/api/v1/inventory/warehouses/{id}", testUuid), "WAREHOUSE_NOT_FOUND", "Warehouse not found");
        verify(warehouseService).getWarehouseById(testUuid);
    }

    @Test
    void getWarehouseByCode_withExistingCode_shouldReturnWarehouse() throws Exception {
        String code = "WH001";
        when(warehouseService.getWarehouseByCode(code)).thenReturn(testWarehouseDTO);

        mockMvc.perform(get("/api/v1/inventory/warehouses/code/{code}", code))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(code)))
                .andExpect(jsonPath("$.name", is(testWarehouseDTO.getName())));

        verify(warehouseService).getWarehouseByCode(code);
    }

    @Test
    void getWarehouseByCode_withNonExistingCode_shouldReturnBadRequest() throws Exception {
        String code = "NONEXISTENT";
        when(warehouseService.getWarehouseByCode(code))
                .thenThrow(new ApplicationException("Warehouse not found with code: " + code, "WAREHOUSE_NOT_FOUND"));

        assertBadRequest(get("/api/v1/inventory/warehouses/code/{code}", code), "WAREHOUSE_NOT_FOUND", "Warehouse not found");
        verify(warehouseService).getWarehouseByCode(code);
    }

    @Test
    void createWarehouse_withValidData_shouldCreateAndReturnWarehouse() throws Exception {
        WarehouseDTO newWarehouseDTO = new WarehouseDTO("WH003", "New Warehouse");
        newWarehouseDTO.setDescription("New description");
        newWarehouseDTO.setAddress("789 New St");
        newWarehouseDTO.setCity("New City");
        newWarehouseDTO.setStateProvince("New State");
        newWarehouseDTO.setPostalCode("54321");
        newWarehouseDTO.setCountry("New Country");

        WarehouseDTO createdWarehouseDTO = buildWarehouseDTO(UUID.randomUUID(), newWarehouseDTO.getCode(), newWarehouseDTO.getName());
        createdWarehouseDTO.setDescription(newWarehouseDTO.getDescription());
        createdWarehouseDTO.setAddress(newWarehouseDTO.getAddress());
        createdWarehouseDTO.setCity(newWarehouseDTO.getCity());
        createdWarehouseDTO.setStateProvince(newWarehouseDTO.getStateProvince());
        createdWarehouseDTO.setPostalCode(newWarehouseDTO.getPostalCode());
        createdWarehouseDTO.setCountry(newWarehouseDTO.getCountry());

        when(warehouseService.createWarehouse(any(WarehouseDTO.class))).thenReturn(createdWarehouseDTO);

        mockMvc.perform(post("/api/v1/inventory/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newWarehouseDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid", notNullValue()))
                .andExpect(jsonPath("$.code", is(newWarehouseDTO.getCode())))
                .andExpect(jsonPath("$.name", is(newWarehouseDTO.getName())))
                .andExpect(jsonPath("$.address", is(newWarehouseDTO.getAddress())));

        verify(warehouseService).createWarehouse(any(WarehouseDTO.class));
    }

    @Test
    void createWarehouse_withExistingCode_shouldReturnBadRequest() throws Exception {
        WarehouseDTO newWarehouseDTO = new WarehouseDTO("WH001", "New Warehouse");

        when(warehouseService.createWarehouse(any(WarehouseDTO.class)))
                .thenThrow(new ApplicationException("Warehouse with code WH001 already exists", "WAREHOUSE_CODE_EXISTS"));

        assertBadRequest(
                post("/api/v1/inventory/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newWarehouseDTO)),
                "WAREHOUSE_CODE_EXISTS",
                "already exists"
        );
        verify(warehouseService).createWarehouse(any(WarehouseDTO.class));
    }

    @Test
    void updateWarehouse_withValidData_shouldUpdateAndReturnWarehouse() throws Exception {
        WarehouseDTO updateDTO = buildWarehouseDTO(testUuid, "WH001", "Updated Warehouse");
        updateDTO.setDescription("Updated description");
        updateDTO.setAddress("456 Updated St");
        updateDTO.setCity("Updated City");
        updateDTO.setStateProvince("Updated State");
        updateDTO.setPostalCode("54321");
        updateDTO.setCountry("Updated Country");

        when(warehouseService.updateWarehouse(eq(testUuid), any(WarehouseDTO.class))).thenReturn(updateDTO);

        mockMvc.perform(put("/api/v1/inventory/warehouses/{id}", testUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid", is(testUuid.toString())))
                .andExpect(jsonPath("$.name", is("Updated Warehouse")))
                .andExpect(jsonPath("$.description", is("Updated description")))
                .andExpect(jsonPath("$.address", is("456 Updated St")));

        verify(warehouseService).updateWarehouse(eq(testUuid), any(WarehouseDTO.class));
    }

    @Test
    void updateWarehouse_withNonExistingId_shouldReturnBadRequest() throws Exception {
        WarehouseDTO updateDTO = buildWarehouseDTO(testUuid, "WH001", "Updated Warehouse");

        when(warehouseService.updateWarehouse(eq(testUuid), any(WarehouseDTO.class)))
                .thenThrow(new ApplicationException("Warehouse not found with id: " + testUuid, "WAREHOUSE_NOT_FOUND"));

        assertBadRequest(
                put("/api/v1/inventory/warehouses/{id}", testUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)),
                "WAREHOUSE_NOT_FOUND",
                "Warehouse not found"
        );
        verify(warehouseService).updateWarehouse(eq(testUuid), any(WarehouseDTO.class));
    }

    @Test
    void deleteWarehouse_withExistingId_shouldReturnNoContent() throws Exception {
        doNothing().when(warehouseService).deleteWarehouse(testUuid);

        mockMvc.perform(delete("/api/v1/inventory/warehouses/{id}", testUuid))
                .andExpect(status().isNoContent());

        verify(warehouseService).deleteWarehouse(testUuid);
    }

    @Test
    void deleteWarehouse_withNonExistingId_shouldReturnBadRequest() throws Exception {
        doThrow(new ApplicationException("Warehouse not found with id: " + testUuid, "WAREHOUSE_NOT_FOUND"))
                .when(warehouseService).deleteWarehouse(testUuid);

        assertBadRequest(delete("/api/v1/inventory/warehouses/{id}", testUuid), "WAREHOUSE_NOT_FOUND", "Warehouse not found");
        verify(warehouseService).deleteWarehouse(testUuid);
    }

    @Test
    void deactivateWarehouse_withExistingId_shouldDeactivateAndReturnWarehouse() throws Exception {
        WarehouseDTO deactivatedWarehouseDTO = buildWarehouseDTO(
                testUuid,
                testWarehouseDTO.getCode(),
                testWarehouseDTO.getName()
        );
        deactivatedWarehouseDTO.setActive(false);

        when(warehouseService.deactivateWarehouse(testUuid)).thenReturn(deactivatedWarehouseDTO);

        mockMvc.perform(put("/api/v1/inventory/warehouses/{id}/deactivate", testUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid", is(testUuid.toString())))
                .andExpect(jsonPath("$.active", is(false)));

        verify(warehouseService).deactivateWarehouse(testUuid);
    }

    @Test
    void activateWarehouse_withExistingId_shouldActivateAndReturnWarehouse() throws Exception {
        WarehouseDTO activatedWarehouseDTO = buildWarehouseDTO(
                testUuid,
                testWarehouseDTO.getCode(),
                testWarehouseDTO.getName()
        );
        activatedWarehouseDTO.setActive(true);

        when(warehouseService.activateWarehouse(testUuid)).thenReturn(activatedWarehouseDTO);

        mockMvc.perform(put("/api/v1/inventory/warehouses/{id}/activate", testUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid", is(testUuid.toString())))
                .andExpect(jsonPath("$.active", is(true)));

        verify(warehouseService).activateWarehouse(testUuid);
    }

    @Test
    void getWarehousesByCity_shouldReturnWarehousesInCity() throws Exception {
        String city = "Test City";
        when(warehouseService.getWarehousesByCity(city)).thenReturn(Collections.singletonList(testWarehouseDTO));

        mockMvc.perform(get("/api/v1/inventory/warehouses/city/{city}", city))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].city", is(city)));

        verify(warehouseService).getWarehousesByCity(city);
    }

    @Test
    void getWarehousesByStateProvince_shouldReturnWarehousesInStateProvince() throws Exception {
        String stateProvince = "Test State";
        when(warehouseService.getWarehousesByStateProvince(stateProvince)).thenReturn(Collections.singletonList(testWarehouseDTO));

        mockMvc.perform(get("/api/v1/inventory/warehouses/state/{stateProvince}", stateProvince))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].stateProvince", is(stateProvince)));

        verify(warehouseService).getWarehousesByStateProvince(stateProvince);
    }

    @Test
    void getWarehousesByCountry_shouldReturnWarehousesInCountry() throws Exception {
        String country = "Test Country";
        when(warehouseService.getWarehousesByCountry(country)).thenReturn(Collections.singletonList(testWarehouseDTO));

        mockMvc.perform(get("/api/v1/inventory/warehouses/country/{country}", country))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].country", is(country)));

        verify(warehouseService).getWarehousesByCountry(country);
    }

    @Test
    void searchWarehousesByName_shouldReturnWarehousesWithNameContainingText() throws Exception {
        String searchText = "Test";
        when(warehouseService.searchWarehousesByName(searchText)).thenReturn(Collections.singletonList(testWarehouseDTO));

        mockMvc.perform(get("/api/v1/inventory/warehouses/search")
                        .param("name", searchText))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", containsString(searchText)));

        verify(warehouseService).searchWarehousesByName(searchText);
    }
}