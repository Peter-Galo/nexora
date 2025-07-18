import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface ExportJob {
  uuid: string;
  userUuid: string;
  category: string;
  exportType: string;
  status: string;
  fileUrl?: string;
  errorMessage?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ExportResponse {
  jobId: string;
  message: string;
}

@Injectable({
  providedIn: 'root',
})
export class ExportService {
  private static readonly API_URL = `${environment.apiUrl}/inventory/export`;

  constructor(private readonly http: HttpClient) {}

  /**
   * Request warehouse data export
   * @returns Observable with export job information
   */
  requestWarehouseExport(): Observable<ExportResponse> {
    return this.http
      .get<ExportResponse>(`${ExportService.API_URL}/WAREHOUSE`)
      .pipe(
        catchError((error) => {
          console.error('Error requesting warehouse export:', error);
          throw error;
        }),
      );
  }

  /**
   * Request product data export
   * @returns Observable with export job information
   */
  requestProductExport(): Observable<ExportResponse> {
    return this.http
      .get<ExportResponse>(`${ExportService.API_URL}/PRODUCT`)
      .pipe(
        catchError((error) => {
          console.error('Error requesting product export:', error);
          throw error;
        }),
      );
  }

  /**
   * Request stock data export
   * @returns Observable with export job information
   */
  requestStockExport(): Observable<ExportResponse> {
    return this.http.get<ExportResponse>(`${ExportService.API_URL}/STOCK`).pipe(
      catchError((error) => {
        console.error('Error requesting stock export:', error);
        throw error;
      }),
    );
  }

  /**
   * Get export job status
   * @param jobId - The export job ID
   * @returns Observable with export job status
   */
  getExportStatus(jobId: string): Observable<ExportJob> {
    return this.http
      .get<ExportJob>(`${ExportService.API_URL}/status/${jobId}`)
      .pipe(
        catchError((error) => {
          console.error('Error getting export status:', error);
          throw error;
        }),
      );
  }

  /**
   * Get all export jobs for the authenticated user
   * @returns Observable with list of export jobs
   */
  getUserExportJobs(): Observable<ExportJob[]> {
    return this.http.get<ExportJob[]>(`${ExportService.API_URL}/jobs`).pipe(
      catchError((error) => {
        console.error('Error fetching user export jobs:', error);
        throw error;
      }),
    );
  }

  /**
   * Helper method to trigger file download
   * @param jobId - The export job ID
   */
  triggerFileDownload(jobId: string): void {
    const downloadUrl = `${ExportService.API_URL}/download/${jobId}`;
    window.open(downloadUrl, '_blank');
  }
}
