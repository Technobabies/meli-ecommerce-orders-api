package com.meli.meli_ecommerce_orders_api.service;

import com.meli.meli_ecommerce_orders_api.dto.CreateOrderRequest;
import com.meli.meli_ecommerce_orders_api.dto.OrderLineItemRequest;
import com.meli.meli_ecommerce_orders_api.model.Order;
import com.meli.meli_ecommerce_orders_api.model.OrderLineItem;
import com.meli.meli_ecommerce_orders_api.model.OrderStatus;
import com.meli.meli_ecommerce_orders_api.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    /**
     * Instantiates a new Order service.
     *
     * @param orderRepository the order repository
     */
    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Create order order.
     *
     * @param request the request
     * @return the order
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order newOrder = new Order();
        newOrder.setCreatedBy(request.getCreatedBy());
        newOrder.setStatus(OrderStatus.PENDING);
        BigDecimal finalTotalPrice = BigDecimal.ZERO;

        for (OrderLineItemRequest itemRequest : request.getItems()) {
            OrderLineItem lineItem = new OrderLineItem();
            lineItem.setProductId(itemRequest.getProductId());
            lineItem.setProductName(itemRequest.getProductName());
            lineItem.setQuantity(itemRequest.getQuantity());
            lineItem.setPricePerUnit(itemRequest.getPricePerUnit());
            BigDecimal lineItemTotal = lineItem.getPricePerUnit().multiply(new BigDecimal(lineItem.getQuantity()));
            lineItem.setTotalPrice(lineItemTotal);
            finalTotalPrice = finalTotalPrice.add(lineItemTotal);
            newOrder.addOrderItem(lineItem);
        }

        newOrder.setTotalPrice(finalTotalPrice);
        return orderRepository.save(newOrder);
    }

    /**
     * Gets all active orders.
     *
     * @return the all active orders
     */
    public List<Order> getAllActiveOrders() {
        return orderRepository.findByDeletedAtIsNull();
    }

    /**
     * Gets order by id.
     *
     * @param id the id
     * @return the order by id
     */
    public Order getOrderById(UUID id) {
        return orderRepository.findById(id)
                .filter(order -> order.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
    }

    /**
     * Soft delete order order.
     *
     * @param id the id
     * @return the order
     */
    @Transactional
    public Order softDeleteOrder(UUID id) {
        Order orderToDelete = getOrderById(id);
        orderToDelete.setDeletedAt(LocalDateTime.now());
        return orderRepository.save(orderToDelete);
    }
}