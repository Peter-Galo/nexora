package com.nexora.controller.inventory;

import com.nexora.dto.inventory.ProductDTO;
import com.nexora.model.inventory.ExportJob;
import com.nexora.model.inventory.event.ExportRequestEvent;
import com.nexora.repository.UserRepository;
import com.nexora.repository.inventory.ExportJobRepository;
import com.nexora.security.JwtService;
import com.nexora.service.event.ExportMessageProducer;
import com.nexora.service.inventory.ExportService;
import com.nexora.service.inventory.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing products.
 */
@RestController
@RequestMapping("/api/v1/inventory/products")
@Tag(name = "Product Management", description = "APIs for managing products in the inventory system")
public class ProductController {

    private final ProductService productService;
    private final ExportService exportService;
    private final ExportMessageProducer exportMessageProducer;
    private final ExportJobRepository exportJobRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public ProductController(
            ProductService productService, 
            ExportService exportService, 
            ExportMessageProducer exportMessageProducer, 
            ExportJobRepository exportJobRepository,
            JwtService jwtService,
            UserRepository userRepository) {
        this.productService = productService;
        this.exportService = exportService;
        this.exportMessageProducer = exportMessageProducer;
        this.exportJobRepository = exportJobRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Get all products", description = "Retrieves a list of all products")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of products"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @Operation(summary = "Get active products", description = "Retrieves a list of all active products")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of active products"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/active")
    public ResponseEntity<List<ProductDTO>> getActiveProducts() {
        return ResponseEntity.ok(productService.getActiveProducts());
    }

    @Operation(summary = "Get product by ID", description = "Retrieves a product by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the product"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(
            @Parameter(description = "ID of the product to retrieve", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(summary = "Get product by code", description = "Retrieves a product by its code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the product"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/code/{code}")
    public ResponseEntity<ProductDTO> getProductByCode(
            @Parameter(description = "Code of the product to retrieve", required = true)
            @PathVariable String code) {
        return ResponseEntity.ok(productService.getProductByCode(code));
    }

    @Operation(summary = "Create a new product", description = "Creates a new product with the provided information. The product ID will be auto-generated and should not be included in the request.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid input or product code already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(
            @Parameter(description = "Product information", required = true, schema = @Schema(implementation = ProductDTO.class))
            @Valid @RequestBody ProductDTO productDTO) {
        return new ResponseEntity<>(productService.createProduct(productDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing product", description = "Updates an existing product with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product successfully updated"),
        @ApiResponse(responseCode = "400", description = "Invalid input or product code already exists"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @Parameter(description = "ID of the product to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated product information", required = true, schema = @Schema(implementation = ProductDTO.class))
            @Valid @RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.updateProduct(id, productDTO));
    }

    @Operation(summary = "Delete a product", description = "Deletes a product by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Product successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID of the product to delete", required = true)
            @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deactivate a product", description = "Deactivates a product by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product successfully deactivated"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ProductDTO> deactivateProduct(
            @Parameter(description = "ID of the product to deactivate", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.deactivateProduct(id));
    }

    @Operation(summary = "Activate a product", description = "Activates a product by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product successfully activated"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}/activate")
    public ResponseEntity<ProductDTO> activateProduct(
            @Parameter(description = "ID of the product to activate", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.activateProduct(id));
    }

    @Operation(summary = "Get products by category", description = "Retrieves a list of products in the specified category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of products"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(
            @Parameter(description = "Category of products to retrieve", required = true)
            @PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    @Operation(summary = "Get products by brand", description = "Retrieves a list of products of the specified brand")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of products"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/brand/{brand}")
    public ResponseEntity<List<ProductDTO>> getProductsByBrand(
            @Parameter(description = "Brand of products to retrieve", required = true)
            @PathVariable String brand) {
        return ResponseEntity.ok(productService.getProductsByBrand(brand));
    }

    @Operation(summary = "Search products by name", description = "Searches for products whose names contain the specified text")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of products"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProductsByName(
            @Parameter(description = "Text to search for in product names", required = true)
            @RequestParam String name) {
        return ResponseEntity.ok(productService.searchProductsByName(name));
    }

    @Operation(summary = "Request product export as XLSX",
            description = "Initiates an asynchronous export of all products as Excel")
    @ApiResponse(responseCode = "202", description = "Export job accepted")
    @GetMapping("/export/xlsx")
    public ResponseEntity<Map<String, Object>> requestProductExport(
            @RequestHeader("Authorization") String authHeader) {

        // Extract user ID from JWT token
        String userId = jwtService.extractUserUUIDFromAuthHeader(authHeader);

        // Initiate export job
        String jobId = exportService.initiateExport(userId, "XLSX");

        // Send message to queue
        ExportRequestEvent event = new ExportRequestEvent();
        event.setJobId(jobId);
        event.setUserId(userId);
        event.setExportType("XLSX");
        exportMessageProducer.sendExportRequest(event);

        // Return job ID to client
        Map<String, Object> response = new HashMap<>();
        response.put("jobId", jobId);
        response.put("message", "Export job initiated successfully");

        return ResponseEntity.accepted().body(response);
    }

    @Operation(summary = "Get export job status",
            description = "Retrieves the current status of an export job")
    @ApiResponse(responseCode = "200", description = "Job status retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Export job not found")
    @GetMapping("/export/status/{jobId}")
    public ResponseEntity<ExportJob> getExportStatus(@PathVariable String jobId) {
        return exportJobRepository.findByJobId(jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Download exported file",
            description = "Downloads a completed export file")
    @ApiResponse(responseCode = "302", description = "Redirect to file download")
    @ApiResponse(responseCode = "404", description = "Export job not found or not completed")
    @GetMapping("/export/download/{jobId}")
    public ResponseEntity<?> downloadExportedFile(@PathVariable String jobId) {
        return exportJobRepository.findByJobId(jobId)
                .filter(job -> "COMPLETED".equals(job.getStatus()))
                .map(job -> ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(job.getFileUrl()))
                        .build())
                .orElse(ResponseEntity.notFound().build());
    }
}
