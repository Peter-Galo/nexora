import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ExportJob,
  ExportService,
} from '../../../services/inventory/export.service';
import { catchError, of, Subject, takeUntil } from 'rxjs';
import {
  DataTableComponent,
  TableColumn,
} from '../../shared/data-table/data-table.component';

@Component({
  selector: 'app-export-jobs',
  standalone: true,
  imports: [CommonModule, DataTableComponent],
  templateUrl: './export-jobs.component.html',
})
export class ExportJobsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  exportJobs = signal<ExportJob[]>([]);
  loading = signal<boolean>(false);
  error = signal<string | null>(null);

  // Define columns for the data table
  exportJobColumns: TableColumn[] = [
    { header: 'Category', field: 'category', customTemplate: true },
    { header: 'Type', field: 'exportType', customTemplate: true },
    { header: 'Status', field: 'status', customTemplate: true },
    { header: 'Created', field: 'createdAt', customTemplate: true },
    { header: 'Updated', field: 'updatedAt', customTemplate: true },
    { header: 'Actions', field: 'actions', customTemplate: true },
  ];

  constructor(private exportService: ExportService) {}

  ngOnInit(): void {
    this.loadExportJobs();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

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

  refresh(): void {
    this.loadExportJobs();
  }

  downloadExport(fileUrl: string): void {
    window.open(fileUrl, '_blank');
  }

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

  canDownload(job: ExportJob): boolean {
    return job.status === 'COMPLETED' && !!job.fileUrl;
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }
}
