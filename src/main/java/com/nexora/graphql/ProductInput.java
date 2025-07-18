package com.nexora.graphql;

import java.math.BigDecimal;

/**
 * Input type for creating and updating products in GraphQL.
 */
public record ProductInput(
        String code,
        String name,
        String description,
        BigDecimal price,
        String category,
        String brand,
        String sku
) {
}