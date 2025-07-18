// src/app/components/inventory/aggregate/aggregate-data.service.ts
import { computed, Injectable, signal } from '@angular/core';
import { AggregateReportData } from '../../components/inventory/models/inventory.models';

@Injectable()
export class AggregateDataService {
  private readonly _aggregateData = signal<AggregateReportData | null>(null);

  // Public API to update data
  public updateData(data: AggregateReportData | null): void {
    this._aggregateData.set(data);
  }

  // Computed values derived from aggregateData
  public readonly stockLevels = computed(
    () => this._aggregateData()?.stockLevels || null,
  );

  public readonly warehouseOverview = computed(
    () => this._aggregateData()?.warehouseOverview || [],
  );

  public readonly inventoryValue = computed(
    () => this._aggregateData()?.inventoryValue || null,
  );

  public readonly productSummary = computed(
    () => this._aggregateData()?.productSummary || [],
  );

  // Sorted computed values
  public readonly sortedInventoryValueByWarehouse = computed(() => {
    const byWarehouse = this.inventoryValue()?.byWarehouse;
    if (!byWarehouse) return [];
    return Object.entries(byWarehouse).sort((a, b) => b[1] - a[1]);
  });

  public readonly sortedLowStockByWarehouse = computed(() => {
    const lowStockByWarehouse = this.stockLevels()?.lowStockByWarehouse;
    if (!lowStockByWarehouse) return [];
    return Object.entries(lowStockByWarehouse).sort(
      (a, b) => b[1].length - a[1].length,
    );
  });

  public readonly sortedHighStockByWarehouse = computed(() => {
    const highStockByWarehouse = this.stockLevels()?.highStockByWarehouse;
    if (!highStockByWarehouse) return [];
    return Object.entries(highStockByWarehouse).sort(
      (a, b) => b[1].length - a[1].length,
    );
  });

  // Helper method for generating accordion IDs
  public getAccordionItemId(prefix: string, index: number): string {
    return `${prefix}${index}`;
  }
}
