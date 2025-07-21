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

  getAllWarehouses(): Observable<WarehouseEntity[]> {
    return this.getAll();
  }

  createWarehouse(
    warehouse: Partial<WarehouseEntity>,
  ): Observable<WarehouseEntity> {
    return this.create(warehouse);
  }

  deleteWarehouse(id: string): Observable<void> {
    return this.remove(id);
  }

  activateWarehouse(id: string): Observable<WarehouseEntity> {
    return this.activate(id);
  }

  deactivateWarehouse(id: string): Observable<WarehouseEntity> {
    return this.deactivate(id);
  }
}
