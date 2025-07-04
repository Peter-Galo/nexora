package com.nexora.repository.inventory;

import com.nexora.model.inventory.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Warehouse entity.
 */
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {

    /**
     * Find a warehouse by its unique code.
     *
     * @param code the warehouse code
     * @return an Optional containing the warehouse if found, or empty if not found
     */
    Optional<Warehouse> findByCode(String code);

    /**
     * Check if a warehouse with the given code exists.
     *
     * @param code the warehouse code
     * @return true if a warehouse with the code exists, false otherwise
     */
    boolean existsByCode(String code);

    /**
     * Find all active warehouses.
     *
     * @return a list of active warehouses
     */
    List<Warehouse> findByActiveTrueOrderByName();

    /**
     * Find warehouses by city.
     *
     * @param city the city
     * @return a list of warehouses in the specified city
     */
    List<Warehouse> findByCity(String city);

    /**
     * Find warehouses by state/province.
     *
     * @param stateProvince the state or province
     * @return a list of warehouses in the specified state or province
     */
    List<Warehouse> findByStateProvince(String stateProvince);

    /**
     * Find warehouses by country.
     *
     * @param country the country
     * @return a list of warehouses in the specified country
     */
    List<Warehouse> findByCountry(String country);

    /**
     * Find warehouses by name containing the given text (case-insensitive).
     *
     * @param name the text to search for in warehouse names
     * @return a list of warehouses whose names contain the specified text
     */
    List<Warehouse> findByNameContainingIgnoreCaseOrderByName(String name);

    @Query("SELECT w FROM Warehouse w ORDER BY w.active DESC, w.name ASC")
    List<Warehouse> findAllOrderedByActiveAndName();
}