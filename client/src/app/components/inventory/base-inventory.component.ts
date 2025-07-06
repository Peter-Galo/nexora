import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { ExportCategory, ExportUtilityService } from '../../services/inventory/export-utility.service';

/**
 * Base component class for inventory-related components
 * Provides common functionality like export, lifecycle management, and utility methods
 */
@Component({
  template: '',
})
export abstract class BaseInventoryComponent implements OnInit, OnDestroy {
  // Common properties
  loading = signal<boolean>(false);
  error = signal<string | null>(null);

  // Export state
  exportState: ReturnType<typeof this.exportUtilityService.createExportState>;

  // Cleanup
  protected destroy$ = new Subject<void>();

  // Abstract properties that must be implemented by child components
  protected abstract exportCategory: ExportCategory;

  constructor(protected exportUtilityService: ExportUtilityService) {
    this.exportState = this.exportUtilityService.createExportState();
  }

  ngOnInit(): void {
    this.loadData();
    this.loadExistingExportJobs();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Abstract method for loading component data
   * Must be implemented by child components
   */
  protected abstract loadData(): void;

  /**
   * Refresh data - calls loadData by default
   */
  refreshData(): void {
    this.loadData();
  }

  /**
   * Export data to Excel
   */
  exportData(): void {
    this.exportUtilityService.initiateExport(
      this.exportCategory,
      this.exportState,
      this.destroy$,
    );
  }

  /**
   * Download the exported file
   */
  downloadExport(jobId: string): void {
    this.exportUtilityService.downloadExport(jobId);
  }

  /**
   * Download file by URL
   */
  downloadFileByUrl(fileUrl: string): void {
    this.exportUtilityService.downloadFileByUrl(fileUrl);
  }

  /**
   * Check if the current user can export data
   * Default implementation allows all users - override in child components for specific permissions
   */
  canExportData(): boolean {
    return true;
  }

  /**
   * Load existing export jobs for the current user
   */
  loadExistingExportJobs(): void {
    if (!this.canExportData()) {
      return;
    }

    this.exportUtilityService.loadExistingExportJobs(
      this.exportCategory,
      this.exportState,
      this.destroy$,
    );
  }

  /**
   * Format date string to localized date
   */
  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }

  /**
   * Get status class for active/inactive states
   */
  getStatusClass(active: boolean): string {
    return active ? 'badge bg-success' : 'badge bg-secondary';
  }

  /**
   * Get status text for active/inactive states
   */
  getStatusText(active: boolean): string {
    return active ? 'Active' : 'Inactive';
  }

  /**
   * Handle errors with consistent error messaging
   */
  protected handleError(error: any, context: string): void {
    console.error(`Error in ${context}:`, error);
    this.error.set(`Failed to ${context}. Please try again.`);
    this.loading.set(false);
  }

  /**
   * Set loading state
   */
  protected setLoading(loading: boolean): void {
    this.loading.set(loading);
  }

  /**
   * Clear error state
   */
  clearError(): void {
    this.error.set(null);
  }

  /**
   * Generic method to handle API calls with loading and error states
   */
  protected handleApiCall<T>(
    apiCall: () => any,
    successCallback: (data: T) => void,
    context: string,
  ): void {
    this.setLoading(true);
    this.clearError();

    apiCall()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: T) => {
          successCallback(data);
          this.setLoading(false);
        },
        error: (error: any) => {
          this.handleError(error, context);
        },
      });
  }
}
