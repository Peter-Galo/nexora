import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseEntity, BaseRepository } from '../../core/repositories/base.repository';

/**
 * Modern Base Inventory Service using Repository Pattern
 * Provides common CRUD operations with caching, error handling, and retry logic
 */
@Injectable({
  providedIn: 'root',
})
export abstract class BaseInventoryService<
  T extends BaseEntity,
> extends BaseRepository<T> {
  /**
   * Get all entities (alias for findAll for backward compatibility)
   */
  getAll(): Observable<T[]> {
    return this.findAll();
  }
}
