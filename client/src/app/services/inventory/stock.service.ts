import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';

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
  providedIn: 'root'
})
export class StockService {
  private static readonly API_URL = 'http://localhost:8080/api/v1/inventory/stocks';

  constructor(private readonly http: HttpClient) {}

  /**
   * Get all stock records
   */
  getAllStocks(): Observable<StockDTO[]> {
    return this.http.get<StockDTO[]>(`${StockService.API_URL}`)
      .pipe(
        catchError(error => {
          console.error('Error fetching all stocks:', error);
          throw error;
        })
      );
  }

  /**
   * Get stock record by ID
   */
  getStockById(id: string): Observable<StockDTO> {
    return this.http.get<StockDTO>(`${StockService.API_URL}/${id}`)
      .pipe(
        catchError(error => {
          console.error('Error fetching stock by ID:', error);
          throw error;
        })
      );
  }

  /**
   * Get stocks by product ID
   */
  getStocksByProductId(productId: string): Observable<StockDTO[]> {
    return this.http.get<StockDTO[]>(`${StockService.API_URL}/product/${productId}`)
      .pipe(
        catchError(error => {
          console.error('Error fetching stocks by product ID:', error);
          throw error;
        })
      );
  }

  /**
   * Get stocks by product code
   */
  getStocksByProductCode(productCode: string): Observable<StockDTO[]> {
    return this.http.get<StockDTO[]>(`${StockService.API_URL}/product/code/${productCode}`)
      .pipe(
        catchError(error => {
          console.error('Error fetching stocks by product code:', error);
          throw error;
        })
      );
  }

  /**
   * Get stocks by warehouse ID
   */
  getStocksByWarehouseId(warehouseId: string): Observable<StockDTO[]> {
    return this.http.get<StockDTO[]>(`${StockService.API_URL}/warehouse/${warehouseId}`)
      .pipe(
        catchError(error => {
          console.error('Error fetching stocks by warehouse ID:', error);
          throw error;
        })
      );
  }

  /**
   * Get stocks by warehouse code
   */
  getStocksByWarehouseCode(warehouseCode: string): Observable<StockDTO[]> {
    return this.http.get<StockDTO[]>(`${StockService.API_URL}/warehouse/code/${warehouseCode}`)
      .pipe(
        catchError(error => {
          console.error('Error fetching stocks by warehouse code:', error);
          throw error;
        })
      );
  }

  /**
   * Get stock for specific product in specific warehouse
   */
  getStockByProductAndWarehouse(productId: string, warehouseId: string): Observable<StockDTO> {
    return this.http.get<StockDTO>(`${StockService.API_URL}/product/${productId}/warehouse/${warehouseId}`)
      .pipe(
        catchError(error => {
          console.error('Error fetching stock by product and warehouse:', error);
          throw error;
        })
      );
  }

  /**
   * Get low stock records (critical for business operations)
   */
  getLowStocks(): Observable<StockDTO[]> {
    return this.http.get<StockDTO[]>(`${StockService.API_URL}/low`)
      .pipe(
        catchError(error => {
          console.error('Error fetching low stocks:', error);
          throw error;
        })
      );
  }

  /**
   * Get over stock records (excess inventory)
   */
  getOverStocks(): Observable<StockDTO[]> {
    return this.http.get<StockDTO[]>(`${StockService.API_URL}/over`)
      .pipe(
        catchError(error => {
          console.error('Error fetching over stocks:', error);
          throw error;
        })
      );
  }

  /**
   * Get zero stock records (out of stock items)
   */
  getZeroStocks(): Observable<StockDTO[]> {
    return this.http.get<StockDTO[]>(`${StockService.API_URL}/zero`)
      .pipe(
        catchError(error => {
          console.error('Error fetching zero stocks:', error);
          throw error;
        })
      );
  }

  /**
   * Create new stock record
   */
  createStock(stock: Partial<StockDTO>): Observable<StockDTO> {
    return this.http.post<StockDTO>(`${StockService.API_URL}`, stock)
      .pipe(
        catchError(error => {
          console.error('Error creating stock:', error);
          throw error;
        })
      );
  }

  /**
   * Update existing stock record
   */
  updateStock(id: string, stock: Partial<StockDTO>): Observable<StockDTO> {
    return this.http.put<StockDTO>(`${StockService.API_URL}/${id}`, stock)
      .pipe(
        catchError(error => {
          console.error('Error updating stock:', error);
          throw error;
        })
      );
  }

  /**
   * Delete stock record
   */
  deleteStock(id: string): Observable<void> {
    return this.http.delete<void>(`${StockService.API_URL}/${id}`)
      .pipe(
        catchError(error => {
          console.error('Error deleting stock:', error);
          throw error;
        })
      );
  }

  /**
   * Add stock quantity
   */
  addStock(id: string, quantity: number): Observable<StockDTO> {
    const params = new HttpParams().set('quantity', quantity.toString());
    return this.http.put<StockDTO>(`${StockService.API_URL}/${id}/add`, {}, { params })
      .pipe(
        catchError(error => {
          console.error('Error adding stock:', error);
          throw error;
        })
      );
  }

  /**
   * Remove stock quantity
   */
  removeStock(id: string, quantity: number): Observable<StockDTO> {
    const params = new HttpParams().set('quantity', quantity.toString());
    return this.http.put<StockDTO>(`${StockService.API_URL}/${id}/remove`, {}, { params })
      .pipe(
        catchError(error => {
          console.error('Error removing stock:', error);
          throw error;
        })
      );
  }
}
