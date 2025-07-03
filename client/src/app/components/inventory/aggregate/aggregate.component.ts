import { Component, computed, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { InventoryService } from '../services/inventory.service';
import { AggregateReportData } from '../models/inventory.models';
import { catchError, of } from 'rxjs';

@Component({
  selector: 'app-aggregate',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  templateUrl: './aggregate.component.html',
  styleUrl: './aggregate.component.css',
})
export class AggregateComponent implements OnInit {
  // Use signals for reactive state management
  protected readonly aggregateData = signal<AggregateReportData | null>(null);

  // Computed values derived from aggregateData
  protected readonly stockLevels = computed(
    () => this.aggregateData()?.stockLevels || null,
  );
  protected readonly warehouseOverview = computed(
    () => this.aggregateData()?.warehouseOverview || [],
  );
  protected readonly inventoryValue = computed(
    () => this.aggregateData()?.inventoryValue || null,
  );
  protected readonly productSummary = computed(
    () => this.aggregateData()?.productSummary || [],
  );

  // Computed sorted values
  protected readonly sortedInventoryValueByWarehouse = computed(() => {
    const byWarehouse = this.inventoryValue()?.byWarehouse;
    if (!byWarehouse) return [];
    return Object.entries(byWarehouse).sort((a, b) => b[1] - a[1]);
  });

  protected readonly sortedLowStockByWarehouse = computed(() => {
    const lowStockByWarehouse = this.stockLevels()?.lowStockByWarehouse;
    if (!lowStockByWarehouse) return [];
    return Object.entries(lowStockByWarehouse).sort(
      (a, b) => b[1].length - a[1].length,
    );
  });

  protected readonly sortedHighStockByWarehouse = computed(() => {
    const highStockByWarehouse = this.stockLevels()?.highStockByWarehouse;
    if (!highStockByWarehouse) return [];
    return Object.entries(highStockByWarehouse).sort(
      (a, b) => b[1].length - a[1].length,
    );
  });

  constructor(private inventoryService: InventoryService) {}

  ngOnInit(): void {
    this.fetchAggregateData();
  }

  fetchAggregateData(): void {
    this.inventoryService
      .getAggregateReport()
      .pipe(
        catchError((err) => {
          console.error('Error fetching aggregate data:', err);

          return of(null);
        }),
      )
      .subscribe((data) => {
        if (data) {
          this.aggregateData.set(data);
        }
      });
  }

  // Helper method for accordion IDs to avoid template duplication
  protected getAccordionItemId(prefix: string, index: number): string {
    return `${prefix}${index}`;
  }
}
