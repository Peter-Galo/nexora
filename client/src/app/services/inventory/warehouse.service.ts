import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseInventoryService } from './base-inventory.service';
import { BaseEntity, RepositoryConfig } from '../../core/repositories/base.repository';

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
    enableCache: true,
  };

  /**
   * Get all warehouses
   * @returns Observable with the list of warehouses
   */
  getAllWarehouses(): Observable<WarehouseEntity[]> {
    return this.getAll();
  }

  /**
   * Create a new warehouse
   * @param warehouse - The warehouse data to create
   * @returns Observable with the created warehouse
   */
  createWarehouse(
    warehouse: Partial<WarehouseEntity>,
  ): Observable<WarehouseEntity> {
    return this.create(warehouse);
  }

  /**
   * Delete a warehouse
   * @param id - The warehouse UUID
   * @returns Observable with void response
   */
  deleteWarehouse(id: string): Observable<void> {
    return this.remove(id);
  }

  /**
   * Activate a warehouse
   * @param id - The warehouse UUID
   * @returns Observable with the activated warehouse
   */
  activateWarehouse(id: string): Observable<WarehouseEntity> {
    return this.activate(id);
  }

  /**
   * Deactivate a warehouse
   * @param id - The warehouse UUID
   * @returns Observable with the deactivated warehouse
   */
  deactivateWarehouse(id: string): Observable<WarehouseEntity> {
    return this.deactivate(id);
  }
}
