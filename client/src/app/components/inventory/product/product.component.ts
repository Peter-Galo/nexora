import { Component } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../../services/inventory/product.service';
import { Product, ProductAnalytics } from '../models/inventory.models';
import { DataTableComponent } from '../../shared/data-table/data-table.component';
import { ExportStatusAlertComponent } from '../../shared/export-status-alert/export-status-alert.component';
import {
  PRODUCT_COLUMNS,
  PRODUCT_SUMMARY_SIMPLE_COLUMNS,
} from '../../shared/data-table/table-columns/model';
import { forkJoin } from 'rxjs';
import { ExportUtilityService } from '../../../services/inventory/export-utility.service';
import { BaseInventoryComponent } from '../base-inventory.component';

@Component({
  selector: 'app-product',
  standalone: true,
  imports: [
    CommonModule,
    CurrencyPipe,
    FormsModule,
    DataTableComponent,
    ExportStatusAlertComponent,
  ],
  templateUrl: './product.component.html',
})
export class ProductComponent extends BaseInventoryComponent {
  // Data properties
  allProducts: Product[] = [];
  filteredProducts: Product[] = [];
  analytics: ProductAnalytics | null = null;

  // Export category for base class
  protected exportCategory = 'PRODUCT' as const;

  // Filter properties
  searchTerm = '';
  selectedCategory = '';
  selectedBrand = '';
  selectedStatus = '';
  priceRangeMin: number | null = null;
  priceRangeMax: number | null = null;

  // UI properties
  showActiveOnly = false;
  showAnalytics = true;
  viewMode: 'table' | 'cards' = 'table';

  // Column definitions
  protected readonly productColumns = PRODUCT_COLUMNS;
  protected readonly productSummaryColumns = PRODUCT_SUMMARY_SIMPLE_COLUMNS;

  // Computed properties
  get categories(): string[] {
    return [...new Set(this.allProducts.map((p) => p.category))].sort();
  }

  get brands(): string[] {
    return [...new Set(this.allProducts.map((p) => p.brand))].sort();
  }

  get categoryBreakdownEntries(): Array<{ key: string; value: number }> {
    if (!this.analytics?.categoryBreakdown) return [];
    return Object.entries(this.analytics.categoryBreakdown)
      .map(([key, value]) => ({ key, value }))
      .sort((a, b) => b.value - a.value);
  }

  get brandBreakdownEntries(): Array<{ key: string; value: number }> {
    if (!this.analytics?.brandBreakdown) return [];
    return Object.entries(this.analytics.brandBreakdown)
      .map(([key, value]) => ({ key, value }))
      .sort((a, b) => b.value - a.value);
  }

  constructor(
    private productService: ProductService,
  ) {
    super();
  }

  protected loadData(): void {
    this.handleApiCall(
      () =>
        forkJoin({
          products: this.productService.getAllProducts(),
          analytics: this.productService.getProductAnalytics(),
        }),
      (data: { products: Product[]; analytics: ProductAnalytics }) => {
        this.allProducts = data.products;
        this.analytics = data.analytics;
        this.applyFilters();
      },
      'load product data',
    );
  }

  applyFilters(): void {
    let filtered = [...this.allProducts];

    // Search term filter
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase().trim();
      filtered = filtered.filter(
        (p) =>
          p.name.toLowerCase().includes(term) ||
          p.code.toLowerCase().includes(term) ||
          p.description.toLowerCase().includes(term) ||
          p.sku.toLowerCase().includes(term),
      );
    }

    // Category filter
    if (this.selectedCategory) {
      filtered = filtered.filter((p) => p.category === this.selectedCategory);
    }

    // Brand filter
    if (this.selectedBrand) {
      filtered = filtered.filter((p) => p.brand === this.selectedBrand);
    }

    // Status filter
    if (this.selectedStatus) {
      const isActive = this.selectedStatus === 'active';
      filtered = filtered.filter((p) => p.active === isActive);
    }

    // Show active only filter
    if (this.showActiveOnly) {
      filtered = filtered.filter((p) => p.active);
    }

    // Price range filter
    if (this.priceRangeMin !== null) {
      filtered = filtered.filter((p) => p.price >= this.priceRangeMin!);
    }
    if (this.priceRangeMax !== null) {
      filtered = filtered.filter((p) => p.price <= this.priceRangeMax!);
    }

    this.filteredProducts = filtered;
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedCategory = '';
    this.selectedBrand = '';
    this.selectedStatus = '';
    this.priceRangeMin = null;
    this.priceRangeMax = null;
    this.showActiveOnly = false;
    this.applyFilters();
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  onFilterChange(): void {
    this.applyFilters();
  }

  /**
   * Clear search and apply filters
   */
  clearSearch(): void {
    this.searchTerm = '';
    this.applyFilters();
  }

  toggleAnalytics(): void {
    this.showAnalytics = !this.showAnalytics;
  }

  toggleViewMode(): void {
    this.viewMode = this.viewMode === 'table' ? 'cards' : 'table';
  }

  // Alias for backward compatibility with template
  override refreshData = () => super.refreshData();
  exportProducts = () => this.exportData();
}
