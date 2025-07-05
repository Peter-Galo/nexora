// data-table.component.ts
import { Component, ContentChild, Input, TemplateRef } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';

export interface TableColumn {
  header: string;
  field: string;
  isCurrency?: boolean;
  customTemplate?: boolean;
}

@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  templateUrl: './data-table.component.html',
})
export class DataTableComponent {
  @Input() data: any[] = [];
  @Input() columns: TableColumn[] = [];
  @Input() title: string = 'Data Table';
  @Input() trackBy: string = 'id';
  @Input() emptyMessage: string = 'No data available';

  @ContentChild('customCell') customCell: TemplateRef<{
    $implicit: any;
    field: string;
  }> | null = null;

  // Helper method to get a nested property value using dot notation (e.g., "user.address.city")
  getPropertyValue(item: any, field: string): any {
    return field.split('.').reduce((obj, prop) => obj && obj[prop], item);
  }
}
