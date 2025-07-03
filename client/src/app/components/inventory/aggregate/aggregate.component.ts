import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InventoryService } from '../services/inventory.service';
import {
  AggregateReportData,
  InventoryValue,
  ProductSummary,
  StockLevels,
  WarehouseOverview,
} from '../models/inventory.models';

@Component({
  selector: 'app-aggregate',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './aggregate.component.html',
  styleUrl: './aggregate.component.css',
})
export class AggregateComponent implements OnInit {
  loading = true;
  error = false;
  aggregateData: AggregateReportData | null = null;

  // Store individual sections of the aggregate data
  stockLevels: StockLevels | null = null;
  warehouseOverview: WarehouseOverview[] = [];
  inventoryValue: InventoryValue | null = null;
  productSummary: ProductSummary[] = [];

  constructor(private inventoryService: InventoryService) {}

  ngOnInit(): void {
    this.fetchAggregateData();
  }

  fetchAggregateData(): void {
    this.loading = true;
    this.error = false;

    this.inventoryService.getAggregateReport().subscribe({
      next: (data) => {
        this.aggregateData = data;

        // Extract individual sections
        this.stockLevels = data.stockLevels;
        this.warehouseOverview = data.warehouseOverview || [];
        this.inventoryValue = data.inventoryValue;
        this.productSummary = data.productSummary || [];

        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching aggregate data:', err);
        this.error = true;
        this.loading = false;
      },
    });
  }
}
