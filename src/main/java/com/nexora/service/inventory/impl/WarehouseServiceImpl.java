package com.nexora.service.inventory.impl;

import com.nexora.dto.inventory.WarehouseDTO;
import com.nexora.exception.ApplicationException;
import com.nexora.model.inventory.Warehouse;
import com.nexora.repository.inventory.WarehouseRepository;
import com.nexora.service.inventory.WarehouseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the WarehouseService interface.
 */
@Service
@Transactional
public class WarehouseServiceImpl implements WarehouseService {
    
    private final WarehouseRepository warehouseRepository;
    
    public WarehouseServiceImpl(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDTO> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDTO> getActiveWarehouses() {
        return warehouseRepository.findByActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public WarehouseDTO getWarehouseById(UUID id) {
        return warehouseRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ApplicationException("Warehouse not found with id: " + id, "WAREHOUSE_NOT_FOUND"));
    }
    
    @Override
    @Transactional(readOnly = true)
    public WarehouseDTO getWarehouseByCode(String code) {
        return warehouseRepository.findByCode(code)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ApplicationException("Warehouse not found with code: " + code, "WAREHOUSE_NOT_FOUND"));
    }
    
    @Override
    public WarehouseDTO createWarehouse(WarehouseDTO warehouseDTO) {
        // Check if warehouse with the same code already exists
        if (warehouseRepository.existsByCode(warehouseDTO.getCode())) {
            throw new ApplicationException("Warehouse with code " + warehouseDTO.getCode() + " already exists", "WAREHOUSE_CODE_EXISTS");
        }
        
        Warehouse warehouse = mapToEntity(warehouseDTO);
        warehouse.setCreatedAt(LocalDateTime.now());
        warehouse.setUpdatedAt(LocalDateTime.now());
        
        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        return mapToDTO(savedWarehouse);
    }
    
    @Override
    public WarehouseDTO updateWarehouse(UUID id, WarehouseDTO warehouseDTO) {
        Warehouse existingWarehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("Warehouse not found with id: " + id, "WAREHOUSE_NOT_FOUND"));
        
        // Check if the code is being changed and if the new code already exists
        if (!existingWarehouse.getCode().equals(warehouseDTO.getCode()) && 
                warehouseRepository.existsByCode(warehouseDTO.getCode())) {
            throw new ApplicationException("Warehouse with code " + warehouseDTO.getCode() + " already exists", "WAREHOUSE_CODE_EXISTS");
        }
        
        // Update the warehouse fields
        existingWarehouse.setCode(warehouseDTO.getCode());
        existingWarehouse.setName(warehouseDTO.getName());
        existingWarehouse.setDescription(warehouseDTO.getDescription());
        existingWarehouse.setAddress(warehouseDTO.getAddress());
        existingWarehouse.setCity(warehouseDTO.getCity());
        existingWarehouse.setStateProvince(warehouseDTO.getStateProvince());
        existingWarehouse.setPostalCode(warehouseDTO.getPostalCode());
        existingWarehouse.setCountry(warehouseDTO.getCountry());
        existingWarehouse.setActive(warehouseDTO.isActive());
        existingWarehouse.setUpdatedAt(LocalDateTime.now());
        
        Warehouse updatedWarehouse = warehouseRepository.save(existingWarehouse);
        return mapToDTO(updatedWarehouse);
    }
    
    @Override
    public void deleteWarehouse(UUID id) {
        if (!warehouseRepository.existsById(id)) {
            throw new ApplicationException("Warehouse not found with id: " + id, "WAREHOUSE_NOT_FOUND");
        }
        
        warehouseRepository.deleteById(id);
    }
    
    @Override
    public WarehouseDTO deactivateWarehouse(UUID id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("Warehouse not found with id: " + id, "WAREHOUSE_NOT_FOUND"));
        
        warehouse.setActive(false);
        warehouse.setUpdatedAt(LocalDateTime.now());
        
        Warehouse updatedWarehouse = warehouseRepository.save(warehouse);
        return mapToDTO(updatedWarehouse);
    }
    
    @Override
    public WarehouseDTO activateWarehouse(UUID id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("Warehouse not found with id: " + id, "WAREHOUSE_NOT_FOUND"));
        
        warehouse.setActive(true);
        warehouse.setUpdatedAt(LocalDateTime.now());
        
        Warehouse updatedWarehouse = warehouseRepository.save(warehouse);
        return mapToDTO(updatedWarehouse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDTO> getWarehousesByCity(String city) {
        return warehouseRepository.findByCity(city).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDTO> getWarehousesByStateProvince(String stateProvince) {
        return warehouseRepository.findByStateProvince(stateProvince).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDTO> getWarehousesByCountry(String country) {
        return warehouseRepository.findByCountry(country).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDTO> searchWarehousesByName(String name) {
        return warehouseRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Maps a Warehouse entity to a WarehouseDTO.
     *
     * @param warehouse the Warehouse entity
     * @return the WarehouseDTO
     */
    private WarehouseDTO mapToDTO(Warehouse warehouse) {
        return new WarehouseDTO(
                warehouse.getUuid(),
                warehouse.getCode(),
                warehouse.getName(),
                warehouse.getDescription(),
                warehouse.getAddress(),
                warehouse.getCity(),
                warehouse.getStateProvince(),
                warehouse.getPostalCode(),
                warehouse.getCountry(),
                warehouse.getCreatedAt(),
                warehouse.getUpdatedAt(),
                warehouse.isActive()
        );
    }
    
    /**
     * Maps a WarehouseDTO to a Warehouse entity.
     *
     * @param warehouseDTO the WarehouseDTO
     * @return the Warehouse entity
     */
    private Warehouse mapToEntity(WarehouseDTO warehouseDTO) {
        Warehouse warehouse = new Warehouse();
        warehouse.setUuid(warehouseDTO.getUuid());
        warehouse.setCode(warehouseDTO.getCode());
        warehouse.setName(warehouseDTO.getName());
        warehouse.setDescription(warehouseDTO.getDescription());
        warehouse.setAddress(warehouseDTO.getAddress());
        warehouse.setCity(warehouseDTO.getCity());
        warehouse.setStateProvince(warehouseDTO.getStateProvince());
        warehouse.setPostalCode(warehouseDTO.getPostalCode());
        warehouse.setCountry(warehouseDTO.getCountry());
        warehouse.setActive(warehouseDTO.isActive());
        
        // Don't set createdAt and updatedAt here, they are set in the service methods
        
        return warehouse;
    }
}