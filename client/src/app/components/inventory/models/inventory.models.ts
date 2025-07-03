// Interfaces for inventory data structures

// Warehouse overview item
export interface WarehouseOverview {
  uuid: string;
  name: string;
  totalProductQuantity: number;
  totalStockValue: number;
}

// Warehouse reference
export interface WarehouseReference {
  uuid: string;
  name: string;
}

// Stock item (for low/high stock)
export interface StockItem {
  productUuid: string;
  productCode: string;
  productName: string;
  quantity: number;
  warehouseUuid: string;
  minStockLevel?: number;
  maxStockLevel?: number;
}

// Product summary item
export interface ProductSummary {
  uuid: string;
  code: string;
  name: string;
  totalQuantity: number;
  totalValue: number;
  warehousesPresentIn: WarehouseReference[];
}

// Inventory value data
export interface InventoryValue {
  totalInventoryValue: number;
  byWarehouse: Record<string, number>;
}

// Stock levels data
export interface StockLevels {
  totalProducts: number;
  totalStockEntries: number;
  totalLowStockProducts: number;
  totalHighStockProducts: number;
  lowStockEntries: number;
  highStockEntries: number;
  lowStockProductCodes: string[];
  highStockProductCodes: string[];
  lowStockByWarehouse: Record<string, StockItem[]>;
  highStockByWarehouse: Record<string, StockItem[]>;
}

// Complete aggregate report data
export interface AggregateReportData {
  warehouseOverview: WarehouseOverview[];
  stockLevels: StockLevels;
  inventoryValue: InventoryValue;
  productSummary: ProductSummary[];
}
