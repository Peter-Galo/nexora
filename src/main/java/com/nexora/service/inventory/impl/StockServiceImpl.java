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
    
    public StockServiceImpl(StockRepository stockRepository, 
                           ProductRepository productRepository, 
                           WarehouseRepository warehouseRepository) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public StockDTO getStockById(UUID id) {
        return stockRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ApplicationException("Stock not found with id: " + id, "STOCK_NOT_FOUND"));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getStocksByProductId(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApplicationException("Product not found with id: " + productId, "PRODUCT_NOT_FOUND"));
        
        return stockRepository.findByProduct(product).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getStocksByProductCode(String productCode) {
        return stockRepository.findByProductCode(productCode).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getStocksByWarehouseId(UUID warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ApplicationException("Warehouse not found with id: " + warehouseId, "WAREHOUSE_NOT_FOUND"));
        
        return stockRepository.findByWarehouse(warehouse).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getStocksByWarehouseCode(String warehouseCode) {
        return stockRepository.findByWarehouseCode(warehouseCode).stream()
                .map(this::mapToDTO)
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
                .map(this::mapToDTO)
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
        
        // Create new stock
        Stock stock = new Stock();
        stock.setProduct(product);
        stock.setWarehouse(warehouse);
        stock.setQuantity(stockDTO.getQuantity());
        stock.setMinStockLevel(stockDTO.getMinStockLevel());
        stock.setMaxStockLevel(stockDTO.getMaxStockLevel());
        stock.setLastRestockDate(stockDTO.getLastRestockDate());
        stock.setCreatedAt(LocalDateTime.now());
        stock.setUpdatedAt(LocalDateTime.now());
        
        Stock savedStock = stockRepository.save(stock);
        return mapToDTO(savedStock);
    }
    
    @Override
    public StockDTO updateStock(UUID id, StockDTO stockDTO) {
        Stock existingStock = stockRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("Stock not found with id: " + id, "STOCK_NOT_FOUND"));
        
        // Get product and warehouse entities
        Product product = getProductFromDTO(stockDTO.getProduct());
        Warehouse warehouse = getWarehouseFromDTO(stockDTO.getWarehouse());
        
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
        existingStock.setQuantity(stockDTO.getQuantity());
        existingStock.setMinStockLevel(stockDTO.getMinStockLevel());
        existingStock.setMaxStockLevel(stockDTO.getMaxStockLevel());
        existingStock.setLastRestockDate(stockDTO.getLastRestockDate());
        existingStock.setUpdatedAt(LocalDateTime.now());
        
        Stock updatedStock = stockRepository.save(existingStock);
        return mapToDTO(updatedStock);
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
        return mapToDTO(updatedStock);
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
        return mapToDTO(updatedStock);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getLowStocks() {
        return stockRepository.findLowStock().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getOverStocks() {
        return stockRepository.findOverStock().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getZeroStocks() {
        return stockRepository.findByQuantity(0).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Maps a Stock entity to a StockDTO.
     *
     * @param stock the Stock entity
     * @return the StockDTO
     */
    private StockDTO mapToDTO(Stock stock) {
        ProductDTO productDTO = mapProductToDTO(stock.getProduct());
        WarehouseDTO warehouseDTO = mapWarehouseToDTO(stock.getWarehouse());
        
        return new StockDTO(
                stock.getUuid(),
                productDTO,
                warehouseDTO,
                stock.getQuantity(),
                stock.getMinStockLevel(),
                stock.getMaxStockLevel(),
                stock.getLastRestockDate(),
                stock.getCreatedAt(),
                stock.getUpdatedAt()
        );
    }
    
    /**
     * Maps a Product entity to a ProductDTO.
     *
     * @param product the Product entity
     * @return the ProductDTO
     */
    private ProductDTO mapProductToDTO(Product product) {
        return new ProductDTO(
                product.getUuid(),
                product.getCode(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.isActive(),
                product.getCategory(),
                product.getBrand(),
                product.getSku()
        );
    }
    
    /**
     * Maps a Warehouse entity to a WarehouseDTO.
     *
     * @param warehouse the Warehouse entity
     * @return the WarehouseDTO
     */
    private WarehouseDTO mapWarehouseToDTO(Warehouse warehouse) {
        return new WarehouseDTO(
                warehouse.getUuid(),
                warehouse.getCode(),
                warehouse.getName(),
                warehouse.getDescription(),
                warehouse.getAddress(),
                warehouse.getCity(),
                warehouse.getStateProvince(),
                warehouse.getPostalCode(),
                warehouse.getCountry(),
                warehouse.getCreatedAt(),
                warehouse.getUpdatedAt(),
                warehouse.isActive()
        );
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
        if (warehouseDTO.getId() != null) {
            return warehouseRepository.findById(warehouseDTO.getId())
                    .orElseThrow(() -> new ApplicationException("Warehouse not found with id: " + warehouseDTO.getId(), "WAREHOUSE_NOT_FOUND"));
        } else if (warehouseDTO.getCode() != null) {
            return warehouseRepository.findByCode(warehouseDTO.getCode())
                    .orElseThrow(() -> new ApplicationException("Warehouse not found with code: " + warehouseDTO.getCode(), "WAREHOUSE_NOT_FOUND"));
        } else {
            throw new ApplicationException("Warehouse ID or code is required", "INVALID_WAREHOUSE");
        }
    }
}