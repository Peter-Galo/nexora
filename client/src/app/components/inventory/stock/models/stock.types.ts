/**
 * Stock Component Types and Interfaces
 * Extracted from stock component for better maintainability and reusability
 */

export type ViewType = 'dashboard' | 'all' | 'low' | 'over' | 'zero';
export type AlertType = 'danger' | 'warning' | 'info';

export interface StockMetrics {
  totalStocks: number;
  lowStockCount: number;
  overStockCount: number;
  zeroStockCount: number;
  totalValue: number;
  criticalAlerts: number;
}

export interface StockAlert {
  type: AlertType;
  count: number;
  message: string;
  actionText: string;
}

export interface TabConfig {
  key: ViewType;
  label: string;
  icon: string;
  badgeKey?: keyof StockMetrics;
  badgeClass?: string;
}

export interface MetricCard {
  title: string;
  value: string | number;
  icon: string;
  iconClass: string;
  subtitle: string;
  subtitleIcon: string;
  subtitleClass: string;
}

export interface QuickStat {
  title: string;
  count: number;
  view: ViewType;
  colorClass: string;
  icon: string;
}

export interface StockStatus {
  class: string;
  icon: string;
}

export interface AlertConfig {
  icon: string;
  buttonClass: string;
  targetView: ViewType;
}

/**
 * Stock filter configuration
 */
export interface StockFilters {
  searchTerm: string;
  selectedWarehouse: string;
  selectedView: ViewType;
  showActiveOnly: boolean;
}

/**
 * Stock action configuration
 */
export interface StockActionConfig {
  type: 'add' | 'remove' | 'adjust';
  label: string;
  icon: string;
  buttonClass: string;
  confirmMessage?: string;
}
