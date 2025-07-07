import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AggregateReportData } from '../../components/inventory/models/inventory.models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class InventoryService {
  private static readonly API_URL = `${environment.apiUrl}/inventory`;

  constructor(private readonly http: HttpClient) {}

  /**
   * Fetches the aggregate inventory report data from the API
   * @returns Observable with the aggregate report data
   */
  getAggregateReport(): Observable<AggregateReportData> {
    return this.http
      .get<AggregateReportData>(`${InventoryService.API_URL}/report/aggregate`)
      .pipe(
        catchError((error) => {
          console.error('Error fetching aggregate report:', error);
          throw error;
        }),
      );
  }
}
