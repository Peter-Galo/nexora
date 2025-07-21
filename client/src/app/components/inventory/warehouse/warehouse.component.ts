import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  WarehouseEntity,
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
    } as Partial<WarehouseEntity>,
    FormValidationService.WAREHOUSE_VALIDATION,
  );

  filterState = this.filteringService.createFilterState(
    [] as WarehouseEntity[],
    FilteringService.COMMON_CONFIGS.warehouse,
  );

  // UI state
  showCreateForm = signal<boolean>(false);
  creating = signal<boolean>(false);

  // Computed properties using permission service
  readonly permissions = computed(() =>
    this.permissionService.getWarehousePermissions(),
  );

  // Column definitions - dynamically filtered based on user permissions
  protected readonly warehouseColumns = computed(() => {
    const perms = this.permissions();
    if (perms.canModify || perms.canDelete) {
      return WAREHOUSE_MANAGEMENT_COLUMNS;
    } else {
      return WAREHOUSE_MANAGEMENT_COLUMNS.filter(
        (column) => column.field !== 'actions',
      );
    }
  });

  // Action button configurations
  protected readonly actionButtonConfig = computed(() => {
    const canModify = this.canModifyWarehouses();
    const canDelete = this.canDeleteWarehouses();
    const hasActions = canModify || canDelete;

    return {
      hasActions,
      canModify,
      canDelete,
    };
  });

  // Get action button configuration for a specific warehouse
  getWarehouseActionConfig = (warehouse: WarehouseEntity) => {
    const config = this.actionButtonConfig();

    return {
      ...config,
      primaryAction: {
        visible: config.canModify,
        isActivate: !warehouse.active,
        text: warehouse.active ? 'Deactivate' : 'Activate',
        icon: warehouse.active ? 'fas fa-pause' : 'fas fa-play',
        title: warehouse.active ? 'Deactivate warehouse' : 'Activate warehouse',
        action: warehouse.active
          ? () => this.deactivateWarehouse(warehouse)
          : () => this.activateWarehouse(warehouse),
        class: `btn btn-sm px-3`,
      },
      deleteAction: {
        visible: config.canDelete,
        text: 'Delete',
        icon: 'fas fa-trash',
        title: 'Delete warehouse',
        action: () => this.deleteWarehouse(warehouse),
        class: `btn btn-sm px-3`,
      },
    };
  };

  constructor() {
    super();
  }

  protected loadData(): void {
    this.handleApiCall(
      () => this.warehouseService.getAllWarehouses(),
      (warehouses: WarehouseEntity[]) => {
        this.filterState.updateData(warehouses);
      },
      'load warehouses',
    );
  }

  // Permission methods using permission service
  canModifyWarehouses = () => this.permissions().canModify;
  canDeleteWarehouses = () => this.permissions().canDelete;
  canCreateWarehouses = () => this.permissions().canCreate;

  onSearchInput(searchTerm: string): void {
    this.filterState.setSearchTerm(searchTerm);
  }

  toggleActiveFilter(): void {
    this.filterState.toggleActiveOnly();
  }

  clearSearch(): void {
    this.filterState.setSearchTerm('');
  }

  activateWarehouse(warehouse: WarehouseEntity): void {
    if (!this.canModifyWarehouses()) {
      this.handleError(
        {
          message:
            this.permissionService.getPermissionErrorMessage(
              'warehouse.activate',
            ),
        },
        'activate warehouse',
      );
      return;
    }

    this.handleApiCall(
      () => this.warehouseService.activateWarehouse(warehouse.uuid!),
      (updatedWarehouse: WarehouseEntity) => {
        const currentData = this.filterState.allData();
        const updatedData = currentData.map((w) =>
          w.uuid === warehouse.uuid ? updatedWarehouse : w,
        );
        this.filterState.updateData(updatedData);
      },
      'activate warehouse',
    );
  }

  deactivateWarehouse(warehouse: WarehouseEntity): void {
    if (!this.canModifyWarehouses()) {
      this.handleError(
        {
          message: this.permissionService.getPermissionErrorMessage(
            'warehouse.deactivate',
          ),
        },
        'deactivate warehouse',
      );
      return;
    }

    this.handleApiCall(
      () => this.warehouseService.deactivateWarehouse(warehouse.uuid!),
      (updatedWarehouse: WarehouseEntity) => {
        const currentData = this.filterState.allData();
        const updatedData = currentData.map((w) =>
          w.uuid === warehouse.uuid ? updatedWarehouse : w,
        );
        this.filterState.updateData(updatedData);
      },
      'deactivate warehouse',
    );
  }

  deleteWarehouse(warehouse: WarehouseEntity): void {
    if (!this.canDeleteWarehouses()) {
      this.handleError(
        {
          message:
            this.permissionService.getPermissionErrorMessage(
              'warehouse.delete',
            ),
        },
        'delete warehouse',
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

    this.handleApiCall(
      () => this.warehouseService.deleteWarehouse(warehouse.uuid!),
      () => {
        const currentData = this.filterState.allData();
        const updatedData = currentData.filter(
          (w) => w.uuid !== warehouse.uuid,
        );
        this.filterState.updateData(updatedData);
      },
      'delete warehouse',
    );
  }

  refresh(): void {
    this.refreshData();
  }

  showCreateWarehouseForm(): void {
    if (!this.canCreateWarehouses()) {
      this.handleError(
        {
          message:
            this.permissionService.getPermissionErrorMessage(
              'warehouse.create',
            ),
        },
        'show create form',
      );
      return;
    }
    this.formState.reset();
    this.showCreateForm.set(true);
  }

  hideCreateWarehouseForm(): void {
    this.showCreateForm.set(false);
    this.formState.reset();
  }

  updateFormField(field: string, value: any): void {
    this.formState.updateField(field as keyof WarehouseEntity, value);
  }

  getFieldError(field: string): string | null {
    return this.formState.getFieldError(field as keyof WarehouseEntity);
  }

  hasFieldError(field: string): boolean {
    return this.formState.hasFieldError(field as keyof WarehouseEntity);
  }

  createFormData() {
    return this.formState.data();
  }

  fieldTouched() {
    return this.formState.touched();
  }

  createWarehouse(): void {
    if (!this.canCreateWarehouses()) {
      this.handleError(
        {
          message:
            this.permissionService.getPermissionErrorMessage(
              'warehouse.create',
            ),
        },
        'create warehouse',
      );
      return;
    }

    if (!this.formState.validateAll()) {
      this.handleError(
        {
          message: 'Please fix the validation errors below before submitting.',
        },
        'validate form',
      );
      return;
    }

    this.creating.set(true);

    this.handleApiCall(
      () =>
        this.warehouseService.createWarehouse(
          this.formState.data() as WarehouseEntity,
        ),
      (newWarehouse: WarehouseEntity) => {
        const currentData = this.filterState.allData();
        this.filterState.updateData([newWarehouse, ...currentData]);
        this.hideCreateWarehouseForm();
        this.creating.set(false);
      },
      'create warehouse',
    );
  }

  override canExportData(): boolean {
    return this.permissionService.canExportData();
  }
}
