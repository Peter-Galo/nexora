import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WarehouseService, WarehouseDTO } from '../services/warehouse.service';
import { DataTableComponent } from '../../shared/data-table/data-table.component';
import { WAREHOUSE_MANAGEMENT_COLUMNS } from '../../shared/data-table/table-columns/model';
import { catchError, of, Subject, debounceTime, distinctUntilChanged, switchMap, takeUntil } from 'rxjs';
import { AuthService } from '../../../auth/services/auth.service';

@Component({
  selector: 'app-warehouse',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    DataTableComponent,
  ],
  templateUrl: './warehouse.component.html',
  styleUrl: './warehouse.component.css'
})
export class WarehouseComponent implements OnInit, OnDestroy {
  // Data signals
  warehouses = signal<WarehouseDTO[]>([]);
  loading = signal<boolean>(false);
  error = signal<string | null>(null);

  // UI state
  showActiveOnly = signal<boolean>(false);
  searchTerm = signal<string>('');

  // Column definitions - dynamically filtered based on user permissions
  protected readonly warehouseColumns = computed(() => {
    const hasAnyPermissions = this.canModifyWarehouses() || this.canDeleteWarehouses();

    if (hasAnyPermissions) {
      // User has permissions, show all columns including actions
      return WAREHOUSE_MANAGEMENT_COLUMNS;
    } else {
      // User has no permissions, filter out the actions column
      return WAREHOUSE_MANAGEMENT_COLUMNS.filter(column => column.field !== 'actions');
    }
  });

  // Search debouncing
  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  constructor(
    private warehouseService: WarehouseService,
    private authService: AuthService
  ) {
    // Set up debounced search with 0.2 second delay
    this.searchSubject.pipe(
      debounceTime(200), // 0.2 second delay
      distinctUntilChanged(),
      switchMap(term => {
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
      takeUntil(this.destroy$)
    ).subscribe((data) => {
      this.warehouses.set(data);
      this.loading.set(false);
    });
  }

  ngOnInit(): void {
    this.loadWarehouses();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
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
   * Load warehouses based on current filters
   */
  loadWarehouses(): void {
    this.loading.set(true);
    this.error.set(null);

    const request = this.showActiveOnly()
      ? this.warehouseService.getActiveWarehouses()
      : this.warehouseService.getAllWarehouses();

    request.pipe(
      catchError((err) => {
        console.error('Error loading warehouses:', err);
        this.error.set('Failed to load warehouses. Please try again.');
        return of([]);
      })
    ).subscribe((data) => {
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

  /**
   * Clear search and reload all warehouses
   */
  clearSearch(): void {
    this.searchTerm.set('');
    this.onSearchInput('');
  }

  /**
   * Activate a warehouse
   */
  activateWarehouse(warehouse: WarehouseDTO): void {
    if (!warehouse.uuid) return;

    // Check permissions first
    if (!this.canModifyWarehouses()) {
      this.error.set('You do not have permission to activate warehouses. MANAGER or ADMIN role required.');
      return;
    }

    this.warehouseService.activateWarehouse(warehouse.uuid).pipe(
      catchError((err) => {
        console.error('Error activating warehouse:', err);
        if (err.status === 403) {
          this.error.set('Access denied. You do not have permission to activate warehouses. MANAGER or ADMIN role required.');
        } else {
          this.error.set('Failed to activate warehouse. Please try again.');
        }
        return of(null);
      })
    ).subscribe((updatedWarehouse) => {
      if (updatedWarehouse) {
        // Update the warehouse in the list
        const warehouses = this.warehouses();
        const index = warehouses.findIndex(w => w.uuid === warehouse.uuid);
        if (index !== -1) {
          warehouses[index] = updatedWarehouse;
          this.warehouses.set([...warehouses]);
        }
      }
    });
  }

  /**
   * Deactivate a warehouse
   */
  deactivateWarehouse(warehouse: WarehouseDTO): void {
    if (!warehouse.uuid) return;

    // Check permissions first
    if (!this.canModifyWarehouses()) {
      this.error.set('You do not have permission to deactivate warehouses. MANAGER or ADMIN role required.');
      return;
    }

    this.warehouseService.deactivateWarehouse(warehouse.uuid).pipe(
      catchError((err) => {
        console.error('Error deactivating warehouse:', err);
        if (err.status === 403) {
          this.error.set('Access denied. You do not have permission to deactivate warehouses. MANAGER or ADMIN role required.');
        } else {
          this.error.set('Failed to deactivate warehouse. Please try again.');
        }
        return of(null);
      })
    ).subscribe((updatedWarehouse) => {
      if (updatedWarehouse) {
        // Update the warehouse in the list
        const warehouses = this.warehouses();
        const index = warehouses.findIndex(w => w.uuid === warehouse.uuid);
        if (index !== -1) {
          warehouses[index] = updatedWarehouse;
          this.warehouses.set([...warehouses]);
        }
      }
    });
  }

  /**
   * Delete a warehouse
   */
  deleteWarehouse(warehouse: WarehouseDTO): void {
    if (!warehouse.uuid) return;

    // Check permissions first
    if (!this.canDeleteWarehouses()) {
      this.error.set('You do not have permission to delete warehouses. ADMIN role required.');
      return;
    }

    if (!confirm(`Are you sure you want to delete warehouse "${warehouse.name}"? This action cannot be undone.`)) {
      return;
    }

    this.warehouseService.deleteWarehouse(warehouse.uuid).pipe(
      catchError((err) => {
        console.error('Error deleting warehouse:', err);
        if (err.status === 403) {
          this.error.set('Access denied. You do not have permission to delete warehouses. ADMIN role required.');
        } else {
          this.error.set('Failed to delete warehouse. Please try again.');
        }
        return of(null);
      })
    ).subscribe(() => {
      // Remove the warehouse from the list
      const warehouses = this.warehouses();
      this.warehouses.set(warehouses.filter(w => w.uuid !== warehouse.uuid));
    });
  }

  /**
   * Refresh the warehouse list
   */
  refresh(): void {
    this.loadWarehouses();
  }
}
