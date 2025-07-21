// src/app/components/inventory/aggregate/table-columns.model.ts

import { TableColumn } from '../data-table.component';

export const WAREHOUSE_COLUMNS: TableColumn[] = [
  { header: 'Warehouse', field: 'name' },
  { header: 'Product Count', field: 'totalProductQuantity' },
  { header: 'Total Stock Value', field: 'totalStockValue', isCurrency: true },
];

export const WAREHOUSE_MANAGEMENT_COLUMNS: TableColumn[] = [
  { header: 'Code', field: 'code' },
  { header: 'Name', field: 'name' },
  { header: 'City', field: 'city' },
  { header: 'State/Province', field: 'stateProvince' },
  { header: 'Country', field: 'country' },
  { header: 'Status', field: 'active', customTemplate: true },
  { header: 'Actions', field: 'actions', customTemplate: true },
];

export const INVENTORY_VALUE_COLUMNS: TableColumn[] = [
  { header: 'Warehouse', field: '0' },
  { header: 'Value', field: '1', isCurrency: true },
];

export const PRODUCT_SUMMARY_COLUMNS: TableColumn[] = [
  { header: 'Product', field: 'name' },
  { header: 'Code', field: 'code' },
  { header: 'Total Quantity', field: 'totalQuantity' },
  { header: 'Total Value', field: 'totalValue', isCurrency: true },
  { header: 'Warehouses', field: 'warehousesPresentIn', customTemplate: true },
];

export const STOCK_COLUMNS = {
  low: [
    { header: 'Product', field: 'productName' },
    { header: 'Code', field: 'productCode' },
    { header: 'Quantity', field: 'quantity' },
    { header: 'Min Stock', field: 'minStockLevel' },
  ],
  high: [
    { header: 'Product', field: 'productName' },
    { header: 'Code', field: 'productCode' },
    { header: 'Quantity', field: 'quantity' },
    { header: 'Max Stock', field: 'maxStockLevel' },
  ],
};

export const PRODUCT_COLUMNS: TableColumn[] = [
  { header: 'Code', field: 'code' },
  { header: 'Name', field: 'name' },
  { header: 'Category', field: 'category' },
  { header: 'Brand', field: 'brand' },
  { header: 'Price', field: 'price', isCurrency: true },
  { header: 'SKU', field: 'sku' },
  { header: 'Status', field: 'active', customTemplate: true },
  { header: 'Created', field: 'createdAt', customTemplate: true },
];
