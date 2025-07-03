import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AggregateReportData } from '../models/inventory.models';

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private static readonly API_URL = 'http://localhost:8080/api/v1/inventory';

  constructor(private readonly http: HttpClient) {}

  /**
   * Fetches the aggregate inventory report data from the API
   * @returns Observable with the aggregate report data
   */
  getAggregateReport(): Observable<AggregateReportData> {
    return this.http.get<AggregateReportData>(`${InventoryService.API_URL}/report/aggregate`)
      .pipe(
        catchError(error => {
          console.error('Error fetching aggregate report:', error);
          throw error;
        })
      );
  }
}
