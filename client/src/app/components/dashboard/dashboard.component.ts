import { Component, OnInit } from '@angular/core';
import { AuthService, User } from '../../auth/services/auth.service';
import { Router } from '@angular/router';
import { CommonModule, CurrencyPipe, NgIf } from '@angular/common';
import { ProductService } from '../../services/inventory/product.service';
import { InventoryService } from '../../services/inventory/inventory.service';
import {
  AggregateReportData,
  ProductAnalytics,
  StockItem,
} from '../inventory/models/inventory.models';
import { catchError, forkJoin, of } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  imports: [NgIf, CommonModule, CurrencyPipe],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  user: User | null = null;

  // Analytics data
  productAnalytics: ProductAnalytics | null = null;
  aggregateData: AggregateReportData | null = null;

  // Loading states
  isLoading = true;
  hasError = false;
  errorMessage = '';

  // UI properties
  currentTime = new Date();

  constructor(
    private authService: AuthService,
    private router: Router,
    private productService: ProductService,
    private inventoryService: InventoryService,
  ) {}

  ngOnInit(): void {
    this.user = this.authService.getUser();

    // If no user is logged in, redirect to login
    if (!this.user) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadDashboardData();
  }

  private loadDashboardData(): void {
    this.isLoading = true;
    this.hasError = false;

    forkJoin({
      productAnalytics: this.productService.getProductAnalytics(),
      aggregateData: this.inventoryService.getAggregateReport(),
    })
      .pipe(
        catchError((error) => {
          console.error('Error loading dashboard data:', error);
          this.hasError = true;
          this.errorMessage =
            'Failed to load dashboard data. Please try again.';
          return of({ productAnalytics: null, aggregateData: null });
        }),
      )
      .subscribe((data) => {
        this.productAnalytics = data.productAnalytics;
        this.aggregateData = data.aggregateData;
        this.isLoading = false;
      });
  }

  // Computed properties for dashboard insights
  get totalInventoryValue(): number {
    return this.aggregateData?.inventoryValue?.totalInventoryValue || 0;
  }

  get lowStockCount(): number {
    return this.aggregateData?.stockLevels?.totalLowStockProducts || 0;
  }

  get highStockCount(): number {
    return this.aggregateData?.stockLevels?.totalHighStockProducts || 0;
  }

  get warehouseCount(): number {
    return this.aggregateData?.warehouseOverview?.length || 0;
  }

  get topCategories(): Array<{ key: string; value: number }> {
    if (!this.productAnalytics?.categoryBreakdown) return [];
    return Object.entries(this.productAnalytics.categoryBreakdown)
      .map(([key, value]) => ({ key, value }))
      .sort((a, b) => b.value - a.value)
      .slice(0, 5);
  }

  get criticalStockItems(): StockItem[] {
    if (!this.aggregateData?.stockLevels) return [];
    const allLowStock: StockItem[] = [];
    Object.values(this.aggregateData.stockLevels.lowStockByWarehouse).forEach(
      (items) => {
        allLowStock.push(...items);
      },
    );
    return allLowStock.slice(0, 5); // Show top 5 critical items
  }

  get inventoryHealthScore(): number {
    if (!this.productAnalytics || !this.aggregateData) return 0;

    const totalProducts = this.productAnalytics.totalProducts;
    const activeProducts = this.productAnalytics.activeProducts;
    const lowStockProducts =
      this.aggregateData.stockLevels.totalLowStockProducts;

    if (totalProducts === 0) return 100;

    const activeRatio = activeProducts / totalProducts;
    const stockHealthRatio = Math.max(0, 1 - lowStockProducts / totalProducts);

    return Math.round((activeRatio * 0.6 + stockHealthRatio * 0.4) * 100);
  }

  refreshData(): void {
    this.currentTime = new Date();
    this.loadDashboardData();
  }

  navigateToProducts(): void {
    this.router.navigate(['/inventory/product']);
  }

  navigateToWarehouses(): void {
    this.router.navigate(['/inventory/warehouse']);
  }

  navigateToStock(): void {
    this.router.navigate(['/inventory/stock']);
  }

  navigateToReports(): void {
    this.router.navigate(['/inventory']);
  }
}
