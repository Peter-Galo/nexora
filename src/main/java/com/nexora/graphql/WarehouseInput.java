package com.nexora.graphql;

/**
 * Input type for creating and updating warehouses in GraphQL.
 * Immutable record representing warehouse input data.
 */
public record WarehouseInput(
        String code,
        String name,
        String description,
        String address,
        String city,
        String stateProvince,
        String postalCode,
        String country
) {
}
