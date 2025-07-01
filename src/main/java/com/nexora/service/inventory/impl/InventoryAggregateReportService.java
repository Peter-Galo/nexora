package com.nexora.service.inventory.impl;

import com.nexora.model.inventory.Stock;
import com.nexora.model.inventory.Warehouse;
import com.nexora.repository.inventory.StockRepository;
import com.nexora.repository.inventory.WarehouseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventoryAggregateReportService {

    private final StockRepository stockRepository;
    private final WarehouseRepository warehouseRepository;

    public InventoryAggregateReportService(StockRepository stockRepository, WarehouseRepository warehouseRepository) {
        this.stockRepository = stockRepository;
        this.warehouseRepository = warehouseRepository;
    }

    public Map<String, Object> getAggregateReport() {
        List<Stock> stocks = stockRepository.findAll();
        List<Warehouse> warehouses = warehouseRepository.findAll();

        // Low stock products per warehouse
        Map<String, List<Map<String, Object>>> lowStockByWarehouse = new HashMap<>();
        for (Warehouse w : warehouses) {
            List<Map<String, Object>> lowStocks = stocks.stream()
                    .filter(s -> s.getWarehouse().getUuid().equals(w.getUuid()) && s.isLowStock())
                    .map(s -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("productCode", s.getProduct().getCode());
                        map.put("productName", s.getProduct().getName());
                        map.put("quantity", s.getQuantity());
                        map.put("minStockLevel", s.getMinStockLevel());
                        return map;
                    })
                    .collect(Collectors.toList());
            lowStockByWarehouse.put(w.getName(), lowStocks);
        }

        // Total inventory value per warehouse
        Map<String, BigDecimal> valueByWarehouse = new HashMap<>();
        for (Warehouse w : warehouses) {
            BigDecimal value = stocks.stream()
                    .filter(s -> s.getWarehouse().getUuid().equals(w.getUuid()))
                    .map(s -> s.getProduct().getPrice().multiply(BigDecimal.valueOf(s.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            valueByWarehouse.put(w.getName(), value);
        }

        // Overall inventory value
        BigDecimal totalValue = stocks.stream()
                .map(s -> s.getProduct().getPrice().multiply(BigDecimal.valueOf(s.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Stock summary by product
        List<Map<String, Object>> productStockSummary = stocks.stream()
                .collect(Collectors.groupingBy(s -> s.getProduct().getCode()))
                .entrySet().stream()
                .map(e -> {
                    String productCode = e.getKey();
                    String productName = e.getValue().get(0).getProduct().getName();
                    int totalQty = e.getValue().stream().mapToInt(Stock::getQuantity).sum();
                    Map<String, Object> map = new HashMap<>();
                    map.put("productCode", productCode);
                    map.put("productName", productName);
                    map.put("totalQuantity", totalQty);
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> report = new HashMap<>();
        report.put("lowStockByWarehouse", lowStockByWarehouse);
        report.put("inventoryValueByWarehouse", valueByWarehouse);
        report.put("totalInventoryValue", totalValue);
        report.put("productStockSummary", productStockSummary);

        return report;
    }
}