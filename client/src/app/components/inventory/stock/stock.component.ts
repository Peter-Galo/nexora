import {
  Component,
  computed,
  inject,
  OnDestroy,
  OnInit,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StockDTO, StockService } from '../services/stock.service';
import { interval, Subscription } from 'rxjs';

interface StockMetrics {
  totalStocks: number;
  lowStockCount: number;
  overStockCount: number;
  zeroStockCount: number;
  totalValue: number;
  criticalAlerts: number;
}

interface StockAlert {
  type: 'critical' | 'warning' | 'info';
  message: string;
  count: number;
  action: string;
}

@Component({
  selector: 'app-stock',
  imports: [CommonModule, FormsModule],
  templateUrl: './stock.component.html',
  styleUrl: './stock.component.css',
})
export class StockComponent implements OnInit, OnDestroy {
  private stockService = inject(StockService);
  private autoRefreshSubscription?: Subscription;

  // Signals for reactive state management
  loading = signal(false);
  error = signal<string | null>(null);

  // Stock data signals
  allStocks = signal<StockDTO[]>([]);

  // UI state
  selectedView = signal<'dashboard' | 'all' | 'low' | 'over' | 'zero'>(
    'dashboard',
  );
  searchTerm = signal('');
  selectedWarehouse = signal<string>('all');
  autoRefresh = signal(true);

  // Computed categorized stocks
  lowStocks = computed(() =>
    this.allStocks().filter(
      (stock) =>
        stock.lowStock ||
        (stock.minStockLevel &&
          stock.quantity <= stock.minStockLevel &&
          stock.quantity > 0),
    ),
  );

  overStocks = computed(() =>
    this.allStocks().filter(
      (stock) =>
        stock.overStock ||
        (stock.maxStockLevel && stock.quantity >= stock.maxStockLevel),
    ),
  );

  zeroStocks = computed(() =>
    this.allStocks().filter((stock) => stock.quantity === 0),
  );

  // Computed metrics for business intelligence
  metrics = computed<StockMetrics>(() => {
    const all = this.allStocks();
    const low = this.lowStocks();
    const over = this.overStocks();
    const zero = this.zeroStocks();

    // Calculate total value using actual product prices
    const totalValue = all.reduce((sum, stock) => {
      const price = stock.product?.price || 0;
      return sum + stock.quantity * price;
    }, 0);

    return {
      totalStocks: all.length,
      lowStockCount: low.length,
      overStockCount: over.length,
      zeroStockCount: zero.length,
      totalValue,
      criticalAlerts: low.length + zero.length,
    };
  });

  // Business alerts computed from stock data
  alerts = computed<StockAlert[]>(() => {
    const alerts: StockAlert[] = [];
    const low = this.lowStocks();
    const zero = this.zeroStocks();
    const over = this.overStocks();

    if (zero.length > 0) {
      alerts.push({
        type: 'critical',
        message: 'Products are completely out of stock',
        count: zero.length,
        action: 'Reorder immediately',
      });
    }

    if (low.length > 0) {
      alerts.push({
        type: 'warning',
        message: 'Products are running low on stock',
        count: low.length,
        action: 'Plan reorder soon',
      });
    }

    if (over.length > 0) {
      alerts.push({
        type: 'info',
        message: 'Products have excess inventory',
        count: over.length,
        action: 'Consider promotions',
      });
    }

    return alerts;
  });

  // Filtered stocks based on search and warehouse selection
  filteredStocks = computed(() => {
    let stocks = this.getCurrentViewStocks();
    const search = this.searchTerm().toLowerCase();
    const warehouse = this.selectedWarehouse();

    if (search) {
      stocks = stocks.filter(
        (stock) =>
          stock.product?.name?.toLowerCase().includes(search) ||
          stock.product?.code?.toLowerCase().includes(search) ||
          stock.warehouse?.name?.toLowerCase().includes(search) ||
          // Fallback to computed fields for backward compatibility
          stock.productName?.toLowerCase().includes(search) ||
          stock.productCode?.toLowerCase().includes(search) ||
          stock.warehouseName?.toLowerCase().includes(search),
      );
    }

    if (warehouse !== 'all') {
      stocks = stocks.filter(
        (stock) =>
          stock.warehouse?.uuid === warehouse ||
          stock.warehouseUuid === warehouse,
      );
    }

    return stocks;
  });

  // Get unique warehouses for filter dropdown
  warehouses = computed(() => {
    const all = this.allStocks();
    const unique = new Map();
    all.forEach((stock) => {
      const warehouse = stock.warehouse;
      if (warehouse && !unique.has(warehouse.uuid)) {
        unique.set(warehouse.uuid, {
          uuid: warehouse.uuid,
          name: warehouse.name,
          code: warehouse.code,
        });
      }
    });
    return Array.from(unique.values());
  });

  ngOnInit() {
    this.loadAllStockData();

    // Auto-refresh every 30 seconds if enabled
    this.autoRefreshSubscription = interval(30000).subscribe(() => {
      if (this.autoRefresh()) {
        this.loadAllStockData();
      }
    });
  }

  ngOnDestroy() {
    // Clean up subscription to prevent memory leaks
    if (this.autoRefreshSubscription) {
      this.autoRefreshSubscription.unsubscribe();
    }
  }

  private loadAllStockData() {
    this.loading.set(true);
    this.error.set(null);

    // Load all stock data and compute categories locally for better performance
    this.stockService.getAllStocks().subscribe({
      next: (stocks) => {
        try {
          // Transform and validate data to ensure backward compatibility
          const transformedStocks = stocks
            .map((stock) => {
              // Validate required fields
              if (!stock.product || !stock.warehouse) {
                console.warn('Invalid stock data:', stock);
                return null;
              }

              return {
                ...stock,
                // Computed fields for backward compatibility
                productUuid: stock.product.uuid || '',
                productCode: stock.product.code || '',
                productName: stock.product.name || '',
                warehouseUuid: stock.warehouse.uuid || '',
                warehouseName: stock.warehouse.name || '',
                warehouseCode: stock.warehouse.code || '',
                lastUpdated: stock.updatedAt || stock.createdAt,
              };
            })
            .filter(Boolean) as StockDTO[]; // Remove null entries

          this.allStocks.set(transformedStocks);
          this.loading.set(false);

          // Log success for debugging
          console.log(
            `Successfully loaded ${transformedStocks.length} stock items`,
          );
        } catch (transformError) {
          console.error('Error transforming stock data:', transformError);
          this.error.set(
            'Failed to process stock data. Please refresh the page.',
          );
          this.loading.set(false);
        }
      },
      error: (error) => {
        const errorMessage =
          error?.error?.message || error?.message || 'Unknown error occurred';
        this.error.set(`Failed to load stock data: ${errorMessage}`);
        this.loading.set(false);
        console.error('Error loading stock data:', error);
      },
    });
  }

  private getCurrentViewStocks(): StockDTO[] {
    switch (this.selectedView()) {
      case 'all':
        return this.allStocks();
      case 'low':
        return this.lowStocks();
      case 'over':
        return this.overStocks();
      case 'zero':
        return this.zeroStocks();
      default:
        return this.allStocks();
    }
  }

  // UI Actions
  setView(view: 'dashboard' | 'all' | 'low' | 'over' | 'zero') {
    this.selectedView.set(view);
  }

  refresh() {
    this.loadAllStockData();
  }

  toggleAutoRefresh() {
    this.autoRefresh.set(!this.autoRefresh());
  }

  // Stock management actions
  addStock(stockId: string, quantity: number) {
    this.stockService.addStock(stockId, quantity).subscribe({
      next: () => {
        this.loadAllStockData(); // Refresh data
      },
      error: (error) => {
        this.error.set('Failed to add stock. Please try again.');
        console.error('Error adding stock:', error);
      },
    });
  }

  removeStock(stockId: string, quantity: number) {
    this.stockService.removeStock(stockId, quantity).subscribe({
      next: () => {
        this.loadAllStockData(); // Refresh data
      },
      error: (error) => {
        this.error.set('Failed to remove stock. Please try again.');
        console.error('Error removing stock:', error);
      },
    });
  }

  // Utility methods
  getStockStatusClass(stock: StockDTO): string {
    if (stock.quantity === 0) return 'text-danger';

    // Use computed fields from backend if available, otherwise fallback to manual calculation
    if (
      stock.lowStock ||
      (stock.minStockLevel &&
        stock.quantity <= stock.minStockLevel &&
        stock.quantity > 0)
    ) {
      return 'text-warning';
    }

    if (
      stock.overStock ||
      (stock.maxStockLevel && stock.quantity >= stock.maxStockLevel)
    ) {
      return 'text-info';
    }

    return 'text-success';
  }

  getStockStatusIcon(stock: StockDTO): string {
    if (stock.quantity === 0) return 'fas fa-exclamation-triangle';

    // Use computed fields from backend if available, otherwise fallback to manual calculation
    if (
      stock.lowStock ||
      (stock.minStockLevel &&
        stock.quantity <= stock.minStockLevel &&
        stock.quantity > 0)
    ) {
      return 'fas fa-exclamation-circle';
    }

    if (
      stock.overStock ||
      (stock.maxStockLevel && stock.quantity >= stock.maxStockLevel)
    ) {
      return 'fas fa-info-circle';
    }

    return 'fas fa-check-circle';
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(value);
  }

  formatDate(dateString: string): string {
    if (!dateString) return 'N/A';

    try {
      return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch (error) {
      console.warn('Invalid date string:', dateString);
      return 'Invalid Date';
    }
  }

  // TrackBy function for better performance in ngFor loops
  trackByStockId(index: number, stock: StockDTO): string {
    return stock.uuid;
  }

  // TrackBy function for alerts
  trackByAlertType(index: number, alert: StockAlert): string {
    return alert.type;
  }

  // TrackBy function for warehouses
  trackByWarehouseId(index: number, warehouse: any): string {
    return warehouse.uuid;
  }
}
