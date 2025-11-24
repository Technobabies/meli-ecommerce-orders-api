package com.meli.meli_ecommerce_orders_api.model;

/**
 * Represents the possible statuses of an Order.
 * Using an Enum ensures type safety and restricts the status to valid values.
 */
public enum OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

