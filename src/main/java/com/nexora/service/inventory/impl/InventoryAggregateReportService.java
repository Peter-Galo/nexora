package com.nexora.service.inventory.impl;

import com.nexora.model.inventory.Product;
import com.nexora.model.inventory.Stock;
import com.nexora.model.inventory.Warehouse;
import com.nexora.repository.inventory.StockRepository;
import com.nexora.repository.inventory.WarehouseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
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
        List<Stock> stocks = stockRepository.findAllWithProductAndWarehouse();
        List<Warehouse> warehouses = warehouseRepository.findAll();

        Map<UUID, Product> productMap = buildProductMap(stocks);
        Map<UUID, List<Stock>> stocksByWarehouse = groupStocksByWarehouse(stocks);
        Map<UUID, List<Stock>> stocksByProduct = groupStocksByProduct(stocks);

        Map<String, Object> report = new HashMap<>();
        report.put("warehouseOverview", buildWarehouseOverview(warehouses, stocksByWarehouse));
        report.put("stockLevels", buildStockLevels(warehouses, stocks, stocksByWarehouse, productMap));
        report.put("inventoryValue", buildInventoryValue(warehouses, stocksByWarehouse));
        report.put("productSummary", buildProductSummary(stocksByProduct, productMap));
        return report;
    }

    private Map<UUID, Product> buildProductMap(List<Stock> stocks) {
        return stocks.stream()
                .map(Stock::getProduct)
                .collect(Collectors.toMap(Product::getUuid, p -> p, (a, b) -> a));
    }

    private Map<UUID, List<Stock>> groupStocksByWarehouse(List<Stock> stocks) {
        return stocks.stream()
                .collect(Collectors.groupingBy(s -> s.getWarehouse().getUuid()));
    }

    private Map<UUID, List<Stock>> groupStocksByProduct(List<Stock> stocks) {
        return stocks.stream()
                .collect(Collectors.groupingBy(s -> s.getProduct().getUuid()));
    }

    private List<Map<String, Object>> buildWarehouseOverview(List<Warehouse> warehouses, Map<UUID, List<Stock>> stocksByWarehouse) {
        return warehouses.stream()
                .map(w -> {
                    List<Stock> warehouseStocks = stocksByWarehouse.getOrDefault(w.getUuid(), Collections.emptyList());
                    BigDecimal totalStockValue = warehouseStocks.stream()
                            .map(s -> s.getProduct().getPrice().multiply(BigDecimal.valueOf(s.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    int totalProductQuantity = warehouseStocks.stream()
                            .mapToInt(Stock::getQuantity)
                            .sum();
                    Map<String, Object> wh = new HashMap<>();
                    wh.put("uuid", w.getUuid());
                    wh.put("name", w.getName());
                    wh.put("totalStockValue", totalStockValue);
                    wh.put("totalProductQuantity", totalProductQuantity);
                    return wh;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("totalStockValue")).compareTo((BigDecimal) a.get("totalStockValue")))
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildStockLevels(
            List<Warehouse> warehouses,
            List<Stock> stocks,
            Map<UUID, List<Stock>> stocksByWarehouse,
            Map<UUID, Product> productMap
    ) {
        Map<String, List<Map<String, Object>>> lowStockByWarehouse = new TreeMap<>();
        Map<String, List<Map<String, Object>>> highStockByWarehouse = new TreeMap<>();
        Set<UUID> lowStockProductUuids = new HashSet<>();
        Set<UUID> highStockProductUuids = new HashSet<>();
        int lowStockEntries = 0;
        int highStockEntries = 0;

        for (Warehouse w : warehouses) {
            List<Stock> warehouseStocks = stocksByWarehouse.getOrDefault(w.getUuid(), Collections.emptyList());

            List<Map<String, Object>> lowStocks = mapStockEntries(
                    warehouseStocks, Stock::isLowStock, lowStockProductUuids, w, true
            );
            lowStockByWarehouse.put(w.getName(), lowStocks);
            lowStockEntries += lowStocks.size();

            List<Map<String, Object>> highStocks = mapStockEntries(
                    warehouseStocks, Stock::isOverStock, highStockProductUuids, w, false
            );
            highStockByWarehouse.put(w.getName(), highStocks);
            highStockEntries += highStocks.size();
        }

        // Sort by number of products descending
        lowStockByWarehouse = lowStockByWarehouse.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        highStockByWarehouse = highStockByWarehouse.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Set<String> lowStockProductCodes = lowStockProductUuids.stream()
                .map(uuid -> productMap.get(uuid).getCode())
                .collect(Collectors.toSet());
        Set<String> highStockProductCodes = highStockProductUuids.stream()
                .map(uuid -> productMap.get(uuid).getCode())
                .collect(Collectors.toSet());

        Map<String, Object> stockLevels = new HashMap<>();
        stockLevels.put("totalProducts", productMap.size());
        stockLevels.put("totalStockEntries", stocks.size());
        stockLevels.put("totalLowStockProducts", lowStockProductUuids.size());
        stockLevels.put("totalHighStockProducts", highStockProductUuids.size());
        stockLevels.put("lowStockEntries", lowStockEntries);
        stockLevels.put("highStockEntries", highStockEntries);
        stockLevels.put("lowStockProductCodes", lowStockProductCodes);
        stockLevels.put("highStockProductCodes", highStockProductCodes);
        stockLevels.put("lowStockByWarehouse", lowStockByWarehouse);
        stockLevels.put("highStockByWarehouse", highStockByWarehouse);
        return stockLevels;
    }

    private List<Map<String, Object>> mapStockEntries(
            List<Stock> stocks,
            java.util.function.Predicate<Stock> filter,
            Set<UUID> productUuids,
            Warehouse warehouse,
            boolean isLowStock
    ) {
        return stocks.stream()
                .filter(filter)
                .sorted((a, b) -> b.getProduct().getPrice().compareTo(a.getProduct().getPrice()))
                .map(s -> {
                    productUuids.add(s.getProduct().getUuid());
                    Map<String, Object> map = new HashMap<>();
                    map.put("productUuid", s.getProduct().getUuid());
                    map.put("productCode", s.getProduct().getCode());
                    map.put("productName", s.getProduct().getName());
                    map.put("quantity", s.getQuantity());
                    map.put(isLowStock ? "minStockLevel" : "maxStockLevel",
                            isLowStock ? s.getMinStockLevel() : s.getMaxStockLevel());
                    map.put("warehouseUuid", warehouse.getUuid());
                    return map;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildInventoryValue(List<Warehouse> warehouses, Map<UUID, List<Stock>> stocksByWarehouse) {
        // Sort by warehouse value descending
        Map<String, BigDecimal> valueByWarehouse = warehouses.stream()
                .map(w -> {
                    List<Stock> warehouseStocks = stocksByWarehouse.getOrDefault(w.getUuid(), Collections.emptyList());
                    BigDecimal warehouseValue = warehouseStocks.stream()
                            .map(s -> s.getProduct().getPrice().multiply(BigDecimal.valueOf(s.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    Map.Entry<String, BigDecimal> entry = new AbstractMap.SimpleEntry<>(w.getName(), warehouseValue);
                    return entry;
                })
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        BigDecimal totalValue = valueByWarehouse.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> inventoryValue = new HashMap<>();
        inventoryValue.put("totalInventoryValue", totalValue);
        inventoryValue.put("byWarehouse", valueByWarehouse);
        return inventoryValue;
    }

    private List<Map<String, Object>> buildProductSummary(Map<UUID, List<Stock>> stocksByProduct, Map<UUID, Product> productMap) {
        return stocksByProduct.entrySet().stream()
                .map(entry -> {
                    Product p = productMap.get(entry.getKey());
                    int totalQuantity = entry.getValue().stream().mapToInt(Stock::getQuantity).sum();
                    BigDecimal productTotalValue = entry.getValue().stream()
                            .map(s -> s.getProduct().getPrice().multiply(BigDecimal.valueOf(s.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    Set<Map<String, Object>> warehousesPresentIn = entry.getValue().stream()
                            .map(s -> {
                                Warehouse w = s.getWarehouse();
                                Map<String, Object> wh = new HashMap<>();
                                wh.put("uuid", w.getUuid());
                                wh.put("name", w.getName());
                                return wh;
                            })
                            .collect(Collectors.toSet());
                    Map<String, Object> map = new HashMap<>();
                    map.put("uuid", p.getUuid());
                    map.put("code", p.getCode());
                    map.put("name", p.getName());
                    map.put("totalQuantity", totalQuantity);
                    map.put("totalValue", productTotalValue);
                    map.put("warehousesPresentIn", warehousesPresentIn);
                    return map;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("totalValue")).compareTo((BigDecimal) a.get("totalValue")))
                .collect(Collectors.toList());
    }
}