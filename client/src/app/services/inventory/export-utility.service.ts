import { Injectable, signal } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, takeUntil } from 'rxjs/operators';
import { ExportJob, ExportService } from './export.service';

export interface ExportedFile {
  jobId: string;
  fileName: string;
  fileUrl: string;
  createdAt: Date;
}

export type ExportCategory = 'WAREHOUSE' | 'STOCK' | 'PRODUCT';

@Injectable({
  providedIn: 'root',
})
export class ExportUtilityService {
  constructor(private exportService: ExportService) {}

  createExportState() {
    return {
      exportLoading: signal<boolean>(false),
      exportJobId: signal<string | null>(null),
      exportStatus: signal<string | null>(null),
      exportedFiles: signal<ExportedFile[]>([]),
      error: signal<string | null>(null),
    };
  }

  /**
   * Generic export method that can be used for any export category
   */
  initiateExport(
    category: ExportCategory,
    exportState: ReturnType<typeof this.createExportState>,
    destroy$: Observable<void>,
  ): void {
    exportState.exportLoading.set(true);
    exportState.error.set(null);
    exportState.exportJobId.set(null);
    exportState.exportStatus.set(null);

    let exportRequest: Observable<any>;

    switch (category) {
      case 'WAREHOUSE':
        exportRequest = this.exportService.requestWarehouseExport();
        break;
      case 'STOCK':
        exportRequest = this.exportService.requestStockExport();
        break;
      case 'PRODUCT':
        exportRequest = this.exportService.requestProductExport();
        break;
      default:
        exportState.exportLoading.set(false);
        exportState.error.set('Invalid export category');
        return;
    }

    exportRequest
      .pipe(
        catchError((err) => {
          console.error(
            `Error requesting ${category.toLowerCase()} export:`,
            err,
          );
          exportState.error.set(
            `Failed to request ${category.toLowerCase()} export. Please try again.`,
          );
          return of(null);
        }),
        takeUntil(destroy$),
      )
      .subscribe((response) => {
        exportState.exportLoading.set(false);
        if (response) {
          exportState.exportJobId.set(response.jobId);
          exportState.exportStatus.set('PENDING');

          // Start polling for export status
          this.pollExportStatus(
            response.jobId,
            category,
            exportState,
            destroy$,
          );

          console.log(`${category} export initiated successfully:`, response);
        }
      });
  }

  /**
   * Poll export status until completion
   */
  private pollExportStatus(
    jobId: string,
    category: ExportCategory,
    exportState: ReturnType<typeof this.createExportState>,
    destroy$: Observable<void>,
  ): void {
    const pollInterval = setInterval(() => {
      this.exportService
        .getExportStatus(jobId)
        .pipe(
          catchError((err) => {
            console.error('Error checking export status:', err);
            clearInterval(pollInterval);
            exportState.exportStatus.set('FAILED');
            exportState.error.set('Failed to check export status.');
            return of(null);
          }),
          takeUntil(destroy$),
        )
        .subscribe((job) => {
          if (job) {
            exportState.exportStatus.set(job.status);

            if (job.status === 'COMPLETED') {
              clearInterval(pollInterval);
              this.addExportedFile(job, category, exportState);
            } else if (job.status === 'FAILED') {
              clearInterval(pollInterval);
              exportState.error.set(
                job.errorMessage || 'Export failed. Please try again.',
              );
            }
          }
        });
    }, 2000); // Poll every 2 seconds

    // Stop polling after 5 minutes to prevent infinite polling
    setTimeout(() => {
      clearInterval(pollInterval);
      if (
        exportState.exportStatus() === 'PROCESSING' ||
        exportState.exportStatus() === 'PENDING'
      ) {
        exportState.exportStatus.set('TIMEOUT');
        exportState.error.set(
          'Export is taking longer than expected. Please check back later.',
        );
      }
    }, 300000); // 5 minutes
  }

  /**
   * Add completed export to the exported files list
   */
  private addExportedFile(
    job: ExportJob,
    category: ExportCategory,
    exportState: ReturnType<typeof this.createExportState>,
  ): void {
    if (job.fileUrl) {
      const fileName = `${category.toLowerCase()}_export_${new Date().toISOString().split('T')[0]}.xlsx`;
      const exportedFile: ExportedFile = {
        jobId: job.uuid,
        fileName: fileName,
        fileUrl: job.fileUrl,
        createdAt: new Date(),
      };

      const currentFiles = exportState.exportedFiles();
      exportState.exportedFiles.set([exportedFile, ...currentFiles]);

      // Clear export status after adding to the list
      exportState.exportStatus.set(null);
      exportState.exportJobId.set(null);
    }
  }

  /**
   * Download the exported file
   */
  downloadExport(jobId: string): void {
    this.exportService.triggerFileDownload(jobId);
  }

  /**
   * Load existing export jobs for a specific category
   */
  loadExistingExportJobs(
    category: ExportCategory,
    exportState: ReturnType<typeof this.createExportState>,
    destroy$: Observable<void>,
  ): void {
    this.exportService
      .getUserExportJobs()
      .pipe(
        catchError((err) => {
          console.error('Error loading existing export jobs:', err);
          return of([]);
        }),
        takeUntil(destroy$),
      )
      .subscribe((jobs) => {
        // Filter for the specific category exports jobs and converts to the expected format
        const categoryExportJobs = jobs
          .filter(
            (job) =>
              job.category === category &&
              job.status === 'COMPLETED' &&
              job.fileUrl,
          )
          .map((job) => ({
            jobId: job.uuid,
            fileName: `${category.toLowerCase()}_export_${new Date(job.createdAt).toISOString().split('T')[0]}.xlsx`,
            fileUrl: job.fileUrl!,
            createdAt: new Date(job.createdAt),
          }));

        exportState.exportedFiles.set(categoryExportJobs);
      });
  }
}
