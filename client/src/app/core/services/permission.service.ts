import { Injectable, inject, signal, computed } from '@angular/core';
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
  providedIn: 'root'
})
export class PermissionService {
  private authService = inject(AuthService);

  // Permission configuration by role
  private readonly rolePermissions: PermissionConfig = {
    'ADMIN': [
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
      'analytics.view'
    ],
    'MANAGER': [
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
      'analytics.view'
    ],
    'USER': [
      'warehouse.read',
      'product.read',
      'stock.read',
      'reports.view'
    ]
  };

  // Current user signal
  private readonly _currentUser = signal(this.authService.getUser());

  // Computed permissions
  readonly currentRole = computed(() => this._currentUser()?.role as UserRole);
  readonly userPermissions = computed(() => {
    const role = this.currentRole();
    return role ? this.rolePermissions[role] || [] : [];
  });

  readonly isAdmin = computed(() => this.currentRole() === 'ADMIN');
  readonly isManager = computed(() => this.currentRole() === 'MANAGER');
  readonly isUser = computed(() => this.currentRole() === 'USER');

  // Warehouse permissions
  readonly canCreateWarehouses = computed(() => this.hasPermission('warehouse.create'));
  readonly canReadWarehouses = computed(() => this.hasPermission('warehouse.read'));
  readonly canUpdateWarehouses = computed(() => this.hasPermission('warehouse.update'));
  readonly canDeleteWarehouses = computed(() => this.hasPermission('warehouse.delete'));
  readonly canModifyWarehouses = computed(() =>
    this.hasPermission('warehouse.update') || this.hasPermission('warehouse.activate') || this.hasPermission('warehouse.deactivate')
  );

  // Product permissions
  readonly canCreateProducts = computed(() => this.hasPermission('product.create'));
  readonly canReadProducts = computed(() => this.hasPermission('product.read'));
  readonly canUpdateProducts = computed(() => this.hasPermission('product.update'));
  readonly canDeleteProducts = computed(() => this.hasPermission('product.delete'));

  // Stock permissions
  readonly canCreateStock = computed(() => this.hasPermission('stock.create'));
  readonly canReadStock = computed(() => this.hasPermission('stock.read'));
  readonly canUpdateStock = computed(() => this.hasPermission('stock.update'));
  readonly canDeleteStock = computed(() => this.hasPermission('stock.delete'));
  readonly canModifyStock = computed(() => this.hasPermission('stock.modify'));

  // Export and reporting permissions
  readonly canExportData = computed(() => this.hasPermission('export.data'));
  readonly canViewReports = computed(() => this.hasPermission('reports.view'));
  readonly canViewAnalytics = computed(() => this.hasPermission('analytics.view'));

  constructor() {
    // Update user when auth state changes
    // This could be enhanced to listen to auth state changes
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
   * Check if user has any of the specified permissions
   */
  hasAnyPermission(permissions: Permission[]): boolean {
    return permissions.some(permission => this.hasPermission(permission));
  }

  /**
   * Check if user has all of the specified permissions
   */
  hasAllPermissions(permissions: Permission[]): boolean {
    return permissions.every(permission => this.hasPermission(permission));
  }

  /**
   * Get all permissions for current user
   */
  getAllPermissions(): Permission[] {
    return this.userPermissions();
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
      canDeactivate: this.hasPermission('warehouse.deactivate')
    };
  }

  /**
   * Check if user can perform product operations
   */
  getProductPermissions() {
    return {
      canCreate: this.canCreateProducts(),
      canRead: this.canReadProducts(),
      canUpdate: this.canUpdateProducts(),
      canDelete: this.canDeleteProducts()
    };
  }

  /**
   * Check if user can perform stock operations
   */
  getStockPermissions() {
    return {
      canCreate: this.canCreateStock(),
      canRead: this.canReadStock(),
      canUpdate: this.canUpdateStock(),
      canDelete: this.canDeleteStock(),
      canModify: this.canModifyStock()
    };
  }

  /**
   * Get filtered actions based on permissions
   */
  getAvailableActions(entityType: 'warehouse' | 'product' | 'stock'): string[] {
    const actions: string[] = [];

    switch (entityType) {
      case 'warehouse':
        const warehousePerms = this.getWarehousePermissions();
        if (warehousePerms.canCreate) actions.push('create');
        if (warehousePerms.canUpdate) actions.push('edit');
        if (warehousePerms.canDelete) actions.push('delete');
        if (warehousePerms.canActivate) actions.push('activate');
        if (warehousePerms.canDeactivate) actions.push('deactivate');
        break;

      case 'product':
        const productPerms = this.getProductPermissions();
        if (productPerms.canCreate) actions.push('create');
        if (productPerms.canUpdate) actions.push('edit');
        if (productPerms.canDelete) actions.push('delete');
        break;

      case 'stock':
        const stockPerms = this.getStockPermissions();
        if (stockPerms.canCreate) actions.push('create');
        if (stockPerms.canUpdate) actions.push('edit');
        if (stockPerms.canDelete) actions.push('delete');
        if (stockPerms.canModify) actions.push('adjust');
        break;
    }

    return actions;
  }

  /**
   * Check if user can access a specific route/feature
   */
  canAccessFeature(feature: string): boolean {
    const featurePermissions: { [key: string]: Permission[] } = {
      'warehouse-management': ['warehouse.read'],
      'product-management': ['product.read'],
      'stock-management': ['stock.read'],
      'analytics-dashboard': ['analytics.view'],
      'reports': ['reports.view'],
      'data-export': ['export.data']
    };

    const requiredPermissions = featurePermissions[feature];
    return requiredPermissions ? this.hasAnyPermission(requiredPermissions) : false;
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
      'warehouse.deactivate': 'You do not have permission to deactivate warehouses',
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
      'analytics.view': 'You do not have permission to view analytics'
    };

    return messages[permission] || 'You do not have permission to perform this action';
  }

  /**
   * Refresh current user data
   */
  refreshUser(): void {
    this._currentUser.set(this.authService.getUser());
  }

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    return !!this._currentUser();
  }

  /**
   * Get user display name
   */
  getUserDisplayName(): string {
    const user = this._currentUser();
    if (user?.firstName && user?.lastName) {
      return `${user.firstName} ${user.lastName}`;
    }
    return user?.email || 'Unknown User';
  }

  /**
   * Get role display name
   */
  getRoleDisplayName(): string {
    const role = this.currentRole();
    const roleNames = {
      'ADMIN': 'Administrator',
      'MANAGER': 'Manager',
      'USER': 'User'
    };
    return role ? roleNames[role] : 'Unknown Role';
  }
}
