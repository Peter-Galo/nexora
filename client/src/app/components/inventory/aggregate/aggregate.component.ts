import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { InventoryService } from '../services/inventory.service';
import { catchError, of } from 'rxjs';
import { DataTableComponent } from '../../shared/data-table/data-table.component';
import { AggregateDataService } from '../services/aggregate-data.service';
import {
  INVENTORY_VALUE_COLUMNS,
  PRODUCT_SUMMARY_COLUMNS,
  STOCK_COLUMNS,
  WAREHOUSE_COLUMNS,
} from '../../shared/data-table/table-columns/model';
import { DataAccordionComponent } from '../../shared/data-accordion/data-accordion.component';

@Component({
  selector: 'app-aggregate',
  standalone: true,
  imports: [
    CommonModule,
    CurrencyPipe,
    DataTableComponent,
    DataAccordionComponent,
  ],
  providers: [AggregateDataService],
  templateUrl: './aggregate.component.html',
})
export class AggregateComponent implements OnInit {
  // Expose column definitions
  protected readonly warehouseColumns = WAREHOUSE_COLUMNS;
  protected readonly inventoryValueColumns = INVENTORY_VALUE_COLUMNS;
  protected readonly productSummaryColumns = PRODUCT_SUMMARY_COLUMNS;
  protected readonly lowStockColumns = STOCK_COLUMNS.low;
  protected readonly highStockColumns = STOCK_COLUMNS.high;

  constructor(
    private inventoryService: InventoryService,
    protected dataService: AggregateDataService,
  ) {}

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
          this.dataService.updateData(data);
        }
      });
  }
}
