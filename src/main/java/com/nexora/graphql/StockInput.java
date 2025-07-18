package com.nexora.graphql;

/**
 * Input type for creating and updating stocks in GraphQL.
 * Immutable record representing stock input data.
 */
public record StockInput(
        String productId,
        String warehouseId,
        Integer quantity,
        Integer minStockLevel,
        Integer maxStockLevel
) {
}
