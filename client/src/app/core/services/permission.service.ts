import { computed, inject, Injectable, signal } from '@angular/core';
import { AuthService } from '../../auth/services/auth.service';

export type UserRole = 'ADMIN' | 'MANAGER' | 'USER';
export type Permission =
  | 'warehouse.create'
  | 'warehouse.read'
  | 'warehouse.update'
  | 'warehouse.delete'
  | 'warehouse.activate'
  | 'warehouse.deactivate'
  | 'product.create'
  | 'product.read'
  | 'product.update'
  | 'product.delete'
  | 'stock.create'
  | 'stock.read'
  | 'stock.update'
  | 'stock.delete'
  | 'stock.modify'
  | 'export.data'
  | 'reports.view'
  | 'analytics.view';

export interface PermissionConfig {
  [key: string]: Permission[];
}

/**
 * Modern Permission Service using Angular Signals
 * Centralizes authorization logic and provides reactive permission checking
 */
@Injectable({
  providedIn: 'root',
})
export class PermissionService {
  private authService = inject(AuthService);

  // Permission configuration by role
  private readonly rolePermissions: PermissionConfig = {
    ADMIN: [
      'warehouse.create',
      'warehouse.read',
      'warehouse.update',
      'warehouse.delete',
      'warehouse.activate',
      'warehouse.deactivate',
      'product.create',
      'product.read',
      'product.update',
      'product.delete',
      'stock.create',
      'stock.read',
      'stock.update',
      'stock.delete',
      'stock.modify',
      'export.data',
      'reports.view',
      'analytics.view',
    ],
    MANAGER: [
      'warehouse.read',
      'warehouse.update',
      'warehouse.activate',
      'warehouse.deactivate',
      'product.read',
      'product.update',
      'stock.read',
      'stock.update',
      'stock.modify',
      'export.data',
      'reports.view',
      'analytics.view',
    ],
    USER: ['warehouse.read', 'product.read', 'stock.read', 'reports.view'],
  };

  // Current user signal
  private readonly _currentUser = signal(this.authService.getUser());

  // Computed permissions
  readonly currentRole = computed(() => this._currentUser()?.role as UserRole);
  readonly userPermissions = computed(() => {
    const role = this.currentRole();
    return role ? this.rolePermissions[role] || [] : [];
  });

  // Warehouse permissions
  readonly canCreateWarehouses = computed(() =>
    this.hasPermission('warehouse.create'),
  );
  readonly canReadWarehouses = computed(() =>
    this.hasPermission('warehouse.read'),
  );
  readonly canUpdateWarehouses = computed(() =>
    this.hasPermission('warehouse.update'),
  );
  readonly canDeleteWarehouses = computed(() =>
    this.hasPermission('warehouse.delete'),
  );
  readonly canModifyWarehouses = computed(
    () =>
      this.hasPermission('warehouse.update') ||
      this.hasPermission('warehouse.activate') ||
      this.hasPermission('warehouse.deactivate'),
  );

  // Export and reporting permissions
  readonly canExportData = computed(() => this.hasPermission('export.data'));

  constructor() {
    this.refreshUser();
  }

  /**
   * Check if user has a specific permission
   */
  hasPermission(permission: Permission): boolean {
    const permissions = this.userPermissions();
    return permissions.includes(permission);
  }

  /**
   * Check if user can perform warehouse operations
   */
  getWarehousePermissions() {
    return {
      canCreate: this.canCreateWarehouses(),
      canRead: this.canReadWarehouses(),
      canUpdate: this.canUpdateWarehouses(),
      canDelete: this.canDeleteWarehouses(),
      canModify: this.canModifyWarehouses(),
      canActivate: this.hasPermission('warehouse.activate'),
      canDeactivate: this.hasPermission('warehouse.deactivate'),
    };
  }

  /**
   * Get permission-based error messages
   */
  getPermissionErrorMessage(permission: Permission): string {
    const messages: { [key in Permission]: string } = {
      'warehouse.create': 'You do not have permission to create warehouses',
      'warehouse.read': 'You do not have permission to view warehouses',
      'warehouse.update': 'You do not have permission to update warehouses',
      'warehouse.delete': 'You do not have permission to delete warehouses',
      'warehouse.activate': 'You do not have permission to activate warehouses',
      'warehouse.deactivate':
        'You do not have permission to deactivate warehouses',
      'product.create': 'You do not have permission to create products',
      'product.read': 'You do not have permission to view products',
      'product.update': 'You do not have permission to update products',
      'product.delete': 'You do not have permission to delete products',
      'stock.create': 'You do not have permission to create stock entries',
      'stock.read': 'You do not have permission to view stock',
      'stock.update': 'You do not have permission to update stock',
      'stock.delete': 'You do not have permission to delete stock',
      'stock.modify': 'You do not have permission to modify stock levels',
      'export.data': 'You do not have permission to export data',
      'reports.view': 'You do not have permission to view reports',
      'analytics.view': 'You do not have permission to view analytics',
    };

    return (
      messages[permission] ||
      'You do not have permission to perform this action'
    );
  }

  refreshUser(): void {
    this._currentUser.set(this.authService.getUser());
  }
}
