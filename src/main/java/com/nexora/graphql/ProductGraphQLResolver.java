package com.nexora.graphql;

import com.nexora.dto.inventory.ProductDTO;
import com.nexora.service.inventory.ProductService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

/**
 * GraphQL resolver for Product entity.
 */
@Controller
public class ProductGraphQLResolver {

    private final ProductService productService;

    public ProductGraphQLResolver(ProductService productService) {
        this.productService = productService;
    }

    @QueryMapping
    public ProductDTO productById(@Argument String id) {
        return productService.getProductById(UUID.fromString(id));
    }

    @QueryMapping
    public ProductDTO productByCode(@Argument String code) {
        return productService.getProductByCode(code);
    }

    @QueryMapping
    public List<ProductDTO> allProducts() {
        return productService.getAllProducts();
    }

    @QueryMapping
    public List<ProductDTO> activeProducts() {
        return productService.getActiveProducts();
    }

    @QueryMapping
    public List<ProductDTO> productsByCategory(@Argument String category) {
        return productService.getProductsByCategory(category);
    }

    @QueryMapping
    public List<ProductDTO> productsByBrand(@Argument String brand) {
        return productService.getProductsByBrand(brand);
    }

    @QueryMapping
    public List<ProductDTO> searchProductsByName(@Argument String name) {
        return productService.searchProductsByName(name);
    }

    @MutationMapping
    public ProductDTO createProduct(@Argument("product") ProductInput input) {
        ProductDTO productDTO = new ProductDTO(
                null,                      // UUID (for create)
                input.code(),              // code
                input.name(),              // name
                input.description(),       // description
                input.price(),             // price
                null,                      // createdAt
                null,                      // updatedAt
                true,                      // active (or false, as you wish)
                input.category(),          // category
                input.brand(),             // brand
                input.sku()                // sku
        );

        return productService.createProduct(productDTO);
    }

    @MutationMapping
    public ProductDTO updateProduct(@Argument String id, @Argument("product") ProductInput input) {
        ProductDTO productDTO = new ProductDTO(
                null,                      // UUID (for create)
                input.code(),              // code
                input.name(),              // name
                input.description(),       // description
                input.price(),             // price
                null,                      // createdAt
                null,                      // updatedAt
                true,                      // active (or false, as you wish)
                input.category(),          // category
                input.brand(),             // brand
                input.sku()                // sku
        );
        return productService.updateProduct(UUID.fromString(id), productDTO);
    }

    @MutationMapping
    public boolean deleteProduct(@Argument String id) {
        productService.deleteProduct(UUID.fromString(id));
        return true;
    }

    @MutationMapping
    public ProductDTO activateProduct(@Argument String id) {
        return productService.activateProduct(UUID.fromString(id));
    }

    @MutationMapping
    public ProductDTO deactivateProduct(@Argument String id) {
        return productService.deactivateProduct(UUID.fromString(id));
    }
}