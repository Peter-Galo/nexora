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
                .map(stock -> entityMapper.mapToDTO(stock, StockDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StockDTO getStockById(UUID id) {
        return stockRepository.findById(id)
                .map(stock -> entityMapper.mapToDTO(stock, StockDTO.class))
                .orElseThrow(() -> new ApplicationException("Stock not found with id: " + id, "STOCK_NOT_FOUND"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getStocksByProductId(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApplicationException("Product not found with id: " + productId, "PRODUCT_NOT_FOUND"));

        return stockRepository.findByProduct(product).stream()
                .map(stock -> entityMapper.mapToDTO(stock, StockDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getStocksByProductCode(String productCode) {
        return stockRepository.findByProductCode(productCode).stream()
                .map(stock -> entityMapper.mapToDTO(stock, StockDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getStocksByWarehouseId(UUID warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ApplicationException("Warehouse not found with id: " + warehouseId, "WAREHOUSE_NOT_FOUND"));

        return stockRepository.findByWarehouse(warehouse).stream()
                .map(stock -> entityMapper.mapToDTO(stock, StockDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getStocksByWarehouseCode(String warehouseCode) {
        return stockRepository.findByWarehouseCode(warehouseCode).stream()
                .map(stock -> entityMapper.mapToDTO(stock, StockDTO.class))
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
                .map(stock -> entityMapper.mapToDTO(stock, StockDTO.class))
                .orElseThrow(() -> new ApplicationException(
                        "Stock not found for product id: " + productId + " and warehouse id: " + warehouseId,
                        "STOCK_NOT_FOUND"));
    }

    @Override
    public StockDTO createStock(StockDTO stockDTO) {
        // Get product and warehouse entities
        Product product = getProductFromDTO(stockDTO.getProduct());
        Warehouse warehouse = getWarehouseFromDTO(stockDTO.getWarehouse());

        // Check if stock already exists for this product and warehouse
        if (stockRepository.findByProductAndWarehouse(product, warehouse).isPresent()) {
            throw new ApplicationException(
                    "Stock already exists for product code: " + product.getCode() +
                            " and warehouse code: " + warehouse.getCode(),
                    "STOCK_ALREADY_EXISTS");
        }

        // Use the entity mapper to map stockDTO to a Stock entity
        Stock stock = entityMapper.mapToEntity(stockDTO, Stock.class);
        stock.setProduct(product);
        stock.setWarehouse(warehouse);
        stock.setCreatedAt(LocalDateTime.now());
        stock.setUpdatedAt(LocalDateTime.now());

        Stock savedStock = stockRepository.save(stock);
        return entityMapper.mapToDTO(savedStock, StockDTO.class);
    }

    @Override
    public StockDTO updateStock(UUID id, StockDTO stockDTO) {
        Stock existingStock = stockRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("Stock not found with id: " + id, "STOCK_NOT_FOUND"));

        // Get product and warehouse entities
        Product product = getProductFromDTO(stockDTO.getProduct());
        Warehouse warehouse = getWarehouseFromDTO(stockDTO.getWarehouse());

        // Check if changing product or warehouse would create a duplicate
        if ((!existingStock.getProduct().getUuid().equals(product.getUuid()) ||
                !existingStock.getWarehouse().getUuid().equals(warehouse.getUuid())) &&
                stockRepository.findByProductAndWarehouse(product, warehouse).isPresent()) {
            throw new ApplicationException(
                    "Stock already exists for product code: " + product.getCode() +
                            " and warehouse code: " + warehouse.getCode(),
                    "STOCK_ALREADY_EXISTS");
        }

        // Use the entity mapper to update fields from StockDTO to existing entity
        entityMapper.mapToExistingEntity(stockDTO, existingStock);
        existingStock.setProduct(product);
        existingStock.setWarehouse(warehouse);
        existingStock.setUpdatedAt(LocalDateTime.now());

        Stock updatedStock = stockRepository.save(existingStock);
        return entityMapper.mapToDTO(updatedStock, StockDTO.class);
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
        return entityMapper.mapToDTO(updatedStock, StockDTO.class);
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
        return entityMapper.mapToDTO(updatedStock, StockDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getLowStocks() {
        return stockRepository.findLowStock().stream()
                .map(stock -> entityMapper.mapToDTO(stock, StockDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getOverStocks() {
        return stockRepository.findOverStock().stream()
                .map(stock -> entityMapper.mapToDTO(stock, StockDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getZeroStocks() {
        return stockRepository.findByQuantity(0).stream()
                .map(stock -> entityMapper.mapToDTO(stock, StockDTO.class))
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
        if (productDTO.getUuid() != null) {
            return productRepository.findById(productDTO.getUuid())
                    .orElseThrow(() -> new ApplicationException("Product not found with id: " + productDTO.getUuid(), "PRODUCT_NOT_FOUND"));
        } else if (productDTO.getCode() != null) {
            return productRepository.findByCode(productDTO.getCode())
                    .orElseThrow(() -> new ApplicationException("Product not found with code: " + productDTO.getCode(), "PRODUCT_NOT_FOUND"));
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
        if (warehouseDTO.getUuid() != null) {
            return warehouseRepository.findById(warehouseDTO.getUuid())
                    .orElseThrow(() -> new ApplicationException("Warehouse not found with id: " + warehouseDTO.getUuid(), "WAREHOUSE_NOT_FOUND"));
        } else if (warehouseDTO.getCode() != null) {
            return warehouseRepository.findByCode(warehouseDTO.getCode())
                    .orElseThrow(() -> new ApplicationException("Warehouse not found with code: " + warehouseDTO.getCode(), "WAREHOUSE_NOT_FOUND"));
        } else {
            throw new ApplicationException("Warehouse ID or code is required", "INVALID_WAREHOUSE");
        }
    }
}
