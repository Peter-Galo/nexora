import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ExportJob, ExportService } from '../services/export.service';
import { Subject, takeUntil, catchError, of } from 'rxjs';

@Component({
  selector: 'app-export-jobs',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './export-jobs.component.html',
  styleUrl: './export-jobs.component.css',
})
export class ExportJobsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  exportJobs = signal<ExportJob[]>([]);
  loading = signal<boolean>(false);
  error = signal<string | null>(null);

  constructor(private exportService: ExportService) {}

  ngOnInit(): void {
    this.loadExportJobs();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Load all export jobs for the authenticated user
   */
  loadExportJobs(): void {
    this.loading.set(true);
    this.error.set(null);

    this.exportService
      .getUserExportJobs()
      .pipe(
        catchError((err) => {
          console.error('Error loading export jobs:', err);
          this.error.set('Failed to load export jobs. Please try again.');
          return of([]);
        }),
        takeUntil(this.destroy$),
      )
      .subscribe((jobs) => {
        this.exportJobs.set(jobs);
        this.loading.set(false);
      });
  }

  /**
   * Refresh the export jobs list
   */
  refresh(): void {
    this.loadExportJobs();
  }

  /**
   * Download an export file using the file URL
   */
  downloadExport(fileUrl: string): void {
    window.open(fileUrl, '_blank');
  }

  /**
   * Get status badge class for styling
   */
  getStatusBadgeClass(status: string): string {
    switch (status.toLowerCase()) {
      case 'completed':
        return 'bg-success';
      case 'pending':
        return 'bg-warning';
      case 'processing':
        return 'bg-info';
      case 'failed':
        return 'bg-danger';
      default:
        return 'bg-secondary';
    }
  }

  /**
   * Get category display name
   */
  getCategoryDisplayName(category: string): string {
    switch (category.toLowerCase()) {
      case 'warehouse':
        return 'Warehouse';
      case 'product':
        return 'Product';
      case 'stock':
        return 'Stock';
      default:
        return category;
    }
  }

  /**
   * Check if export job can be downloaded
   */
  canDownload(job: ExportJob): boolean {
    return job.status === 'COMPLETED' && !!job.fileUrl;
  }

  /**
   * Format date for display
   */
  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }
}
