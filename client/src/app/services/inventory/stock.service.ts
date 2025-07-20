import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface ProductDTO {
  uuid: string;
  code: string;
  name: string;
  description?: string;
  price: number;
  category?: string;
  brand?: string;
  sku?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface WarehouseDTO {
  uuid: string;
  code: string;
  name: string;
  location?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface StockDTO {
  uuid: string;
  product: ProductDTO;
  warehouse: WarehouseDTO;
  quantity: number;
  minStockLevel?: number;
  maxStockLevel?: number;
  lowStock: boolean;
  overStock: boolean;
  lastRestockDate?: string;
  createdAt: string;
  updatedAt: string;

  // Computed fields for backward compatibility
  productUuid: string;
  productCode: string;
  productName: string;
  warehouseUuid: string;
  warehouseName: string;
  warehouseCode: string;
  lastUpdated: string;
}

@Injectable({
  providedIn: 'root',
})
export class StockService {
  private static readonly API_URL = `${environment.apiUrl}/inventory/stocks`;

  constructor(private readonly http: HttpClient) {}

  /**
   * Get all stock records
   */
  getAllStocks(): Observable<StockDTO[]> {
    return this.http.get<StockDTO[]>(`${StockService.API_URL}`).pipe(
      catchError((error) => {
        console.error('Error fetching all stocks:', error);
        throw error;
      }),
    );
  }

  /**
   * Add stock quantity
   */
  addStock(id: string, quantity: number): Observable<StockDTO> {
    const params = new HttpParams().set('quantity', quantity.toString());
    return this.http
      .put<StockDTO>(`${StockService.API_URL}/${id}/add`, {}, { params })
      .pipe(
        catchError((error) => {
          console.error('Error adding stock:', error);
          throw error;
        }),
      );
  }

  /**
   * Remove stock quantity
   */
  removeStock(id: string, quantity: number): Observable<StockDTO> {
    const params = new HttpParams().set('quantity', quantity.toString());
    return this.http
      .put<StockDTO>(`${StockService.API_URL}/${id}/remove`, {}, { params })
      .pipe(
        catchError((error) => {
          console.error('Error removing stock:', error);
          throw error;
        }),
      );
  }
}
