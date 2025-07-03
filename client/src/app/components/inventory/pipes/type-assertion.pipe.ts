import { Pipe, PipeTransform } from '@angular/core';
import { StockItem } from '../models/inventory.models';

@Pipe({
  name: 'asNumber',
  standalone: true
})
export class AsNumberPipe implements PipeTransform {
  transform(value: unknown): number {
    return typeof value === 'number' ? value : 0;
  }
}

@Pipe({
  name: 'asStockItemArray',
  standalone: true
})
export class AsStockItemArrayPipe implements PipeTransform {
  transform(value: unknown): StockItem[] {
    return Array.isArray(value) ? value : [];
  }
}
