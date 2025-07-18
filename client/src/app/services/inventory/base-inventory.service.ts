import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseRepository, BaseEntity, RepositoryConfig, QueryParams } from '../../core/repositories/base.repository';

/**
 * Modern Base Inventory Service using Repository Pattern
 * Provides common CRUD operations with caching, error handling, and retry logic
 */
@Injectable({
  providedIn: 'root'
})
export abstract class BaseInventoryService<T extends BaseEntity> extends BaseRepository<T> {

  /**
   * Get all entities (alias for findAll for backward compatibility)
   */
  getAll(): Observable<T[]> {
    return this.findAll();
  }

  /**
   * Get entity by ID (alias for findById for backward compatibility)
   */
  getById(id: string): Observable<T> {
    return this.findById(id);
  }

  /**
   * Get entity by code
   */
  getByCode(code: string): Observable<T> {
    return this.get<T>(`code/${code}`);
  }

  /**
   * Get entity by UUID (alias for findByUuid for backward compatibility)
   */
  getByUuid(uuid: string): Observable<T> {
    return this.findByUuid(uuid);
  }

  /**
   * Search entities by name
   */
  searchByName(name: string): Observable<T[]> {
    return this.search(name);
  }

  /**
   * Get active entities (alias for findActive for backward compatibility)
   */
  getActive(): Observable<T[]> {
    return this.findActive();
  }

  /**
   * Get entities with pagination
   */
  getPaginated(page: number = 0, size: number = 20, params?: QueryParams) {
    return this.findAllPaginated(page, size, params);
  }

  /**
   * Bulk operations support
   */
  bulkCreate(entities: Partial<T>[]): Observable<T[]> {
    return this.post<T[]>(entities as any, 'bulk');
  }

  /**
   * Bulk update support
   */
  bulkUpdate(updates: Array<{ id: string; data: Partial<T> }>): Observable<T[]> {
    return this.post<T[]>(updates as any, 'bulk-update');
  }

  /**
   * Bulk delete support
   */
  bulkDelete(ids: string[]): Observable<void> {
    return this.post<void>({ ids } as any, 'bulk-delete');
  }

  /**
   * Export entities to file
   */
  export(format: 'csv' | 'excel' = 'excel', params?: QueryParams): Observable<Blob> {
    const exportParams = { format, ...params };
    return this.get<Blob>('export', exportParams);
  }

  /**
   * Get entity statistics
   */
  getStatistics(): Observable<any> {
    return this.get<any>('statistics');
  }

  /**
   * Validate entity data
   */
  validate(entity: Partial<T>): Observable<{ valid: boolean; errors?: string[] }> {
    return this.post<{ valid: boolean; errors?: string[] }>(entity, 'validate');
  }
}
