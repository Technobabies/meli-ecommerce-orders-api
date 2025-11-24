package com.meli.meli_ecommerce_orders_api.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO representing a single item to be included in a new order request.
 * This class is used to transfer data from the client to the service layer.
 */
public class OrderLineItemRequest {
    private UUID productId;
    private String productName;
    private int quantity;
    private BigDecimal pricePerUnit;

    /**
     * Gets product id.
     *
     * @return the product id
     */
// Getters and Setters for all fields are required for JSON deserialization
    public UUID getProductId() { return productId; }

    /**
     * Sets product id.
     *
     * @param productId the product id
     */
    public void setProductId(UUID productId) { this.productId = productId; }

    /**
     * Gets product name.
     *
     * @return the product name
     */
    public String getProductName() { return productName; }

    /**
     * Sets product name.
     *
     * @param productName the product name
     */
    public void setProductName(String productName) { this.productName = productName; }

    /**
     * Gets quantity.
     *
     * @return the quantity
     */
    public int getQuantity() { return quantity; }

    /**
     * Sets quantity.
     *
     * @param quantity the quantity
     */
    public void setQuantity(int quantity) { this.quantity = quantity; }

    /**
     * Gets price per unit.
     *
     * @return the price per unit
     */
    public BigDecimal getPricePerUnit() { return pricePerUnit; }

    /**
     * Sets price per unit.
     *
     * @param pricePerUnit the price per unit
     */
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }
}
