import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';

/**
 * Base service class for inventory-related HTTP operations
 * Provides common CRUD operations and error handling
 */
@Injectable({
  providedIn: 'root'
})
export abstract class BaseInventoryService<T> {
  protected abstract readonly apiUrl: string;
  protected abstract readonly entityName: string;

  constructor(protected readonly http: HttpClient) {}

  /**
   * Generic GET request with error handling
   */
  protected get<R = T>(endpoint: string = ''): Observable<R> {
    const url = endpoint ? `${this.apiUrl}/${endpoint}` : this.apiUrl;
    return this.http.get<R>(url).pipe(
      catchError(error => {
        console.error(`Error fetching ${this.entityName}:`, error);
        throw error;
      })
    );
  }

  /**
   * Generic GET request for arrays with error handling
   */
  protected getArray<R = T>(endpoint: string = ''): Observable<R[]> {
    return this.get<R[]>(endpoint);
  }

  /**
   * Generic POST request with error handling
   */
  protected post<R = T>(data: Partial<T>, endpoint: string = ''): Observable<R> {
    const url = endpoint ? `${this.apiUrl}/${endpoint}` : this.apiUrl;
    return this.http.post<R>(url, data).pipe(
      catchError(error => {
        console.error(`Error creating ${this.entityName}:`, error);
        throw error;
      })
    );
  }

  /**
   * Generic PUT request with error handling
   */
  protected put<R = T>(id: string, data: Partial<T>, endpoint: string = ''): Observable<R> {
    const url = endpoint ? `${this.apiUrl}/${id}/${endpoint}` : `${this.apiUrl}/${id}`;
    return this.http.put<R>(url, data).pipe(
      catchError(error => {
        console.error(`Error updating ${this.entityName}:`, error);
        throw error;
      })
    );
  }

  /**
   * Generic DELETE request with error handling
   */
  protected delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError(error => {
        console.error(`Error deleting ${this.entityName}:`, error);
        throw error;
      })
    );
  }

  /**
   * Get all entities
   */
  getAll(): Observable<T[]> {
    return this.getArray();
  }

  /**
   * Get entity by ID
   */
  getById(id: string): Observable<T> {
    return this.get(id);
  }

  /**
   * Get entity by code
   */
  getByCode(code: string): Observable<T> {
    return this.get(`code/${code}`);
  }

  /**
   * Create new entity
   */
  create(entity: Partial<T>): Observable<T> {
    return this.post(entity);
  }

  /**
   * Update existing entity
   */
  update(id: string, entity: Partial<T>): Observable<T> {
    return this.put(id, entity);
  }

  /**
   * Delete entity
   */
  remove(id: string): Observable<void> {
    return this.delete(id);
  }

  /**
   * Search entities by name
   */
  searchByName(name: string): Observable<T[]> {
    return this.http.get<T[]>(`${this.apiUrl}/search`, {
      params: { name }
    }).pipe(
      catchError(error => {
        console.error(`Error searching ${this.entityName} by name:`, error);
        throw error;
      })
    );
  }

  /**
   * Get active entities
   */
  getActive(): Observable<T[]> {
    return this.getArray('active');
  }

  /**
   * Activate entity
   */
  activate(id: string): Observable<T> {
    return this.put(id, {}, 'activate');
  }

  /**
   * Deactivate entity
   */
  deactivate(id: string): Observable<T> {
    return this.put(id, {}, 'deactivate');
  }
}
