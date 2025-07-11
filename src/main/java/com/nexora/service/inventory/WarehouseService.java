package com.nexora.service.inventory;

import com.nexora.dto.inventory.WarehouseDTO;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing warehouses.
 */
public interface WarehouseService {
    
    /**
     * Get all warehouses.
     *
     * @return a list of all warehouses
     */
    List<WarehouseDTO> getAllWarehouses();
    
    /**
     * Get all active warehouses.
     *
     * @return a list of active warehouses
     */
    List<WarehouseDTO> getActiveWarehouses();
    
    /**
     * Get a warehouse by its UUID.
     *
     * @param uuid the warehouse UUID
     * @return the warehouse with the specified UUID
     * @throws com.nexora.exception.ApplicationException if the warehouse is not found
     */
    WarehouseDTO getWarehouseById(UUID uuid);
    
    /**
     * Get a warehouse by its code.
     *
     * @param code the warehouse code
     * @return the warehouse with the specified code
     * @throws com.nexora.exception.ApplicationException if the warehouse is not found
     */
    WarehouseDTO getWarehouseByCode(String code);
    
    /**
     * Create a new warehouse.
     *
     * @param warehouseDTO the warehouse data
     * @return the created warehouse
     * @throws com.nexora.exception.ApplicationException if a warehouse with the same code already exists
     */
    WarehouseDTO createWarehouse(WarehouseDTO warehouseDTO);
    
    /**
     * Update an existing warehouse.
     *
     * @param uuid the UUID of the warehouse to update
     * @param warehouseDTO the updated warehouse data
     * @return the updated warehouse
     * @throws com.nexora.exception.ApplicationException if the warehouse is not found
     */
    WarehouseDTO updateWarehouse(UUID uuid, WarehouseDTO warehouseDTO);
    
    /**
     * Delete a warehouse by its UUID.
     *
     * @param uuid the UUID of the warehouse to delete
     * @throws com.nexora.exception.ApplicationException if the warehouse is not found
     */
    void deleteWarehouse(UUID uuid);
    
    /**
     * Deactivate a warehouse by its UUID.
     *
     * @param uuid the UUID of the warehouse to deactivate
     * @return the deactivated warehouse
     * @throws com.nexora.exception.ApplicationException if the warehouse is not found
     */
    WarehouseDTO deactivateWarehouse(UUID uuid);
    
    /**
     * Activate a warehouse by its UUID.
     *
     * @param uuid the UUID of the warehouse to activate
     * @return the activated warehouse
     * @throws com.nexora.exception.ApplicationException if the warehouse is not found
     */
    WarehouseDTO activateWarehouse(UUID uuid);
    
    /**
     * Find warehouses by city.
     *
     * @param city the city
     * @return a list of warehouses in the specified city
     */
    List<WarehouseDTO> getWarehousesByCity(String city);
    
    /**
     * Find warehouses by state/province.
     *
     * @param stateProvince the state or province
     * @return a list of warehouses in the specified state or province
     */
    List<WarehouseDTO> getWarehousesByStateProvince(String stateProvince);
    
    /**
     * Find warehouses by country.
     *
     * @param country the country
     * @return a list of warehouses in the specified country
     */
    List<WarehouseDTO> getWarehousesByCountry(String country);
    
    /**
     * Search warehouses by name.
     *
     * @param name the text to search for in warehouse names
     * @return a list of warehouses whose names contain the specified text
     */
    List<WarehouseDTO> searchWarehousesByName(String name);
}