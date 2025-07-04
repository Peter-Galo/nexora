import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface WarehouseDTO {
  uuid?: string;
  code: string;
  name: string;
  description?: string;
  address?: string;
  city?: string;
  stateProvince?: string;
  postalCode?: string;
  country?: string;
  active?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root',
})
export class WarehouseService {
  private static readonly API_URL =
    'http://localhost:8080/api/v1/inventory/warehouses';

  constructor(private readonly http: HttpClient) {}

  /**
   * Fetches all warehouses from the API
   * @returns Observable with the list of warehouses
   */
  getAllWarehouses(): Observable<WarehouseDTO[]> {
    return this.http.get<WarehouseDTO[]>(`${WarehouseService.API_URL}`).pipe(
      catchError((error) => {
        console.error('Error fetching all warehouses:', error);
        throw error;
      }),
    );
  }

  /**
   * Fetches only active warehouses from the API
   * @returns Observable with the list of active warehouses
   */
  getActiveWarehouses(): Observable<WarehouseDTO[]> {
    return this.http
      .get<WarehouseDTO[]>(`${WarehouseService.API_URL}/active`)
      .pipe(
        catchError((error) => {
          console.error('Error fetching active warehouses:', error);
          throw error;
        }),
      );
  }

  /**
   * Fetches a warehouse by ID
   * @param id - The warehouse UUID
   * @returns Observable with the warehouse data
   */
  getWarehouseById(id: string): Observable<WarehouseDTO> {
    return this.http
      .get<WarehouseDTO>(`${WarehouseService.API_URL}/${id}`)
      .pipe(
        catchError((error) => {
          console.error('Error fetching warehouse by ID:', error);
          throw error;
        }),
      );
  }

  /**
   * Fetches a warehouse by code
   * @param code - The warehouse code
   * @returns Observable with the warehouse data
   */
  getWarehouseByCode(code: string): Observable<WarehouseDTO> {
    return this.http
      .get<WarehouseDTO>(`${WarehouseService.API_URL}/code/${code}`)
      .pipe(
        catchError((error) => {
          console.error('Error fetching warehouse by code:', error);
          throw error;
        }),
      );
  }

  /**
   * Creates a new warehouse
   * @param warehouse - The warehouse data to create
   * @returns Observable with the created warehouse
   */
  createWarehouse(warehouse: WarehouseDTO): Observable<WarehouseDTO> {
    return this.http
      .post<WarehouseDTO>(`${WarehouseService.API_URL}`, warehouse)
      .pipe(
        catchError((error) => {
          console.error('Error creating warehouse:', error);
          throw error;
        }),
      );
  }

  /**
   * Updates an existing warehouse
   * @param id - The warehouse UUID
   * @param warehouse - The updated warehouse data
   * @returns Observable with the updated warehouse
   */
  updateWarehouse(
    id: string,
    warehouse: WarehouseDTO,
  ): Observable<WarehouseDTO> {
    return this.http
      .put<WarehouseDTO>(`${WarehouseService.API_URL}/${id}`, warehouse)
      .pipe(
        catchError((error) => {
          console.error('Error updating warehouse:', error);
          throw error;
        }),
      );
  }

  /**
   * Deletes a warehouse
   * @param id - The warehouse UUID
   * @returns Observable with void response
   */
  deleteWarehouse(id: string): Observable<void> {
    return this.http.delete<void>(`${WarehouseService.API_URL}/${id}`).pipe(
      catchError((error) => {
        console.error('Error deleting warehouse:', error);
        throw error;
      }),
    );
  }

  /**
   * Activates a warehouse
   * @param id - The warehouse UUID
   * @returns Observable with the activated warehouse
   */
  activateWarehouse(id: string): Observable<WarehouseDTO> {
    return this.http
      .put<WarehouseDTO>(`${WarehouseService.API_URL}/${id}/activate`, {})
      .pipe(
        catchError((error) => {
          console.error('Error activating warehouse:', error);
          throw error;
        }),
      );
  }

  /**
   * Deactivates a warehouse
   * @param id - The warehouse UUID
   * @returns Observable with the deactivated warehouse
   */
  deactivateWarehouse(id: string): Observable<WarehouseDTO> {
    return this.http
      .put<WarehouseDTO>(`${WarehouseService.API_URL}/${id}/deactivate`, {})
      .pipe(
        catchError((error) => {
          console.error('Error deactivating warehouse:', error);
          throw error;
        }),
      );
  }

  /**
   * Searches warehouses by name
   * @param name - The search term for warehouse name
   * @returns Observable with the list of matching warehouses
   */
  searchWarehousesByName(name: string): Observable<WarehouseDTO[]> {
    return this.http
      .get<WarehouseDTO[]>(`${WarehouseService.API_URL}/search`, {
        params: { name },
      })
      .pipe(
        catchError((error) => {
          console.error('Error searching warehouses by name:', error);
          throw error;
        }),
      );
  }

  /**
   * Fetches warehouses by city
   * @param city - The city name
   * @returns Observable with the list of warehouses in the city
   */
  getWarehousesByCity(city: string): Observable<WarehouseDTO[]> {
    return this.http
      .get<WarehouseDTO[]>(`${WarehouseService.API_URL}/city/${city}`)
      .pipe(
        catchError((error) => {
          console.error('Error fetching warehouses by city:', error);
          throw error;
        }),
      );
  }

  /**
   * Fetches warehouses by state/province
   * @param stateProvince - The state or province name
   * @returns Observable with the list of warehouses in the state/province
   */
  getWarehousesByStateProvince(
    stateProvince: string,
  ): Observable<WarehouseDTO[]> {
    return this.http
      .get<WarehouseDTO[]>(`${WarehouseService.API_URL}/state/${stateProvince}`)
      .pipe(
        catchError((error) => {
          console.error('Error fetching warehouses by state/province:', error);
          throw error;
        }),
      );
  }

  /**
   * Fetches warehouses by country
   * @param country - The country name
   * @returns Observable with the list of warehouses in the country
   */
  getWarehousesByCountry(country: string): Observable<WarehouseDTO[]> {
    return this.http
      .get<WarehouseDTO[]>(`${WarehouseService.API_URL}/country/${country}`)
      .pipe(
        catchError((error) => {
          console.error('Error fetching warehouses by country:', error);
          throw error;
        }),
      );
  }
}
