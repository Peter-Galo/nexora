import { Component, computed, signal, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  WarehouseDTO,
  WarehouseService,
} from '../../../services/inventory/warehouse.service';
import { DataTableComponent } from '../../shared/data-table/data-table.component';
import { WAREHOUSE_MANAGEMENT_COLUMNS } from '../../shared/data-table/table-columns/model';
import { BaseInventoryComponent } from '../base-inventory.component';
import { FormValidationService } from '../../../core/services/form-validation.service';
import { PermissionService } from '../../../core/services/permission.service';
import { FilteringService } from '../../../core/services/filtering.service';

@Component({
  selector: 'app-warehouse',
  standalone: true,
  imports: [CommonModule, FormsModule, DataTableComponent],
  templateUrl: './warehouse.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WarehouseComponent extends BaseInventoryComponent {
  // Export category for base class
  protected exportCategory = 'WAREHOUSE' as const;

  // Inject services using modern Angular patterns
  private warehouseService = inject(WarehouseService);
  private formValidationService = inject(FormValidationService);
  private permissionService = inject(PermissionService);
  private filteringService = inject(FilteringService);

  // Form state using validation service
  formState = this.formValidationService.createFormState(
    {
      code: '',
      name: '',
      description: '',
      address: '',
      city: '',
      stateProvince: '',
      postalCode: '',
      country: '',
      active: true,
    } as Partial<WarehouseDTO>,
    FormValidationService.WAREHOUSE_VALIDATION
  );

  // Filter state using filtering service
  filterState = this.filteringService.createFilterState(
    [] as WarehouseDTO[],
    FilteringService.COMMON_CONFIGS.warehouse
  );

  // UI state
  showCreateForm = signal<boolean>(false);
  creating = signal<boolean>(false);

  // Computed properties using permission service
  readonly permissions = computed(() => this.permissionService.getWarehousePermissions());

  // Column definitions - dynamically filtered based on user permissions
  protected readonly warehouseColumns = computed(() => {
    const perms = this.permissions();
    if (perms.canModify || perms.canDelete) {
      return WAREHOUSE_MANAGEMENT_COLUMNS;
    } else {
      return WAREHOUSE_MANAGEMENT_COLUMNS.filter(column => column.field !== 'actions');
    }
  });

  constructor() {
    super();
  }

  /**
   * Implementation of abstract loadData method from BaseInventoryComponent
   */
  protected loadData(): void {
    this.handleApiCall(
      () => this.warehouseService.getAllWarehouses(),
      (warehouses: WarehouseDTO[]) => {
        this.filterState.updateData(warehouses);
      },
      'load warehouses'
    );
  }

  // Permission methods using permission service
  canModifyWarehouses = () => this.permissions().canModify;
  canDeleteWarehouses = () => this.permissions().canDelete;
  canCreateWarehouses = () => this.permissions().canCreate;

  /**
   * Handle search input changes
   */
  onSearchInput(searchTerm: string): void {
    this.filterState.setSearchTerm(searchTerm);
  }

  /**
   * Toggle active only filter
   */
  toggleActiveFilter(): void {
    this.filterState.toggleActiveOnly();
  }

  /**
   * Clear search term
   */
  clearSearch(): void {
    this.filterState.setSearchTerm('');
  }

  /**
   * Activate a warehouse
   */
  activateWarehouse(warehouse: WarehouseDTO): void {
    if (!this.canModifyWarehouses()) {
      this.handleError(
        { message: this.permissionService.getPermissionErrorMessage('warehouse.activate') },
        'activate warehouse'
      );
      return;
    }

    this.handleApiCall(
      () => this.warehouseService.activateWarehouse(warehouse.uuid!),
      (updatedWarehouse: WarehouseDTO) => {
        const currentData = this.filterState.allData();
        const updatedData = currentData.map(w =>
          w.uuid === warehouse.uuid ? updatedWarehouse : w
        );
        this.filterState.updateData(updatedData);
      },
      'activate warehouse'
    );
  }

  /**
   * Deactivate a warehouse
   */
  deactivateWarehouse(warehouse: WarehouseDTO): void {
    if (!this.canModifyWarehouses()) {
      this.handleError(
        { message: this.permissionService.getPermissionErrorMessage('warehouse.deactivate') },
        'deactivate warehouse'
      );
      return;
    }

    this.handleApiCall(
      () => this.warehouseService.deactivateWarehouse(warehouse.uuid!),
      (updatedWarehouse: WarehouseDTO) => {
        const currentData = this.filterState.allData();
        const updatedData = currentData.map(w =>
          w.uuid === warehouse.uuid ? updatedWarehouse : w
        );
        this.filterState.updateData(updatedData);
      },
      'deactivate warehouse'
    );
  }

  /**
   * Delete a warehouse
   */
  deleteWarehouse(warehouse: WarehouseDTO): void {
    if (!this.canDeleteWarehouses()) {
      this.handleError(
        { message: this.permissionService.getPermissionErrorMessage('warehouse.delete') },
        'delete warehouse'
      );
      return;
    }

    if (!confirm(`Are you sure you want to delete warehouse "${warehouse.name}"? This action cannot be undone.`)) {
      return;
    }

    this.handleApiCall(
      () => this.warehouseService.deleteWarehouse(warehouse.uuid!),
      () => {
        const currentData = this.filterState.allData();
        const updatedData = currentData.filter(w => w.uuid !== warehouse.uuid);
        this.filterState.updateData(updatedData);
      },
      'delete warehouse'
    );
  }

  /**
   * Refresh the warehouse list
   */
  refresh(): void {
    this.refreshData();
  }

  /**
   * Show the create warehouse form
   */
  showCreateWarehouseForm(): void {
    if (!this.canCreateWarehouses()) {
      this.handleError(
        { message: this.permissionService.getPermissionErrorMessage('warehouse.create') },
        'show create form'
      );
      return;
    }
    this.formState.reset();
    this.showCreateForm.set(true);
  }

  /**
   * Hide the create warehouse form
   */
  hideCreateWarehouseForm(): void {
    this.showCreateForm.set(false);
    this.formState.reset();
  }

  /**
   * Update form field value
   */
  updateFormField(field: string, value: any): void {
    this.formState.updateField(field as keyof WarehouseDTO, value);
  }

  /**
   * Get error message for a specific field
   */
  getFieldError(field: string): string | null {
    return this.formState.getFieldError(field as keyof WarehouseDTO);
  }

  /**
   * Check if a field has an error
   */
  hasFieldError(field: string): boolean {
    return this.formState.hasFieldError(field as keyof WarehouseDTO);
  }

  /**
   * Get current form data
   */
  createFormData() {
    return this.formState.data();
  }

  /**
   * Get field touched state
   */
  fieldTouched() {
    return this.formState.touched();
  }

  /**
   * Get form errors
   */
  formErrors() {
    return this.formState.errors();
  }

  /**
   * Make Object available in template
   */
  Object = Object;

  /**
   * Create a new warehouse
   */
  createWarehouse(): void {
    if (!this.canCreateWarehouses()) {
      this.handleError(
        { message: this.permissionService.getPermissionErrorMessage('warehouse.create') },
        'create warehouse'
      );
      return;
    }

    if (!this.formState.validateAll()) {
      this.handleError(
        { message: 'Please fix the validation errors below before submitting.' },
        'validate form'
      );
      return;
    }

    this.creating.set(true);

    this.handleApiCall(
      () => this.warehouseService.createWarehouse(this.formState.data() as WarehouseDTO),
      (newWarehouse: WarehouseDTO) => {
        const currentData = this.filterState.allData();
        this.filterState.updateData([newWarehouse, ...currentData]);
        this.hideCreateWarehouseForm();
        this.creating.set(false);
      },
      'create warehouse'
    );
  }

  /**
   * Override canExportData to use warehouse-specific permissions
   */
  override canExportData(): boolean {
    return this.permissionService.canExportData();
  }

  // Alias for backward compatibility with template
  exportWarehouses = () => this.exportData();
}
