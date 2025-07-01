package com.nexora.graphql;

import com.nexora.dto.inventory.ProductDTO;
import com.nexora.dto.inventory.StockDTO;
import com.nexora.dto.inventory.WarehouseDTO;
import com.nexora.service.inventory.ProductService;
import com.nexora.service.inventory.StockService;
import com.nexora.service.inventory.WarehouseService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

/**
 * GraphQL resolver for Stock entity.
 */
@Controller
public class StockGraphQLResolver {

    private final StockService stockService;
    private final ProductService productService;
    private final WarehouseService warehouseService;

    public StockGraphQLResolver(StockService stockService, ProductService productService, WarehouseService warehouseService) {
        this.stockService = stockService;
        this.productService = productService;
        this.warehouseService = warehouseService;
    }

    @QueryMapping
    public List<StockDTO> allStocks() {
        return stockService.getAllStocks();
    }

    @QueryMapping
    public StockDTO stockById(@Argument String id) {
        return stockService.getStockById(UUID.fromString(id));
    }

    @QueryMapping
    public List<StockDTO> stocksByProductId(@Argument String productId) {
        return stockService.getStocksByProductId(UUID.fromString(productId));
    }

    @QueryMapping
    public List<StockDTO> stocksByProductCode(@Argument String productCode) {
        return stockService.getStocksByProductCode(productCode);
    }

    @QueryMapping
    public List<StockDTO> stocksByWarehouseId(@Argument String warehouseId) {
        return stockService.getStocksByWarehouseId(UUID.fromString(warehouseId));
    }

    @QueryMapping
    public List<StockDTO> stocksByWarehouseCode(@Argument String warehouseCode) {
        return stockService.getStocksByWarehouseCode(warehouseCode);
    }

    @QueryMapping
    public StockDTO stockByProductAndWarehouse(@Argument String productId, @Argument String warehouseId) {
        return stockService.getStockByProductAndWarehouse(UUID.fromString(productId), UUID.fromString(warehouseId));
    }

    @QueryMapping
    public List<StockDTO> lowStocks() {
        return stockService.getLowStocks();
    }

    @QueryMapping
    public List<StockDTO> overStocks() {
        return stockService.getOverStocks();
    }

    @QueryMapping
    public List<StockDTO> zeroStocks() {
        return stockService.getZeroStocks();
    }

    @MutationMapping
    public StockDTO createStock(@Argument("stock") StockInput input) {
        ProductDTO productDTO = productService.getProductById(UUID.fromString(input.getProductId()));
        WarehouseDTO warehouseDTO = warehouseService.getWarehouseById(UUID.fromString(input.getWarehouseId()));
        
        StockDTO stockDTO = new StockDTO();
        stockDTO.setProduct(productDTO);
        stockDTO.setWarehouse(warehouseDTO);
        stockDTO.setQuantity(input.getQuantity());
        stockDTO.setMinStockLevel(input.getMinStockLevel());
        stockDTO.setMaxStockLevel(input.getMaxStockLevel());
        
        return stockService.createStock(stockDTO);
    }

    @MutationMapping
    public StockDTO updateStock(@Argument String id, @Argument("stock") StockInput input) {
        StockDTO existingStock = stockService.getStockById(UUID.fromString(id));
        
        // Only update product and warehouse if they are provided
        if (input.getProductId() != null) {
            ProductDTO productDTO = productService.getProductById(UUID.fromString(input.getProductId()));
            existingStock.setProduct(productDTO);
        }
        
        if (input.getWarehouseId() != null) {
            WarehouseDTO warehouseDTO = warehouseService.getWarehouseById(UUID.fromString(input.getWarehouseId()));
            existingStock.setWarehouse(warehouseDTO);
        }
        
        if (input.getQuantity() != null) {
            existingStock.setQuantity(input.getQuantity());
        }
        
        if (input.getMinStockLevel() != null) {
            existingStock.setMinStockLevel(input.getMinStockLevel());
        }
        
        if (input.getMaxStockLevel() != null) {
            existingStock.setMaxStockLevel(input.getMaxStockLevel());
        }
        
        return stockService.updateStock(UUID.fromString(id), existingStock);
    }

    @MutationMapping
    public boolean deleteStock(@Argument String id) {
        stockService.deleteStock(UUID.fromString(id));
        return true;
    }

    @MutationMapping
    public StockDTO addStock(@Argument String id, @Argument int quantity) {
        return stockService.addStock(UUID.fromString(id), quantity);
    }

    @MutationMapping
    public StockDTO removeStock(@Argument String id, @Argument int quantity) {
        return stockService.removeStock(UUID.fromString(id), quantity);
    }
}