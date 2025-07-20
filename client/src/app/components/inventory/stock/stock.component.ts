import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  StockDTO,
  StockService,
} from '../../../services/inventory/stock.service';
import { interval, Subscription } from 'rxjs';
import { BaseInventoryComponent } from '../base-inventory.component';
import { DataTableComponent, TableColumn } from '../../shared/data-table/data-table.component';

type ViewType = 'dashboard' | 'all' | 'low' | 'over' | 'zero';
type AlertType = 'danger' | 'warning' | 'info';

interface StockMetrics {
  totalStocks: number;
  lowStockCount: number;
  overStockCount: number;
  zeroStockCount: number;
  totalValue: number;
  criticalAlerts: number;
}

interface StockAlert {
  type: AlertType;
  message: string;
  count: number;
  action: string;
}

interface TabConfig {
  key: ViewType;
  label: string;
  icon: string;
  badgeKey?: keyof StockMetrics;
  badgeClass?: string;
}

interface MetricCard {
  title: string;
  value: string | number;
  icon: string;
  iconClass: string;
  subtitle: string;
  subtitleIcon: string;
  subtitleClass: string;
}

interface QuickStat {
  title: string;
  count: number;
  description: string;
  buttonText: string;
  view: ViewType;
  colorClass: string;
  icon: string;
}

@Component({
  selector: 'app-stock',
  imports: [CommonModule, FormsModule, DataTableComponent],
  templateUrl: './stock.component.html',
})
export class StockComponent extends BaseInventoryComponent {
  // Export category for base class
  protected exportCategory = 'STOCK' as const;

  private stockService = inject(StockService);
  private autoRefreshSubscription?: Subscription;

  // Signals
  allStocks = signal<StockDTO[]>([]);
  selectedView = signal<ViewType>('dashboard');
  searchTerm = signal('');
  selectedWarehouse = signal<string>('all');
  autoRefresh = signal(true);

  constructor() {
    super();
  }

  // Configuration data
  tabs: TabConfig[] = [
    { key: 'dashboard', label: 'Dashboard', icon: 'fas fa-tachometer-alt' },
    {
      key: 'all',
      label: 'All Stocks',
      icon: 'fas fa-list',
      badgeKey: 'totalStocks',
      badgeClass: 'bg-secondary',
    },
    {
      key: 'low',
      label: 'Low Stock',
      icon: 'fas fa-exclamation-circle text-warning',
      badgeKey: 'lowStockCount',
      badgeClass: 'bg-warning',
    },
    {
      key: 'zero',
      label: 'Out of Stock',
      icon: 'fas fa-exclamation-triangle text-danger',
      badgeKey: 'zeroStockCount',
      badgeClass: 'bg-danger',
    },
    {
      key: 'over',
      label: 'Overstock',
      icon: 'fas fa-info-circle text-info',
      badgeKey: 'overStockCount',
      badgeClass: 'bg-info',
    },
  ];

  // Column definitions for data table
  stockColumns: TableColumn[] = [
    { header: 'Status', field: 'status', customTemplate: true },
    { header: 'Product', field: 'product', customTemplate: true },
    { header: 'Warehouse', field: 'warehouse', customTemplate: true },
    { header: 'Current Stock', field: 'currentStock', customTemplate: true },
    { header: 'Stock Levels', field: 'stockLevels', customTemplate: true },
    { header: 'Last Updated', field: 'lastUpdated', customTemplate: true },
    { header: 'Actions', field: 'actions', customTemplate: true },
  ];

  // Computed properties
  stockCategories = computed(() => {
    const all = this.allStocks();
    return {
      all,
      low: all.filter(
        (s) =>
          s.quantity > 0 &&
          (s.lowStock || (s.minStockLevel && s.quantity <= s.minStockLevel)),
      ),
      over: all.filter(
        (s) =>
          s.overStock || (s.maxStockLevel && s.quantity >= s.maxStockLevel),
      ),
      zero: all.filter((s) => s.quantity === 0),
    };
  });

  metrics = computed<StockMetrics>(() => {
    const { all, low, over, zero } = this.stockCategories();
    const totalValue = all.reduce(
      (sum, stock) => sum + stock.quantity * (stock.product?.price || 0),
      0,
    );

    return {
      totalStocks: all.length,
      lowStockCount: low.length,
      overStockCount: over.length,
      zeroStockCount: zero.length,
      totalValue,
      criticalAlerts: low.length + zero.length,
    };
  });

  metricCards = computed<MetricCard[]>(() => {
    const m = this.metrics();
    return [
      {
        title: 'Total Stock Items',
        value: m.totalStocks,
        icon: 'fas fa-boxes fa-2x',
        iconClass: 'text-primary',
        subtitle: 'Active inventory',
        subtitleIcon: 'fas fa-chart-line',
        subtitleClass: 'text-success',
      },
      {
        title: 'Total Inventory Value',
        value: this.formatCurrency(m.totalValue),
        icon: '',
        iconClass: 'text-success',
        subtitle: 'Estimated value',
        subtitleIcon: 'fas fa-calculator',
        subtitleClass: 'text-info',
      },
      {
        title: 'Critical Alerts',
        value: m.criticalAlerts,
        icon: 'fas fa-exclamation-triangle fa-2x',
        iconClass: 'text-warning',
        subtitle: 'Requires attention',
        subtitleIcon: 'fas fa-bell',
        subtitleClass: 'text-warning',
      },
      {
        title: 'Active Warehouses',
        value: this.warehouses().length,
        icon: 'fas fa-warehouse fa-2x',
        iconClass: 'text-info',
        subtitle: 'Locations',
        subtitleIcon: 'fas fa-map-marker-alt',
        subtitleClass: 'text-info',
      },
    ];
  });

  quickStats = computed<QuickStat[]>(() => {
    const m = this.metrics();
    return [
      {
        title: 'Out of Stock Items',
        count: m.zeroStockCount,
        description: 'Items requiring immediate reorder',
        buttonText: 'View Critical Items',
        view: 'zero',
        colorClass: 'danger',
        icon: 'fas fa-exclamation-triangle',
      },
      {
        title: 'Low Stock Items',
        count: m.lowStockCount,
        description: 'Items below minimum threshold',
        buttonText: 'Plan Reorders',
        view: 'low',
        colorClass: 'warning',
        icon: 'fas fa-exclamation-circle',
      },
      {
        title: 'Overstock Items',
        count: m.overStockCount,
        description: 'Items above maximum threshold',
        buttonText: 'Consider Promotions',
        view: 'over',
        colorClass: 'info',
        icon: 'fas fa-info-circle',
      },
    ];
  });

  alerts = computed<StockAlert[]>(() => {
    const { low, zero, over } = this.stockCategories();
    const alerts: StockAlert[] = [];

    if (zero.length > 0) {
      alerts.push({
        type: 'danger',
        message: `${zero.length === 1 ? 'Product is' : 'Products are'} completely out of stock`,
        count: zero.length,
        action: 'Reorder immediately',
      });
    }

    if (low.length > 0) {
      alerts.push({
        type: 'warning',
        message: `${low.length === 1 ? 'Product is' : 'Products are'} running low on stock`,
        count: low.length,
        action: 'Plan reorder soon',
      });
    }

    if (over.length > 0) {
      alerts.push({
        type: 'info',
        message: `${over.length === 1 ? 'Product has' : 'Products have'} excess inventory`,
        count: over.length,
        action: 'Consider promotions',
      });
    }

    return alerts;
  });

  filteredStocks = computed(() => {
    let stocks = this.getCurrentViewStocks();
    const search = this.searchTerm().toLowerCase();
    const warehouse = this.selectedWarehouse();

    if (search) {
      stocks = stocks.filter((stock) =>
        [
          stock.product?.name,
          stock.product?.code,
          stock.warehouse?.name,
          stock.productName,
          stock.productCode,
          stock.warehouseName,
        ].some((field) => field?.toLowerCase().includes(search)),
      );
    }

    if (warehouse !== 'all') {
      stocks = stocks.filter(
        (stock) =>
          stock.warehouse?.uuid === warehouse ||
          stock.warehouseUuid === warehouse,
      );
    }

    return stocks.sort((a, b) => {
      const quantityDiff = b.quantity - a.quantity;
      if (quantityDiff !== 0) return quantityDiff;

      const nameA = (a.product?.name || a.productName || '').toLowerCase();
      const nameB = (b.product?.name || b.productName || '').toLowerCase();
      return nameA.localeCompare(nameB);
    });
  });

  warehouses = computed(() => {
    const unique = new Map();
    this.allStocks().forEach((stock) => {
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

  override ngOnInit() {
    super.ngOnInit();
    this.autoRefreshSubscription = interval(30000).subscribe(() => {
      if (this.autoRefresh()) this.loadStockData();
    });
  }

  override ngOnDestroy() {
    this.autoRefreshSubscription?.unsubscribe();
    super.ngOnDestroy();
  }

  /**
   * Implementation of abstract loadData method from BaseInventoryComponent
   */
  protected loadData(): void {
    this.loadStockData();
  }

  private loadStockData() {
    this.loading.set(true);
    this.error.set(null);

    this.stockService.getAllStocks().subscribe({
      next: (stocks) => {
        try {
          const transformedStocks = stocks
            .filter((stock) => stock.product && stock.warehouse)
            .map((stock) => ({
              ...stock,
              productUuid: stock.product.uuid || '',
              productCode: stock.product.code || '',
              productName: stock.product.name || '',
              warehouseUuid: stock.warehouse.uuid || '',
              warehouseName: stock.warehouse.name || '',
              warehouseCode: stock.warehouse.code || '',
              lastUpdated: stock.updatedAt || stock.createdAt,
            }));

          this.allStocks.set(transformedStocks);
          this.loading.set(false);
          console.log(
            `Successfully loaded ${transformedStocks.length} stock items`,
          );
        } catch (error: any) {
          console.error('Error transforming stock data:', error);
          this.error.set(
            'Failed to process stock data. Please refresh the page.',
          );
          this.loading.set(false);
        }
      },
      error: (error: any) => {
        const errorMessage =
          error?.error?.message || error?.message || 'Unknown error occurred';
        this.error.set(`Failed to load stock data: ${errorMessage}`);
        this.loading.set(false);
        console.error('Error loading stock data:', error);
      },
    });
  }

  private getCurrentViewStocks(): StockDTO[] {
    const { all, low, over, zero } = this.stockCategories();
    const viewMap = { all, low, over, zero, dashboard: all };
    return viewMap[this.selectedView()] || all;
  }

  // UI Actions
  setView(view: ViewType) {
    this.selectedView.set(view);
  }

  refresh() {
    this.loadStockData();
  }

  toggleAutoRefresh() {
    this.autoRefresh.set(!this.autoRefresh());
  }

  // Stock management actions
  private handleStockAction(
    action: (id: string, qty: number) => any,
    stockId: string,
    quantity: number,
    actionName: string,
  ) {
    action.call(this.stockService, stockId, quantity).subscribe({
      next: () => this.loadStockData(),
      error: (error: any) => {
        this.error.set(`Failed to ${actionName} stock. Please try again.`);
        console.error(`Error ${actionName} stock:`, error);
      },
    });
  }

  addStock(stockId: string, quantity: number) {
    this.handleStockAction(
      this.stockService.addStock,
      stockId,
      quantity,
      'add',
    );
  }

  removeStock(stockId: string, quantity: number) {
    this.handleStockAction(
      this.stockService.removeStock,
      stockId,
      quantity,
      'remove',
    );
  }

  // Utility methods
  getStockStatus(stock: StockDTO): { class: string; icon: string } {
    if (stock.quantity === 0) {
      return { class: 'text-danger', icon: 'fas fa-exclamation-triangle' };
    }

    const isLow =
      stock.lowStock ||
      (stock.minStockLevel && stock.quantity <= stock.minStockLevel);
    const isOver =
      stock.overStock ||
      (stock.maxStockLevel && stock.quantity >= stock.maxStockLevel);

    if (isLow)
      return { class: 'text-warning', icon: 'fas fa-exclamation-circle' };
    if (isOver) return { class: 'text-info', icon: 'fas fa-info-circle' };
    return { class: 'text-success', icon: 'fas fa-check-circle' };
  }

  getStockStatusClass(stock: StockDTO): string {
    return this.getStockStatus(stock).class;
  }

  getStockStatusIcon(stock: StockDTO): string {
    return this.getStockStatus(stock).icon;
  }

  getStockBadgeClass(stock: StockDTO): string {
    if (stock.quantity === 0) return 'bg-danger';

    const isLow =
      stock.lowStock ||
      (stock.minStockLevel &&
        stock.quantity <= stock.minStockLevel &&
        stock.quantity > 0);
    const isOver =
      stock.overStock ||
      (stock.maxStockLevel && stock.quantity >= stock.maxStockLevel);

    if (isLow) return 'bg-warning';
    if (isOver) return 'bg-info';
    return 'bg-success';
  }

  getAlertConfig(type: AlertType): {
    icon: string;
    buttonClass: string;
    targetView: ViewType;
  } {
    const configs = {
      danger: {
        icon: 'fas fa-exclamation-triangle',
        buttonClass: 'btn-danger',
        targetView: 'zero' as ViewType,
      },
      warning: {
        icon: 'fas fa-exclamation-circle',
        buttonClass: 'btn-warning',
        targetView: 'low' as ViewType,
      },
      info: {
        icon: 'fas fa-info-circle',
        buttonClass: 'btn-info',
        targetView: 'over' as ViewType,
      },
    };
    return configs[type];
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(value);
  }

  override formatDate(dateString: string): string {
    if (!dateString) return 'N/A';
    try {
      return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return 'Invalid Date';
    }
  }

  // Alias for backward compatibility with template
  exportStocks = () => this.exportData();
}
