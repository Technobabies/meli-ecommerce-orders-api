package com.meli.meli_ecommerce_orders_api.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a customer's order, which acts as a container for multiple line items.
 * This is the root entity for the order aggregate.
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, name = "created_by")
    private UUID createdBy;

    // --- Relationship: One Order has Many Line Items ---
    // cascade = CascadeType.ALL: Operations (e.g., save) on an Order will automatically
    // apply to its associated line items.
    // orphanRemoval = true: If a line item is removed from this list, it will be
    // deleted from the database upon saving.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference // Annotation to prevent infinite loop of reference
    private List<OrderLineItem> orderItems = new ArrayList<>();

    // The total price of the entire order, calculated from its line items.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    // For service-level soft delete. Null if the order is active.
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime orderDate;

    @UpdateTimestamp
    private LocalDateTime lastUpdatedDate;

    // --- Helper methods to maintain consistency in the relationship ---

    /**
     * Adds a line item to the order and sets the bidirectional link.
     * @param item The OrderLineItem to add.
     */
    public void addOrderItem(OrderLineItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    /**
     * Removes a line item from the order and breaks the bidirectional link.
     * @param item The OrderLineItem to remove.
     */
    public void removeOrderItem(OrderLineItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }

    // --- Getters and Setters ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCreatedBy() { return this.createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public List<OrderLineItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderLineItem> orderItems) { this.orderItems = orderItems; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public LocalDateTime getLastUpdatedDate() { return lastUpdatedDate; }
    public void setLastUpdatedDate(LocalDateTime lastUpdatedDate) { this.lastUpdatedDate = lastUpdatedDate; }
}
