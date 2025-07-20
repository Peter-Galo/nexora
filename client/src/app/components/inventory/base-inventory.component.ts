import { Component, effect, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { ExportCategory, ExportUtilityService } from '../../services/inventory/export-utility.service';
import { AppStateService } from '../../core/state/app-state.service';

/**
 * Modern Base Inventory Component using Angular Signals and State Management
 * Provides common functionality like export, lifecycle management, and utility methods
 */
@Component({
  template: '',
})
export abstract class BaseInventoryComponent implements OnInit, OnDestroy {
  // Dependency injection using modern inject function
  protected readonly exportUtilityService = inject(ExportUtilityService);
  protected readonly appStateService = inject(AppStateService);

  // Component-specific signals
  private readonly _loading = signal<boolean>(false);
  private readonly _error = signal<string | null>(null);
  private readonly _refreshTrigger = signal<number>(0);

  // Public signals - writable for child components
  readonly loading = this._loading;
  readonly error = this._error;

  // Export state
  exportState: ReturnType<typeof this.exportUtilityService.createExportState>;

  // Cleanup
  protected destroy$ = new Subject<void>();

  // Abstract properties that must be implemented by child components
  protected abstract exportCategory: ExportCategory;

  constructor() {
    this.exportState = this.exportUtilityService.createExportState();

    // Set up reactive effects
    this.setupReactiveEffects();
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
   * Set up reactive effects for component state management
   */
  private setupReactiveEffects(): void {
    // Effect to sync component loading state with global app state
    effect(() => {
      if (this._loading()) {
        this.appStateService.setLoading(true);
      }
    });

    // Effect to sync component error state with global app state
    effect(() => {
      const error = this._error();
      if (error) {
        this.appStateService.setError(error);
      }
    });

    // Effect to handle refresh triggers
    effect(() => {
      const trigger = this._refreshTrigger();
      if (trigger > 0) {
        this.loadData();
      }
    });
  }

  /**
   * Abstract method for loading component data
   * Must be implemented by child components
   */
  protected abstract loadData(): void;

  /**
   * Refresh data - triggers reactive refresh
   */
  refreshData(): void {
    this._refreshTrigger.update((count) => count + 1);
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
   * Handle errors with consistent error messaging and state management
   */
  protected handleError(error: any, context: string): void {
    console.error(`Error in ${context}:`, error);

    // Extract meaningful error message
    const errorMessage = this.extractErrorMessage(error, context);

    this._error.set(errorMessage);
    this._loading.set(false);

    // Also update global app state
    this.appStateService.setError(errorMessage);
    this.appStateService.setLoading(false);
  }

  /**
   * Extract meaningful error message from error object
   */
  private extractErrorMessage(error: any, context: string): string {
    if (error?.message) {
      return error.message;
    }

    if (error?.error?.message) {
      return error.error.message;
    }

    if (error?.status) {
      switch (error.status) {
        case 400:
          return `Invalid request for ${context}. Please check your input.`;
        case 401:
          return 'Authentication required. Please log in again.';
        case 403:
          return `Access denied for ${context}. You do not have permission.`;
        case 404:
          return `Resource not found for ${context}.`;
        case 500:
          return `Server error occurred during ${context}. Please try again later.`;
        default:
          return `Failed to ${context}. Please try again.`;
      }
    }

    return `Failed to ${context}. Please try again.`;
  }

  /**
   * Set loading state
   */
  protected setLoading(loading: boolean): void {
    this._loading.set(loading);
  }

  /**
   * Clear error state
   */
  clearError(): void {
    this._error.set(null);
    this.appStateService.clearError();
  }

  /**
   * Modern generic method to handle API calls with loading and error states
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
          this.appStateService.setLoading(false);
        },
        error: (error: any) => {
          this.handleError(error, context);
        },
      });
  }

  /**
   * Enhanced API call handler with retry logic
   */
  protected handleApiCallWithRetry<T>(
    apiCall: () => any,
    successCallback: (data: T) => void,
    context: string,
    retryCount: number = 3,
  ): void {
    this.setLoading(true);
    this.clearError();

    apiCall()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: T) => {
          successCallback(data);
          this.setLoading(false);
          this.appStateService.setLoading(false);
        },
        error: (error: any) => {
          if (retryCount > 0 && this.shouldRetry(error)) {
            console.warn(
              `Retrying ${context}, attempts left: ${retryCount - 1}`,
            );
            setTimeout(() => {
              this.handleApiCallWithRetry(
                apiCall,
                successCallback,
                context,
                retryCount - 1,
              );
            }, 1000);
          } else {
            this.handleError(error, context);
          }
        },
      });
  }

  /**
   * Determine if an error should trigger a retry
   */
  private shouldRetry(error: any): boolean {
    // Retry on network errors or server errors (5xx)
    return !error.status || error.status >= 500;
  }
}
