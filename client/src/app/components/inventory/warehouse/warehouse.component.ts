import { Component, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  WarehouseDTO,
  WarehouseService,
} from '../../../services/inventory/warehouse.service';
import { DataTableComponent } from '../../shared/data-table/data-table.component';
import { WAREHOUSE_MANAGEMENT_COLUMNS } from '../../shared/data-table/table-columns/model';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  of,
  Subject,
  switchMap,
  takeUntil,
} from 'rxjs';
import { AuthService } from '../../../auth/services/auth.service';
import { ExportUtilityService } from '../../../services/inventory/export-utility.service';
import { BaseInventoryComponent } from '../base-inventory.component';

@Component({
  selector: 'app-warehouse',
  standalone: true,
  imports: [CommonModule, FormsModule, DataTableComponent],
  templateUrl: './warehouse.component.html',
})
export class WarehouseComponent extends BaseInventoryComponent {
  // Export category for base class
  protected exportCategory = 'WAREHOUSE' as const;

  // Data signals
  warehouses = signal<WarehouseDTO[]>([]);

  // UI state
  showActiveOnly = signal<boolean>(false);
  searchTerm = signal<string>('');

  // Create warehouse form state
  showCreateForm = signal<boolean>(false);
  createFormData = signal<Partial<WarehouseDTO>>({
    code: '',
    name: '',
    description: '',
    address: '',
    city: '',
    stateProvince: '',
    postalCode: '',
    country: '',
    active: true,
  });
  creating = signal<boolean>(false);

  // Form validation errors
  formErrors = signal<{ [key: string]: string }>({});

  // Field validation states
  fieldTouched = signal<{ [key: string]: boolean }>({});

  // Column definitions - dynamically filtered based on user permissions
  protected readonly warehouseColumns = computed(() => {
    const hasAnyPermissions =
      this.canModifyWarehouses() || this.canDeleteWarehouses();

    if (hasAnyPermissions) {
      // User has permissions, show all columns including actions
      return WAREHOUSE_MANAGEMENT_COLUMNS;
    } else {
      // User has no permissions, filter out the actions column
      return WAREHOUSE_MANAGEMENT_COLUMNS.filter(
        (column) => column.field !== 'actions',
      );
    }
  });

  // Search debouncing
  private searchSubject = new Subject<string>();

  constructor(
    private warehouseService: WarehouseService,
    private authService: AuthService,
    exportUtilityService: ExportUtilityService,
  ) {
    super(exportUtilityService);

    // Set up debounced search with 0.2 second delay
    this.searchSubject
      .pipe(
        debounceTime(200), // 0.2 second delay
        distinctUntilChanged(),
        switchMap((term) => {
          if (!term.trim()) {
            // If search term is empty, load all warehouses
            return this.showActiveOnly()
              ? this.warehouseService.getActiveWarehouses()
              : this.warehouseService.getAllWarehouses();
          } else {
            // Search by name
            return this.warehouseService.searchWarehousesByName(term.trim());
          }
        }),
        catchError((err) => {
          console.error('Error in debounced search:', err);
          this.error.set('Failed to search warehouses. Please try again.');
          return of([]);
        }),
        takeUntil(this.destroy$),
      )
      .subscribe((data) => {
        this.warehouses.set(data);
        this.loading.set(false);
      });
  }

  /**
   * Implementation of abstract loadData method from BaseInventoryComponent
   */
  protected loadData(): void {
    this.loadWarehouses();
  }

  /**
   * Check if the current user has permission to modify warehouses
   */
  canModifyWarehouses(): boolean {
    const user = this.authService.getUser();
    return user?.role === 'MANAGER' || user?.role === 'ADMIN';
  }

  /**
   * Check if the current user has permission to delete warehouses (ADMIN only)
   */
  canDeleteWarehouses(): boolean {
    const user = this.authService.getUser();
    return user?.role === 'ADMIN';
  }

  /**
   * Check if the current user has permission to create warehouses (ADMIN only)
   */
  canCreateWarehouses(): boolean {
    const user = this.authService.getUser();
    return user?.role === 'ADMIN';
  }

  /**
   * Load warehouses based on current filters
   */
  loadWarehouses(): void {
    this.loading.set(true);
    this.error.set(null);

    const request = this.showActiveOnly()
      ? this.warehouseService.getActiveWarehouses()
      : this.warehouseService.getAllWarehouses();

    request
      .pipe(
        catchError((err) => {
          console.error('Error loading warehouses:', err);
          this.error.set('Failed to load warehouses. Please try again.');
          return of([]);
        }),
      )
      .subscribe((data) => {
        this.warehouses.set(data);
        this.loading.set(false);
      });
  }

  /**
   * Toggle between showing all warehouses and active only
   */
  toggleActiveFilter(): void {
    this.showActiveOnly.set(!this.showActiveOnly());
    this.loadWarehouses();
  }

  /**
   * Handle search input changes with debouncing
   */
  onSearchInput(searchTerm: string): void {
    this.searchTerm.set(searchTerm);
    this.loading.set(true);
    this.error.set(null);
    this.searchSubject.next(searchTerm);
  }

  /**
   * Search warehouses by name (for manual trigger if needed)
   */
  searchWarehouses(): void {
    const term = this.searchTerm().trim();
    this.onSearchInput(term);
  }

  private setWarehouseActiveState(
    warehouse: WarehouseDTO,
    active: boolean,
    permissionError: string,
    accessDeniedError: string,
    generalError: string,
  ): void {
    if (!warehouse.uuid) return;

    if (!this.canModifyWarehouses()) {
      this.error.set(permissionError);
      return;
    }

    const action$ = active
      ? this.warehouseService.activateWarehouse(warehouse.uuid)
      : this.warehouseService.deactivateWarehouse(warehouse.uuid);

    action$
      .pipe(
        catchError((err) => {
          console.error(
            `Error ${active ? 'activating' : 'deactivating'} warehouse:`,
            err,
          );
          if (err.status === 403) {
            this.error.set(accessDeniedError);
          } else {
            this.error.set(generalError);
          }
          return of(null);
        }),
      )
      .subscribe((updatedWarehouse) => {
        if (updatedWarehouse) {
          const warehouses = this.warehouses();
          const index = warehouses.findIndex((w) => w.uuid === warehouse.uuid);
          if (index !== -1) {
            warehouses[index] = updatedWarehouse;
            this.warehouses.set([...warehouses]);
          }
        }
      });
  }

  activateWarehouse(warehouse: WarehouseDTO): void {
    this.setWarehouseActiveState(
      warehouse,
      true,
      'You do not have permission to activate warehouses. MANAGER or ADMIN role required.',
      'Access denied. You do not have permission to activate warehouses. MANAGER or ADMIN role required.',
      'Failed to activate warehouse. Please try again.',
    );
  }

  deactivateWarehouse(warehouse: WarehouseDTO): void {
    this.setWarehouseActiveState(
      warehouse,
      false,
      'You do not have permission to deactivate warehouses. MANAGER or ADMIN role required.',
      'Access denied. You do not have permission to deactivate warehouses. MANAGER or ADMIN role required.',
      'Failed to deactivate warehouse. Please try again.',
    );
  }

  /**
   * Clear search and reload all warehouses
   */
  clearSearch(): void {
    this.searchTerm.set('');
    this.onSearchInput('');
  }

  /**
   * Delete a warehouse
   */
  deleteWarehouse(warehouse: WarehouseDTO): void {
    if (!warehouse.uuid) return;

    // Check permissions first
    if (!this.canDeleteWarehouses()) {
      this.error.set(
        'You do not have permission to delete warehouses. ADMIN role required.',
      );
      return;
    }

    if (
      !confirm(
        `Are you sure you want to delete warehouse "${warehouse.name}"? This action cannot be undone.`,
      )
    ) {
      return;
    }

    this.warehouseService
      .deleteWarehouse(warehouse.uuid)
      .pipe(
        catchError((err) => {
          console.error('Error deleting warehouse:', err);
          if (err.status === 403) {
            this.error.set(
              'Access denied. You do not have permission to delete warehouses. ADMIN role required.',
            );
          } else {
            this.error.set('Failed to delete warehouse. Please try again.');
          }
          return of(null);
        }),
      )
      .subscribe(() => {
        // Remove the warehouse from the list
        const warehouses = this.warehouses();
        this.warehouses.set(
          warehouses.filter((w) => w.uuid !== warehouse.uuid),
        );
      });
  }

  /**
   * Refresh the warehouse list
   */
  refresh(): void {
    this.loadWarehouses();
  }

  /**
   * Show the create warehouse form
   */
  showCreateWarehouseForm(): void {
    if (!this.canCreateWarehouses()) {
      this.error.set(
        'You do not have permission to create warehouses. ADMIN role required.',
      );
      return;
    }
    this.resetCreateForm();
    this.showCreateForm.set(true);
  }

  /**
   * Hide the create warehouse form
   */
  hideCreateWarehouseForm(): void {
    this.showCreateForm.set(false);
    this.resetCreateForm();
  }

  /**
   * Reset the create warehouse form
   */
  resetCreateForm(): void {
    this.createFormData.set({
      code: '',
      name: '',
      description: '',
      address: '',
      city: '',
      stateProvince: '',
      postalCode: '',
      country: '',
      active: true,
    });
    this.creating.set(false);
    this.formErrors.set({});
    this.fieldTouched.set({});
  }

  /**
   * Update form field value
   */
  updateFormField(field: keyof WarehouseDTO, value: any): void {
    const currentData = this.createFormData();
    this.createFormData.set({
      ...currentData,
      [field]: value,
    });

    // Mark field as touched
    const touched = this.fieldTouched();
    this.fieldTouched.set({
      ...touched,
      [field]: true,
    });

    // Validate the field
    this.validateField(field, value);
  }

  /**
   * Validate a specific field
   */
  validateField(field: keyof WarehouseDTO, value: any): void {
    const errors = this.formErrors();
    const newErrors = { ...errors };

    // Clear existing error for this field
    delete newErrors[field];

    // Validate based on field type
    switch (field) {
      case 'code':
        if (!value || !value.toString().trim()) {
          newErrors[field] = 'Warehouse code is required';
        } else if (value.toString().trim().length < 2) {
          newErrors[field] = 'Warehouse code must be at least 2 characters';
        } else if (value.toString().trim().length > 50) {
          newErrors[field] = 'Warehouse code cannot exceed 50 characters';
        }
        break;

      case 'name':
        if (!value || !value.toString().trim()) {
          newErrors[field] = 'Warehouse name is required';
        } else if (value.toString().trim().length < 2) {
          newErrors[field] = 'Warehouse name must be at least 2 characters';
        } else if (value.toString().trim().length > 100) {
          newErrors[field] = 'Warehouse name cannot exceed 100 characters';
        }
        break;

      case 'address':
        if (!value || !value.toString().trim()) {
          newErrors[field] = 'Address is required';
        } else if (value.toString().trim().length > 200) {
          newErrors[field] = 'Address cannot exceed 200 characters';
        }
        break;

      case 'city':
        if (!value || !value.toString().trim()) {
          newErrors[field] = 'City is required';
        } else if (value.toString().trim().length > 100) {
          newErrors[field] = 'City cannot exceed 100 characters';
        }
        break;

      case 'country':
        if (!value || !value.toString().trim()) {
          newErrors[field] = 'Country is required';
        } else if (value.toString().trim().length > 100) {
          newErrors[field] = 'Country cannot exceed 100 characters';
        }
        break;

      case 'description':
        if (value && value.toString().length > 500) {
          newErrors[field] = 'Description cannot exceed 500 characters';
        }
        break;

      case 'stateProvince':
        if (value && value.toString().length > 100) {
          newErrors[field] = 'State/Province cannot exceed 100 characters';
        }
        break;

      case 'postalCode':
        if (value && value.toString().length > 20) {
          newErrors[field] = 'Postal code cannot exceed 20 characters';
        }
        break;
    }

    this.formErrors.set(newErrors);
  }

  /**
   * Validate all form fields
   */
  validateAllFields(): boolean {
    const formData = this.createFormData();
    let isValid = true;

    // Validate all required and optional fields
    Object.keys(formData).forEach((key) => {
      const field = key as keyof WarehouseDTO;
      this.validateField(field, formData[field]);
    });

    // Mark all fields as touched
    const touchedFields: { [key: string]: boolean } = {};
    Object.keys(formData).forEach((key) => {
      touchedFields[key] = true;
    });
    this.fieldTouched.set(touchedFields);

    // Check if there are any errors
    const errors = this.formErrors();
    isValid = Object.keys(errors).length === 0;

    return isValid;
  }

  /**
   * Get error message for a specific field
   */
  getFieldError(field: keyof WarehouseDTO): string | null {
    const errors = this.formErrors();
    const touched = this.fieldTouched();

    if (touched[field] && errors[field]) {
      return errors[field];
    }

    return null;
  }

  /**
   * Check if a field has an error and is touched
   */
  hasFieldError(field: keyof WarehouseDTO): boolean {
    return !!this.getFieldError(field);
  }

  /**
   * Create a new warehouse
   */
  createWarehouse(): void {
    if (!this.canCreateWarehouses()) {
      this.error.set(
        'You do not have permission to create warehouses. ADMIN role required.',
      );
      return;
    }

    // Validate all fields before submission
    if (!this.validateAllFields()) {
      this.error.set(
        'Please fix the validation errors below before submitting.',
      );
      return;
    }

    const formData = this.createFormData();
    this.creating.set(true);
    this.error.set(null);

    this.warehouseService
      .createWarehouse(formData as WarehouseDTO)
      .pipe(
        catchError((err) => {
          console.error('Error creating warehouse:', err);
          this.handleCreateWarehouseError(err);
          return of(null);
        }),
      )
      .subscribe((newWarehouse) => {
        this.creating.set(false);
        if (newWarehouse) {
          // Add the new warehouse to the list
          const warehouses = this.warehouses();
          this.warehouses.set([newWarehouse, ...warehouses]);

          // Hide the form and reset
          this.hideCreateWarehouseForm();

          // Show success message (optional - you could add a success signal)
          console.log('Warehouse created successfully:', newWarehouse);
        }
      });
  }

  /**
   * Handle errors from warehouse creation
   */
  private handleCreateWarehouseError(err: any): void {
    if (err.status === 403) {
      this.error.set(
        'Access denied. You do not have permission to create warehouses. ADMIN role required.',
      );
    } else if (err.status === 400) {
      // Try to parse validation errors from backend
      if (err.error && err.error.errors) {
        this.handleBackendValidationErrors(err.error.errors);
      } else if (err.error && err.error.message) {
        // Handle specific error messages
        if (
          err.error.message.includes('code') &&
          err.error.message.includes('already exists')
        ) {
          const errors = this.formErrors();
          this.formErrors.set({
            ...errors,
            code: 'This warehouse code already exists. Please choose a different code.',
          });
          this.error.set(
            'Warehouse code already exists. Please fix the errors below.',
          );
        } else {
          this.error.set(
            err.error.message ||
              'Invalid warehouse data. Please check your input.',
          );
        }
      } else {
        this.error.set(
          'Invalid warehouse data. Please check your input and try again.',
        );
      }
    } else if (err.status === 409) {
      // Conflict - likely duplicate code
      const errors = this.formErrors();
      this.formErrors.set({
        ...errors,
        code: 'This warehouse code already exists. Please choose a different code.',
      });
      this.error.set(
        'Warehouse code already exists. Please fix the errors below.',
      );
    } else {
      this.error.set('Failed to create warehouse. Please try again.');
    }
  }

  /**
   * Handle validation errors from backend
   */
  private handleBackendValidationErrors(backendErrors: any): void {
    const errors = this.formErrors();
    const newErrors = { ...errors };

    // Parse backend validation errors
    if (Array.isArray(backendErrors)) {
      backendErrors.forEach((error: any) => {
        if (error.field && error.message) {
          newErrors[error.field] = error.message;
        }
      });
    } else if (typeof backendErrors === 'object') {
      Object.keys(backendErrors).forEach((field) => {
        if (backendErrors[field]) {
          newErrors[field] = Array.isArray(backendErrors[field])
            ? backendErrors[field][0]
            : backendErrors[field];
        }
      });
    }

    this.formErrors.set(newErrors);
    this.error.set('Please fix the validation errors below.');
  }

  /**
   * Override canExportData to use warehouse-specific permissions
   */
  override canExportData(): boolean {
    return this.canModifyWarehouses();
  }

  // Alias for backward compatibility with template
  exportWarehouses = () => this.exportData();

  protected readonly Object = Object;
}
