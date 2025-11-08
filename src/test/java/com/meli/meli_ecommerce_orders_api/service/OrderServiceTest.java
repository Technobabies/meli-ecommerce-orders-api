package com.meli.meli_ecommerce_orders_api.service;

import com.meli.meli_ecommerce_orders_api.dto.CreateOrderRequest;
import com.meli.meli_ecommerce_orders_api.dto.OrderLineItemRequest;
import com.meli.meli_ecommerce_orders_api.model.Order;
import com.meli.meli_ecommerce_orders_api.model.OrderLineItem;
import com.meli.meli_ecommerce_orders_api.model.OrderStatus;
import com.meli.meli_ecommerce_orders_api.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------------------------------------------------------
    // Test createOrder()
    // ---------------------------------------------------------
    @Test
    void testCreateOrder_Success() {
        UUID userId = UUID.randomUUID();

        // Create fake request
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCreatedBy(userId);

        OrderLineItemRequest item1 = new OrderLineItemRequest();
        item1.setProductId(UUID.randomUUID());
        item1.setProductName("Laptop");
        item1.setQuantity(2);
        item1.setPricePerUnit(BigDecimal.valueOf(500));

        OrderLineItemRequest item2 = new OrderLineItemRequest();
        item2.setProductId(UUID.randomUUID());
        item2.setProductName("Mouse");
        item2.setQuantity(1);
        item2.setPricePerUnit(BigDecimal.valueOf(50));

        request.setItems(List.of(item1, item2));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order savedOrder = orderService.createOrder(request);

        assertNotNull(savedOrder);
        assertEquals(userId, savedOrder.getCreatedBy());
        assertEquals(OrderStatus.PENDING, savedOrder.getStatus());
        assertEquals(BigDecimal.valueOf(1050), savedOrder.getTotalPrice());
        assertEquals(2, savedOrder.getOrderItems().size());

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    // ---------------------------------------------------------
    // Test getAllActiveOrders()
    // ---------------------------------------------------------
    @Test
    void testGetAllActiveOrders_ReturnsList() {
        Order order1 = new Order();
        order1.setId(UUID.randomUUID());
        Order order2 = new Order();
        order2.setId(UUID.randomUUID());

        when(orderRepository.findByDeletedAtIsNull()).thenReturn(List.of(order1, order2));

        List<Order> result = orderService.getAllActiveOrders();

        assertEquals(2, result.size());
        verify(orderRepository, times(1)).findByDeletedAtIsNull();
    }

    // ---------------------------------------------------------
    // Test getOrderById()
    // ---------------------------------------------------------
    @Test
    void testGetOrderById_FoundAndActive() {
        UUID id = UUID.randomUUID();
        Order order = new Order();
        order.setId(id);
        order.setDeletedAt(null);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(orderRepository).findById(id);
    }

    @Test
    void testGetOrderById_Deleted_ThrowsException() {
        UUID id = UUID.randomUUID();
        Order order = new Order();
        order.setId(id);
        order.setDeletedAt(LocalDateTime.now());

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        assertThrows(EntityNotFoundException.class, () -> orderService.getOrderById(id));
    }

    @Test
    void testGetOrderById_NotFound_ThrowsException() {
        UUID id = UUID.randomUUID();

        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> orderService.getOrderById(id));
    }

    // ---------------------------------------------------------
    // Test softDeleteOrder()
    // ---------------------------------------------------------
    @Test
    void testSoftDeleteOrder_Success() {
        UUID id = UUID.randomUUID();
        Order order = new Order();
        order.setId(id);
        order.setDeletedAt(null);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order deleted = orderService.softDeleteOrder(id);

        assertNotNull(deleted.getDeletedAt());
        verify(orderRepository).save(order);
    }

    @Test
    void testSoftDeleteOrder_NotFound_ThrowsException() {
        UUID id = UUID.randomUUID();

        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> orderService.softDeleteOrder(id));
    }
}
