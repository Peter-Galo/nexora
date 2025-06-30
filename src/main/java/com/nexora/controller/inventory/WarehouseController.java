package com.nexora.controller.inventory;

import com.nexora.dto.inventory.WarehouseDTO;
import com.nexora.service.inventory.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing warehouses.
 */
@RestController
@RequestMapping("/api/v1/inventory/warehouses")
@Tag(name = "Warehouse Management", description = "APIs for managing warehouses in the inventory system")
public class WarehouseController {
    
    private final WarehouseService warehouseService;
    
    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }
    
    @Operation(summary = "Get all warehouses", description = "Retrieves a list of all warehouses")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of warehouses"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<WarehouseDTO>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseService.getAllWarehouses());
    }
    
    @Operation(summary = "Get active warehouses", description = "Retrieves a list of all active warehouses")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of active warehouses"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/active")
    public ResponseEntity<List<WarehouseDTO>> getActiveWarehouses() {
        return ResponseEntity.ok(warehouseService.getActiveWarehouses());
    }
    
    @Operation(summary = "Get warehouse by ID", description = "Retrieves a warehouse by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the warehouse"),
        @ApiResponse(responseCode = "404", description = "Warehouse not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<WarehouseDTO> getWarehouseById(
            @Parameter(description = "ID of the warehouse to retrieve", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.getWarehouseById(id));
    }
    
    @Operation(summary = "Get warehouse by code", description = "Retrieves a warehouse by its code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the warehouse"),
        @ApiResponse(responseCode = "404", description = "Warehouse not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/code/{code}")
    public ResponseEntity<WarehouseDTO> getWarehouseByCode(
            @Parameter(description = "Code of the warehouse to retrieve", required = true)
            @PathVariable String code) {
        return ResponseEntity.ok(warehouseService.getWarehouseByCode(code));
    }
    
    @Operation(summary = "Create a new warehouse", description = "Creates a new warehouse with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Warehouse successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid input or warehouse code already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<WarehouseDTO> createWarehouse(
            @Parameter(description = "Warehouse information", required = true, schema = @Schema(implementation = WarehouseDTO.class))
            @Valid @RequestBody WarehouseDTO warehouseDTO) {
        return new ResponseEntity<>(warehouseService.createWarehouse(warehouseDTO), HttpStatus.CREATED);
    }
    
    @Operation(summary = "Update an existing warehouse", description = "Updates an existing warehouse with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Warehouse successfully updated"),
        @ApiResponse(responseCode = "400", description = "Invalid input or warehouse code already exists"),
        @ApiResponse(responseCode = "404", description = "Warehouse not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<WarehouseDTO> updateWarehouse(
            @Parameter(description = "ID of the warehouse to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated warehouse information", required = true, schema = @Schema(implementation = WarehouseDTO.class))
            @Valid @RequestBody WarehouseDTO warehouseDTO) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(id, warehouseDTO));
    }
    
    @Operation(summary = "Delete a warehouse", description = "Deletes a warehouse by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Warehouse successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Warehouse not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarehouse(
            @Parameter(description = "ID of the warehouse to delete", required = true)
            @PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Deactivate a warehouse", description = "Deactivates a warehouse by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Warehouse successfully deactivated"),
        @ApiResponse(responseCode = "404", description = "Warehouse not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<WarehouseDTO> deactivateWarehouse(
            @Parameter(description = "ID of the warehouse to deactivate", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.deactivateWarehouse(id));
    }
    
    @Operation(summary = "Activate a warehouse", description = "Activates a warehouse by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Warehouse successfully activated"),
        @ApiResponse(responseCode = "404", description = "Warehouse not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}/activate")
    public ResponseEntity<WarehouseDTO> activateWarehouse(
            @Parameter(description = "ID of the warehouse to activate", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.activateWarehouse(id));
    }
    
    @Operation(summary = "Get warehouses by city", description = "Retrieves a list of warehouses in the specified city")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of warehouses"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/city/{city}")
    public ResponseEntity<List<WarehouseDTO>> getWarehousesByCity(
            @Parameter(description = "City of warehouses to retrieve", required = true)
            @PathVariable String city) {
        return ResponseEntity.ok(warehouseService.getWarehousesByCity(city));
    }
    
    @Operation(summary = "Get warehouses by state/province", description = "Retrieves a list of warehouses in the specified state or province")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of warehouses"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/state/{stateProvince}")
    public ResponseEntity<List<WarehouseDTO>> getWarehousesByStateProvince(
            @Parameter(description = "State or province of warehouses to retrieve", required = true)
            @PathVariable String stateProvince) {
        return ResponseEntity.ok(warehouseService.getWarehousesByStateProvince(stateProvince));
    }
    
    @Operation(summary = "Get warehouses by country", description = "Retrieves a list of warehouses in the specified country")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of warehouses"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/country/{country}")
    public ResponseEntity<List<WarehouseDTO>> getWarehousesByCountry(
            @Parameter(description = "Country of warehouses to retrieve", required = true)
            @PathVariable String country) {
        return ResponseEntity.ok(warehouseService.getWarehousesByCountry(country));
    }
    
    @Operation(summary = "Search warehouses by name", description = "Searches for warehouses whose names contain the specified text")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of warehouses"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public ResponseEntity<List<WarehouseDTO>> searchWarehousesByName(
            @Parameter(description = "Text to search for in warehouse names", required = true)
            @RequestParam String name) {
        return ResponseEntity.ok(warehouseService.searchWarehousesByName(name));
    }
}