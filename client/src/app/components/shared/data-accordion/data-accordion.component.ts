import { Component, Input } from '@angular/core';
import { StockItem } from '../../inventory/models/inventory.models';
import {
  DataTableComponent,
  TableColumn,
} from '../data-table/data-table.component';
import { AggregateDataService } from '../../../services/inventory/aggregate-data.service';

@Component({
  selector: 'app-data-accordion',
  imports: [DataTableComponent],
  templateUrl: './data-accordion.component.html',
  styleUrl: './data-accordion.component.css',
})
export class DataAccordionComponent {
  @Input() stockData: [string, StockItem[]][] = [];
  @Input() columns: TableColumn[] = [];
  @Input() title: string = '';
  @Input() itemTitle: string = '';
  @Input() accordionId: string = '';
  @Input() accordionPrefix: string = '';

  constructor(public dataService: AggregateDataService) {}
}
