import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject, timer } from 'rxjs';
import { catchError, retry, shareReplay, switchMap, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

/**
 * Base Entity Interface
 */
export interface BaseEntity {
  id?: string;
  uuid?: string;
  createdAt?: string;
  updatedAt?: string;
  active?: boolean;
}

/**
 * Repository Configuration Interface
 */
export interface RepositoryConfig {
  baseUrl: string;
  entityName: string;
  cacheTimeout?: number;
  retryAttempts?: number;
  enableCache?: boolean;
}

/**
 * Query Parameters Interface
 */
export interface QueryParams {
  [key: string]: string | number | boolean | string[] | number[] | boolean[];
}

/**
 * Paginated Response Interface
 */
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

/**
 * Repository Error Interface
 */
export interface RepositoryError {
  message: string;
  status: number;
  timestamp: string;
  path: string;
  details?: any;
}

/**
 * Modern Repository Base Class implementing Repository Pattern
 * Provides CRUD operations, caching, error handling, and retry logic
 */
@Injectable()
export abstract class BaseRepository<T extends BaseEntity> {
  protected readonly http = inject(HttpClient);

  // Abstract properties that must be implemented by child classes
  protected abstract readonly config: RepositoryConfig;

  // Cache management
  private readonly cache = new Map<string, { data: any; timestamp: number }>();
  private readonly refreshSubject = new BehaviorSubject<void>(undefined);

  /**
   * Get the full API URL for the entity
   */
  protected get apiUrl(): string {
    return `${environment.apiUrl}/${this.config.baseUrl}`;
  }

  /**
   * Get cache timeout in milliseconds
   */
  protected get cacheTimeout(): number {
    return this.config.cacheTimeout || 5 * 60 * 1000; // 5 minutes default
  }

  /**
   * Get retry attempts count
   */
  protected get retryAttempts(): number {
    return this.config.retryAttempts || 3;
  }

  /**
   * Check if caching is enabled
   */
  protected get isCacheEnabled(): boolean {
    return this.config.enableCache !== false;
  }

  /**
   * Generic HTTP GET request with caching and error handling
   */
  protected get<R = T>(endpoint: string = '', params?: QueryParams): Observable<R> {
    const url = this.buildUrl(endpoint);
    const cacheKey = this.buildCacheKey(url, params);

    // Check cache first if enabled
    if (this.isCacheEnabled && this.isValidCache(cacheKey)) {
      return new Observable(observer => {
        observer.next(this.cache.get(cacheKey)!.data);
        observer.complete();
      });
    }

    const httpParams = this.buildHttpParams(params);

    return this.http.get<R>(url, { params: httpParams }).pipe(
      retry(this.retryAttempts),
      tap(data => {
        if (this.isCacheEnabled) {
          this.setCache(cacheKey, data);
        }
      }),
      catchError(error => this.handleError(error, 'GET', url)),
      shareReplay(1)
    );
  }

  /**
   * Generic HTTP POST request with error handling
   */
  protected post<R = T>(data: Partial<T>, endpoint: string = ''): Observable<R> {
    const url = this.buildUrl(endpoint);

    return this.http.post<R>(url, data).pipe(
      retry(this.retryAttempts),
      tap(() => this.invalidateCache()),
      catchError(error => this.handleError(error, 'POST', url))
    );
  }

  /**
   * Generic HTTP PUT request with error handling
   */
  protected put<R = T>(id: string, data: Partial<T>, endpoint: string = ''): Observable<R> {
    const url = this.buildUrl(endpoint ? `${id}/${endpoint}` : id);

    return this.http.put<R>(url, data).pipe(
      retry(this.retryAttempts),
      tap(() => this.invalidateCache()),
      catchError(error => this.handleError(error, 'PUT', url))
    );
  }

  /**
   * Generic HTTP PATCH request with error handling
   */
  protected patch<R = T>(id: string, data: Partial<T>, endpoint: string = ''): Observable<R> {
    const url = this.buildUrl(endpoint ? `${id}/${endpoint}` : id);

    return this.http.patch<R>(url, data).pipe(
      retry(this.retryAttempts),
      tap(() => this.invalidateCache()),
      catchError(error => this.handleError(error, 'PATCH', url))
    );
  }

  /**
   * Generic HTTP DELETE request with error handling
   */
  protected delete(id: string): Observable<void> {
    const url = this.buildUrl(id);

    return this.http.delete<void>(url).pipe(
      retry(this.retryAttempts),
      tap(() => this.invalidateCache()),
      catchError(error => this.handleError(error, 'DELETE', url))
    );
  }

  /**
   * Get all entities
   */
  findAll(params?: QueryParams): Observable<T[]> {
    return this.get<T[]>('', params);
  }

  /**
   * Get paginated entities
   */
  findAllPaginated(page: number = 0, size: number = 20, params?: QueryParams): Observable<PaginatedResponse<T>> {
    const paginationParams = { page, size, ...params };
    return this.get<PaginatedResponse<T>>('paginated', paginationParams);
  }

  /**
   * Get entity by ID
   */
  findById(id: string): Observable<T> {
    return this.get<T>(id);
  }

  /**
   * Get entity by UUID
   */
  findByUuid(uuid: string): Observable<T> {
    return this.get<T>(`uuid/${uuid}`);
  }

  /**
   * Create new entity
   */
  create(entity: Partial<T>): Observable<T> {
    return this.post<T>(entity);
  }

  /**
   * Update existing entity
   */
  update(id: string, entity: Partial<T>): Observable<T> {
    return this.put<T>(id, entity);
  }

  /**
   * Partially update entity
   */
  partialUpdate(id: string, entity: Partial<T>): Observable<T> {
    return this.patch<T>(id, entity);
  }

  /**
   * Delete entity
   */
  remove(id: string): Observable<void> {
    return this.delete(id);
  }

  /**
   * Search entities
   */
  search(query: string, params?: QueryParams): Observable<T[]> {
    const searchParams = { q: query, ...params };
    return this.get<T[]>('search', searchParams);
  }

  /**
   * Get active entities
   */
  findActive(params?: QueryParams): Observable<T[]> {
    return this.get<T[]>('active', params);
  }

  /**
   * Activate entity
   */
  activate(id: string): Observable<T> {
    return this.put<T>(id, {}, 'activate');
  }

  /**
   * Deactivate entity
   */
  deactivate(id: string): Observable<T> {
    return this.put<T>(id, {}, 'deactivate');
  }

  /**
   * Refresh cache and reload data
   */
  refresh(): void {
    this.invalidateCache();
    this.refreshSubject.next();
  }

  /**
   * Get refresh observable for reactive updates
   */
  getRefreshObservable(): Observable<void> {
    return this.refreshSubject.asObservable();
  }

  /**
   * Build full URL with endpoint
   */
  private buildUrl(endpoint: string): string {
    return endpoint ? `${this.apiUrl}/${endpoint}` : this.apiUrl;
  }

  /**
   * Build HTTP parameters from query params
   */
  private buildHttpParams(params?: QueryParams): HttpParams {
    let httpParams = new HttpParams();

    if (params) {
      Object.keys(params).forEach(key => {
        const value = params[key];
        if (value !== null && value !== undefined) {
          if (Array.isArray(value)) {
            value.forEach(v => httpParams = httpParams.append(key, v.toString()));
          } else {
            httpParams = httpParams.set(key, value.toString());
          }
        }
      });
    }

    return httpParams;
  }

  /**
   * Build cache key from URL and parameters
   */
  private buildCacheKey(url: string, params?: QueryParams): string {
    const paramString = params ? JSON.stringify(params) : '';
    return `${url}:${paramString}`;
  }

  /**
   * Check if cache entry is valid
   */
  private isValidCache(key: string): boolean {
    const entry = this.cache.get(key);
    if (!entry) return false;

    const now = Date.now();
    return (now - entry.timestamp) < this.cacheTimeout;
  }

  /**
   * Set cache entry
   */
  private setCache(key: string, data: any): void {
    this.cache.set(key, {
      data,
      timestamp: Date.now()
    });
  }

  /**
   * Invalidate all cache entries
   */
  private invalidateCache(): void {
    this.cache.clear();
  }

  /**
   * Handle HTTP errors with proper error transformation
   */
  private handleError(error: HttpErrorResponse, method: string, url: string): Observable<never> {
    const repositoryError: RepositoryError = {
      message: this.getErrorMessage(error),
      status: error.status || 0,
      timestamp: new Date().toISOString(),
      path: url,
      details: error.error
    };

    console.error(`Repository Error [${method}] ${url}:`, repositoryError);

    return throwError(() => repositoryError);
  }

  /**
   * Extract meaningful error message from HTTP error
   */
  private getErrorMessage(error: HttpErrorResponse): string {
    if (error.error?.message) {
      return error.error.message;
    }

    switch (error.status) {
      case 0:
        return 'Network error. Please check your connection.';
      case 400:
        return 'Invalid request. Please check your input.';
      case 401:
        return 'Authentication required. Please log in.';
      case 403:
        return 'Access denied. You do not have permission.';
      case 404:
        return `${this.config.entityName} not found.`;
      case 409:
        return 'Conflict. The resource already exists or is in use.';
      case 422:
        return 'Validation error. Please check your input.';
      case 500:
        return 'Server error. Please try again later.';
      case 503:
        return 'Service unavailable. Please try again later.';
      default:
        return `An error occurred while processing ${this.config.entityName}.`;
    }
  }
}
