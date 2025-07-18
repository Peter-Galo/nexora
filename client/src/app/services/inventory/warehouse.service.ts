import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseInventoryService } from './base-inventory.service';
import { BaseEntity, RepositoryConfig, QueryParams } from '../../core/repositories/base.repository';

/**
 * Modern Warehouse Entity Interface extending BaseEntity
 */
export interface WarehouseEntity extends BaseEntity {
  code: string;
  name: string;
  description?: string;
  address: string;
  city: string;
  stateProvince?: string;
  postalCode?: string;
  country: string;
}

/**
 * Warehouse DTO for backward compatibility
 * @deprecated Use WarehouseEntity instead
 */
export interface WarehouseDTO extends WarehouseEntity {
  // Keeping for backward compatibility
}

/**
 * Modern Warehouse Service using Repository Pattern
 * Provides warehouse-specific operations with caching, error handling, and retry logic
 */
@Injectable({
  providedIn: 'root',
})
export class WarehouseService extends BaseInventoryService<WarehouseEntity> {

  // Repository configuration
  protected readonly config: RepositoryConfig = {
    baseUrl: 'inventory/warehouses',
    entityName: 'Warehouse',
    cacheTimeout: 5 * 60 * 1000, // 5 minutes
    retryAttempts: 3,
    enableCache: true
  };

  /**
   * Get all warehouses (backward compatible method)
   * @returns Observable with the list of warehouses
   */
  getAllWarehouses(): Observable<WarehouseDTO[]> {
    return this.getAll();
  }

  /**
   * Get only active warehouses (backward compatible method)
   * @returns Observable with the list of active warehouses
   */
  getActiveWarehouses(): Observable<WarehouseDTO[]> {
    return this.getActive();
  }

  /**
   * Get warehouse by ID (backward compatible method)
   * @param id - The warehouse UUID
   * @returns Observable with the warehouse data
   */
  getWarehouseById(id: string): Observable<WarehouseDTO> {
    return this.getById(id);
  }

  /**
   * Get warehouse by code (backward compatible method)
   * @param code - The warehouse code
   * @returns Observable with the warehouse data
   */
  getWarehouseByCode(code: string): Observable<WarehouseDTO> {
    return this.getByCode(code);
  }

  /**
   * Create a new warehouse (backward compatible method)
   * @param warehouse - The warehouse data to create
   * @returns Observable with the created warehouse
   */
  createWarehouse(warehouse: Partial<WarehouseEntity>): Observable<WarehouseDTO> {
    return this.create(warehouse);
  }

  /**
   * Update an existing warehouse (backward compatible method)
   * @param id - The warehouse UUID
   * @param warehouse - The updated warehouse data
   * @returns Observable with the updated warehouse
   */
  updateWarehouse(id: string, warehouse: Partial<WarehouseEntity>): Observable<WarehouseDTO> {
    return this.update(id, warehouse);
  }

  /**
   * Delete a warehouse (backward compatible method)
   * @param id - The warehouse UUID
   * @returns Observable with void response
   */
  deleteWarehouse(id: string): Observable<void> {
    return this.remove(id);
  }

  /**
   * Activate a warehouse (backward compatible method)
   * @param id - The warehouse UUID
   * @returns Observable with the activated warehouse
   */
  activateWarehouse(id: string): Observable<WarehouseDTO> {
    return this.activate(id);
  }

  /**
   * Deactivate a warehouse (backward compatible method)
   * @param id - The warehouse UUID
   * @returns Observable with the deactivated warehouse
   */
  deactivateWarehouse(id: string): Observable<WarehouseDTO> {
    return this.deactivate(id);
  }

  /**
   * Search warehouses by name (backward compatible method)
   * @param name - The search term for warehouse name
   * @returns Observable with the list of matching warehouses
   */
  searchWarehousesByName(name: string): Observable<WarehouseDTO[]> {
    return this.searchByName(name);
  }

  /**
   * Get warehouses by city
   * @param city - The city name
   * @returns Observable with the list of warehouses in the city
   */
  getWarehousesByCity(city: string): Observable<WarehouseDTO[]> {
    return this.findAll({ city });
  }

  /**
   * Get warehouses by state/province
   * @param stateProvince - The state or province name
   * @returns Observable with the list of warehouses in the state/province
   */
  getWarehousesByStateProvince(stateProvince: string): Observable<WarehouseDTO[]> {
    return this.findAll({ stateProvince });
  }

  /**
   * Get warehouses by country
   * @param country - The country name
   * @returns Observable with the list of warehouses in the country
   */
  getWarehousesByCountry(country: string): Observable<WarehouseDTO[]> {
    return this.findAll({ country });
  }

  // ========== Modern Warehouse-Specific Methods ==========

  /**
   * Get warehouses with pagination and filtering
   * @param page - Page number (0-based)
   * @param size - Page size
   * @param filters - Optional filters
   * @returns Observable with paginated warehouse data
   */
  getWarehousesPaginated(page: number = 0, size: number = 20, filters?: Partial<WarehouseEntity>) {
    return this.getPaginated(page, size, filters);
  }

  /**
   * Search warehouses with advanced filters
   * @param searchTerm - Search term
   * @param filters - Additional filters
   * @returns Observable with filtered warehouses
   */
  searchWarehouses(searchTerm: string, filters?: Partial<WarehouseEntity>): Observable<WarehouseDTO[]> {
    const searchParams = { q: searchTerm, ...filters };
    return this.findAll(searchParams);
  }

  /**
   * Get warehouse statistics
   * @returns Observable with warehouse statistics
   */
  getWarehouseStatistics(): Observable<{
    total: number;
    active: number;
    inactive: number;
    byCountry: Record<string, number>;
    byCity: Record<string, number>;
  }> {
    return this.getStatistics();
  }

  /**
   * Validate warehouse data before creation/update
   * @param warehouse - Warehouse data to validate
   * @returns Observable with validation result
   */
  validateWarehouse(warehouse: Partial<WarehouseEntity>): Observable<{ valid: boolean; errors?: string[] }> {
    return this.validate(warehouse);
  }

  /**
   * Bulk create warehouses
   * @param warehouses - Array of warehouse data
   * @returns Observable with created warehouses
   */
  createWarehousesBulk(warehouses: Partial<WarehouseEntity>[]): Observable<WarehouseDTO[]> {
    return this.bulkCreate(warehouses);
  }

  /**
   * Bulk update warehouses
   * @param updates - Array of warehouse updates
   * @returns Observable with updated warehouses
   */
  updateWarehousesBulk(updates: Array<{ id: string; data: Partial<WarehouseEntity> }>): Observable<WarehouseDTO[]> {
    return this.bulkUpdate(updates);
  }

  /**
   * Bulk delete warehouses
   * @param ids - Array of warehouse IDs
   * @returns Observable with void response
   */
  deleteWarehousesBulk(ids: string[]): Observable<void> {
    return this.bulkDelete(ids);
  }

  /**
   * Export warehouses to file
   * @param format - Export format (csv or excel)
   * @param filters - Optional filters
   * @returns Observable with file blob
   */
  exportWarehouses(format: 'csv' | 'excel' = 'excel', filters?: Partial<WarehouseEntity>): Observable<Blob> {
    return this.export(format, filters);
  }

  /**
   * Get warehouses near a location (if backend supports geolocation)
   * @param latitude - Latitude coordinate
   * @param longitude - Longitude coordinate
   * @param radius - Search radius in kilometers
   * @returns Observable with nearby warehouses
   */
  getWarehousesNearLocation(latitude: number, longitude: number, radius: number = 50): Observable<WarehouseDTO[]> {
    return this.findAll({ latitude, longitude, radius });
  }

  /**
   * Get warehouse capacity information
   * @param id - Warehouse ID
   * @returns Observable with capacity data
   */
  getWarehouseCapacity(id: string): Observable<{
    totalCapacity: number;
    usedCapacity: number;
    availableCapacity: number;
    utilizationPercentage: number;
  }> {
    return this.get<any>(`${id}/capacity`);
  }

  /**
   * Update warehouse capacity
   * @param id - Warehouse ID
   * @param capacity - New capacity value
   * @returns Observable with updated warehouse
   */
  updateWarehouseCapacity(id: string, capacity: number): Observable<WarehouseDTO> {
    return this.put<WarehouseDTO>(id, { capacity } as any, 'capacity');
  }

  /**
   * Get warehouse inventory summary
   * @param id - Warehouse ID
   * @returns Observable with inventory summary
   */
  getWarehouseInventorySummary(id: string): Observable<{
    totalProducts: number;
    totalValue: number;
    lowStockItems: number;
    categories: Record<string, number>;
  }> {
    return this.get<any>(`${id}/inventory-summary`);
  }

  /**
   * Refresh warehouse cache
   */
  refreshWarehouseCache(): void {
    this.refresh();
  }
}
