package com.nexora.service.inventory.impl;

import com.nexora.dto.inventory.ProductDTO;
import com.nexora.dto.inventory.StockDTO;
import com.nexora.dto.inventory.WarehouseDTO;
import com.nexora.exception.ApplicationException;
import com.nexora.model.inventory.Product;
import com.nexora.model.inventory.Stock;
import com.nexora.model.inventory.Warehouse;
import com.nexora.repository.inventory.ProductRepository;
import com.nexora.repository.inventory.StockRepository;
import com.nexora.repository.inventory.WarehouseRepository;
import com.nexora.service.inventory.StockService;
import com.nexora.util.EntityMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the StockService interface.
 */
@Service
@Transactional
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final EntityMapper entityMapper;

    public StockServiceImpl(StockRepository stockRepository,
                            ProductRepository productRepository,
                            WarehouseRepository warehouseRepository,
                            EntityMapper entityMapper) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.entityMapper = entityMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getAllStocks() {
        return stockRepository.findAllWithProductAndWarehouse().stream()
                .map(entityMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StockDTO getStockById(UUID id) {
        return stockRepository.findById(id)
                .map(entityMapper::mapToDTO)
                .orElseThrow(() -> new ApplicationException("Stock not found with id: " + id, "STOCK_NOT_FOUND"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getStocksByProductId(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApplicationException("Product not found with id: " + productId, "PRODUCT_NOT_FOUND"));

        return stockRepository.findByProduct(product).stream()
                .map(entityMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getStocksByProductCode(String productCode) {
        return stockRepository.findByProductCode(productCode).stream()
                .map(entityMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getStocksByWarehouseId(UUID warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ApplicationException("Warehouse not found with id: " + warehouseId, "WAREHOUSE_NOT_FOUND"));

        return stockRepository.findByWarehouse(warehouse).stream()
                .map(entityMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getStocksByWarehouseCode(String warehouseCode) {
        return stockRepository.findByWarehouseCode(warehouseCode).stream()
                .map(entityMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StockDTO getStockByProductAndWarehouse(UUID productId, UUID warehouseId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApplicationException("Product not found with id: " + productId, "PRODUCT_NOT_FOUND"));

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ApplicationException("Warehouse not found with id: " + warehouseId, "WAREHOUSE_NOT_FOUND"));

        return stockRepository.findByProductAndWarehouse(product, warehouse)
                .map(entityMapper::mapToDTO)
                .orElseThrow(() -> new ApplicationException(
                        "Stock not found for product id: " + productId + " and warehouse id: " + warehouseId,
                        "STOCK_NOT_FOUND"));
    }

    @Override
    public StockDTO createStock(StockDTO stockDTO) {
        // Get product and warehouse entities
        Product product = getProductFromDTO(stockDTO.product());
        Warehouse warehouse = getWarehouseFromDTO(stockDTO.warehouse());

        // Check if stock already exists for this product and warehouse
        if (stockRepository.findByProductAndWarehouse(product, warehouse).isPresent()) {
            throw new ApplicationException(
                    "Stock already exists for product code: " + product.getCode() +
                            " and warehouse code: " + warehouse.getCode(),
                    "STOCK_ALREADY_EXISTS");
        }

        // Create new stock
        Stock stock = new Stock();
        stock.setProduct(product);
        stock.setWarehouse(warehouse);
        stock.setQuantity(stockDTO.quantity());
        stock.setMinStockLevel(stockDTO.minStockLevel());
        stock.setMaxStockLevel(stockDTO.maxStockLevel());
        stock.setLastRestockDate(stockDTO.lastRestockDate());
        stock.setCreatedAt(LocalDateTime.now());
        stock.setUpdatedAt(LocalDateTime.now());

        Stock savedStock = stockRepository.save(stock);
        return entityMapper.mapToDTO(savedStock);
    }

    @Override
    public StockDTO updateStock(UUID id, StockDTO stockDTO) {
        Stock existingStock = stockRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("Stock not found with id: " + id, "STOCK_NOT_FOUND"));

        // Get product and warehouse entities
        Product product = getProductFromDTO(stockDTO.product());
        Warehouse warehouse = getWarehouseFromDTO(stockDTO.warehouse());

        // Check if changing product or warehouse would create a duplicate
        if ((existingStock.getProduct().getUuid() != product.getUuid() ||
                existingStock.getWarehouse().getUuid() != warehouse.getUuid()) &&
                stockRepository.findByProductAndWarehouse(product, warehouse).isPresent()) {
            throw new ApplicationException(
                    "Stock already exists for product code: " + product.getCode() +
                            " and warehouse code: " + warehouse.getCode(),
                    "STOCK_ALREADY_EXISTS");
        }

        // Update stock
        existingStock.setProduct(product);
        existingStock.setWarehouse(warehouse);
        existingStock.setQuantity(stockDTO.quantity());
        existingStock.setMinStockLevel(stockDTO.minStockLevel());
        existingStock.setMaxStockLevel(stockDTO.maxStockLevel());
        existingStock.setLastRestockDate(stockDTO.lastRestockDate());
        existingStock.setUpdatedAt(LocalDateTime.now());

        Stock updatedStock = stockRepository.save(existingStock);
        return entityMapper.mapToDTO(updatedStock);
    }

    @Override
    public void deleteStock(UUID id) {
        if (!stockRepository.existsById(id)) {
            throw new ApplicationException("Stock not found with id: " + id, "STOCK_NOT_FOUND");
        }

        stockRepository.deleteById(id);
    }

    @Override
    public StockDTO addStock(UUID id, int quantity) {
        if (quantity < 0) {
            throw new ApplicationException("Cannot add negative stock amount", "INVALID_QUANTITY");
        }

        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("Stock not found with id: " + id, "STOCK_NOT_FOUND"));

        stock.addStock(quantity);
        Stock updatedStock = stockRepository.save(stock);
        return entityMapper.mapToDTO(updatedStock);
    }

    @Override
    public StockDTO removeStock(UUID id, int quantity) {
        if (quantity < 0) {
            throw new ApplicationException("Cannot remove negative stock amount", "INVALID_QUANTITY");
        }

        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("Stock not found with id: " + id, "STOCK_NOT_FOUND"));

        if (stock.getQuantity() < quantity) {
            throw new ApplicationException("Not enough stock available", "INSUFFICIENT_STOCK");
        }

        stock.removeStock(quantity);
        Stock updatedStock = stockRepository.save(stock);
        return entityMapper.mapToDTO(updatedStock);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getLowStocks() {
        return stockRepository.findLowStock().stream()
                .map(entityMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getOverStocks() {
        return stockRepository.findOverStock().stream()
                .map(entityMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getZeroStocks() {
        return stockRepository.findByQuantity(0).stream()
                .map(entityMapper::mapToDTO)
                .collect(Collectors.toList());
    }


    /**
     * Gets a Product entity from a ProductDTO.
     *
     * @param productDTO the ProductDTO
     * @return the Product entity
     * @throws com.nexora.exception.ApplicationException if the product is not found
     */
    private Product getProductFromDTO(ProductDTO productDTO) {
        if (productDTO.uuid() != null) {
            return productRepository.findById(productDTO.uuid())
                    .orElseThrow(() -> new ApplicationException("Product not found with id: " + productDTO.uuid(), "PRODUCT_NOT_FOUND"));
        } else if (productDTO.code() != null) {
            return productRepository.findByCode(productDTO.code())
                    .orElseThrow(() -> new ApplicationException("Product not found with code: " + productDTO.code(), "PRODUCT_NOT_FOUND"));
        } else {
            throw new ApplicationException("Product ID or code is required", "INVALID_PRODUCT");
        }
    }

    /**
     * Gets a Warehouse entity from a WarehouseDTO.
     *
     * @param warehouseDTO the WarehouseDTO
     * @return the Warehouse entity
     * @throws com.nexora.exception.ApplicationException if the warehouse is not found
     */
    private Warehouse getWarehouseFromDTO(WarehouseDTO warehouseDTO) {
        if (warehouseDTO.uuid() != null) {
            return warehouseRepository.findById(warehouseDTO.uuid())
                    .orElseThrow(() -> new ApplicationException("Warehouse not found with id: " + warehouseDTO.uuid(), "WAREHOUSE_NOT_FOUND"));
        } else if (warehouseDTO.code() != null) {
            return warehouseRepository.findByCode(warehouseDTO.code())
                    .orElseThrow(() -> new ApplicationException("Warehouse not found with code: " + warehouseDTO.code(), "WAREHOUSE_NOT_FOUND"));
        } else {
            throw new ApplicationException("Warehouse ID or code is required", "INVALID_WAREHOUSE");
        }
    }
}
