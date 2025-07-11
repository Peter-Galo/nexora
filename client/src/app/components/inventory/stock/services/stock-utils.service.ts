import { Injectable } from '@angular/core';
import { StockDTO } from '../../../../services/inventory/stock.service';
import {
  StockMetrics,
  StockStatus,
  AlertConfig,
  AlertType,
  ViewType,
  MetricCard,
  QuickStat,
  TabConfig
} from '../models/stock.types';

/**
 * Stock Utilities Service
 * Provides utility methods for stock calculations, formatting, and status determination
 */
@Injectable({
  providedIn: 'root'
})
export class StockUtilsService {

  /**
   * Calculate comprehensive stock metrics
   */
  calculateStockMetrics(stocks: StockDTO[]): StockMetrics {
    const lowStocks = stocks.filter(s =>
      s.quantity > 0 && (s.lowStock || (s.minStockLevel && s.quantity <= s.minStockLevel))
    );

    const overStocks = stocks.filter(s =>
      s.overStock || (s.maxStockLevel && s.quantity >= s.maxStockLevel)
    );

    const zeroStocks = stocks.filter(s => s.quantity === 0);

    const totalValue = stocks.reduce(
      (sum, stock) => sum + stock.quantity * (stock.product?.price || 0),
      0
    );

    return {
      totalStocks: stocks.length,
      lowStockCount: lowStocks.length,
      overStockCount: overStocks.length,
      zeroStockCount: zeroStocks.length,
      totalValue,
      criticalAlerts: lowStocks.length + zeroStocks.length,
    };
  }

  /**
   * Categorize stocks by their status
   */
  categorizeStocks(stocks: StockDTO[]) {
    return {
      all: stocks,
      low: stocks.filter(s =>
        s.quantity > 0 && (s.lowStock || (s.minStockLevel && s.quantity <= s.minStockLevel))
      ),
      over: stocks.filter(s =>
        s.overStock || (s.maxStockLevel && s.quantity >= s.maxStockLevel)
      ),
      zero: stocks.filter(s => s.quantity === 0),
    };
  }

  /**
   * Generate metric cards for dashboard
   */
  generateMetricCards(metrics: StockMetrics, warehouseCount: number): MetricCard[] {
    return [
      {
        title: 'Total Stock Items',
        value: metrics.totalStocks,
        icon: 'fas fa-boxes fa-2x',
        iconClass: 'text-primary',
        subtitle: 'Active inventory',
        subtitleIcon: 'fas fa-chart-line',
        subtitleClass: 'text-success',
      },
      {
        title: 'Total Inventory Value',
        value: this.formatCurrency(metrics.totalValue),
        icon: 'fas fa-dollar-sign fa-2x',
        iconClass: 'text-success',
        subtitle: 'Estimated value',
        subtitleIcon: 'fas fa-calculator',
        subtitleClass: 'text-info',
      },
      {
        title: 'Critical Alerts',
        value: metrics.criticalAlerts,
        icon: 'fas fa-exclamation-triangle fa-2x',
        iconClass: 'text-warning',
        subtitle: 'Requires attention',
        subtitleIcon: 'fas fa-bell',
        subtitleClass: 'text-warning',
      },
      {
        title: 'Active Warehouses',
        value: warehouseCount,
        icon: 'fas fa-warehouse fa-2x',
        iconClass: 'text-info',
        subtitle: 'Locations',
        subtitleIcon: 'fas fa-map-marker-alt',
        subtitleClass: 'text-info',
      },
    ];
  }

  /**
   * Generate quick stats for dashboard
   */
  generateQuickStats(metrics: StockMetrics): QuickStat[] {
    return [
      {
        title: 'Out of Stock',
        count: metrics.zeroStockCount,
        view: 'zero',
        colorClass: 'text-danger',
        icon: 'fas fa-exclamation-triangle',
      },
      {
        title: 'Low Stock',
        count: metrics.lowStockCount,
        view: 'low',
        colorClass: 'text-warning',
        icon: 'fas fa-exclamation-circle',
      },
      {
        title: 'Overstock',
        count: metrics.overStockCount,
        view: 'over',
        colorClass: 'text-info',
        icon: 'fas fa-info-circle',
      },
    ];
  }

  /**
   * Get default tab configuration
   */
  getDefaultTabConfig(): TabConfig[] {
    return [
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
  }

  /**
   * Determine stock status with icon and class
   */
  getStockStatus(stock: StockDTO): StockStatus {
    if (stock.quantity === 0) {
      return { class: 'text-danger', icon: 'fas fa-exclamation-triangle' };
    }

    const isLow = stock.lowStock ||
      (stock.minStockLevel && stock.quantity <= stock.minStockLevel);
    const isOver = stock.overStock ||
      (stock.maxStockLevel && stock.quantity >= stock.maxStockLevel);

    if (isLow) return { class: 'text-warning', icon: 'fas fa-exclamation-circle' };
    if (isOver) return { class: 'text-info', icon: 'fas fa-info-circle' };
    return { class: 'text-success', icon: 'fas fa-check-circle' };
  }

  /**
   * Get stock badge class for UI display
   */
  getStockBadgeClass(stock: StockDTO): string {
    if (stock.quantity === 0) return 'bg-danger';

    const isLow = stock.lowStock ||
      (stock.minStockLevel && stock.quantity <= stock.minStockLevel && stock.quantity > 0);
    const isOver = stock.overStock ||
      (stock.maxStockLevel && stock.quantity >= stock.maxStockLevel);

    if (isLow) return 'bg-warning';
    if (isOver) return 'bg-info';
    return 'bg-success';
  }

  /**
   * Get alert configuration by type
   */
  getAlertConfig(type: AlertType): AlertConfig {
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

  /**
   * Format currency value
   */
  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(value);
  }

  /**
   * Format date with enhanced error handling
   */
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
    } catch {
      return 'Invalid Date';
    }
  }

  /**
   * Filter stocks based on search term
   */
  filterStocksBySearch(stocks: StockDTO[], searchTerm: string): StockDTO[] {
    if (!searchTerm.trim()) return stocks;

    const term = searchTerm.toLowerCase().trim();
    return stocks.filter(stock =>
      stock.product?.name?.toLowerCase().includes(term) ||
      stock.product?.code?.toLowerCase().includes(term) ||
      stock.product?.sku?.toLowerCase().includes(term) ||
      stock.warehouse?.name?.toLowerCase().includes(term) ||
      stock.warehouse?.code?.toLowerCase().includes(term)
    );
  }

  /**
   * Filter stocks by warehouse
   */
  filterStocksByWarehouse(stocks: StockDTO[], warehouseId: string): StockDTO[] {
    if (warehouseId === 'all') return stocks;
    return stocks.filter(stock => stock.warehouse?.uuid === warehouseId);
  }

  /**
   * Sort stocks by specified criteria
   */
  sortStocks(stocks: StockDTO[], sortBy: 'name' | 'quantity' | 'value' | 'status' = 'name'): StockDTO[] {
    return [...stocks].sort((a, b) => {
      switch (sortBy) {
        case 'name':
          return (a.product?.name || '').localeCompare(b.product?.name || '');
        case 'quantity':
          return b.quantity - a.quantity;
        case 'value':
          const aValue = a.quantity * (a.product?.price || 0);
          const bValue = b.quantity * (b.product?.price || 0);
          return bValue - aValue;
        case 'status':
          // Sort by status priority: zero -> low -> over -> normal
          const getStatusPriority = (stock: StockDTO) => {
            if (stock.quantity === 0) return 0;
            if (stock.lowStock || (stock.minStockLevel && stock.quantity <= stock.minStockLevel)) return 1;
            if (stock.overStock || (stock.maxStockLevel && stock.quantity >= stock.maxStockLevel)) return 2;
            return 3;
          };
          return getStatusPriority(a) - getStatusPriority(b);
        default:
          return 0;
      }
    });
  }
}
