package com.meli.meli_ecommerce_orders_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meli.meli_ecommerce_orders_api.dto.CreateOrderRequest;
import com.meli.meli_ecommerce_orders_api.dto.OrderLineItemRequest;
import com.meli.meli_ecommerce_orders_api.model.Order;
import com.meli.meli_ecommerce_orders_api.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @TestConfiguration
    static class ControllerTestConfig {
        @Bean
        public OrderService orderService() {
            return Mockito.mock(OrderService.class);
        }
    }

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    @Autowired
    OrderControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, OrderService orderService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.orderService = orderService;
    }

    private CreateOrderRequest validRequest;

    @BeforeEach
    void setUp() {
        OrderLineItemRequest item = new OrderLineItemRequest();
        item.setProductId(UUID.randomUUID());
        item.setProductName("Test Product");
        item.setQuantity(2);
        item.setPricePerUnit(BigDecimal.valueOf(10.0));

        validRequest = new CreateOrderRequest();
        validRequest.setCreatedBy(UUID.randomUUID());
        validRequest.setItems(List.of(item));
    }

    @Test
    void testCreateOrder_Success() throws Exception {
        Order createdOrder = new Order();
        createdOrder.setId(UUID.randomUUID());
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(createdOrder);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                // CORRECCIÓN: Ahora accedemos a $.data.id porque usas ApiResponse
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.success", is(true)));

        verify(orderService, times(1)).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    void testCreateOrder_Failure_InvalidInput() throws Exception {
        CreateOrderRequest invalid = new CreateOrderRequest(); // Falta createdBy e items
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void testGetAllOrders_Success() throws Exception {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        when(orderService.getAllActiveOrders()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                // CORRECCIÓN: La lista está dentro de $.data
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(order.getId().toString())));
    }

    @Test
    void testGetAllOrders_EmptyList() throws Exception {
        when(orderService.getAllActiveOrders()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                // CORRECCIÓN: La lista vacía está en $.data
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void testGetOrderById_Success() throws Exception {
        UUID id = UUID.randomUUID();
        Order order = new Order();
        order.setId(id);
        when(orderService.getOrderById(id)).thenReturn(order);

        mockMvc.perform(get("/api/v1/orders/{id}", id))
                .andExpect(status().isOk())
                // CORRECCIÓN: Accedemos a $.data.id
                .andExpect(jsonPath("$.data.id", is(id.toString())));
    }

    @Test
    void testGetOrderById_NotFound() throws Exception {
        UUID id = UUID.randomUUID();
        // CORRECCIÓN: Usamos EntityNotFoundException que es lo que captura tu GlobalExceptionHandler para devolver 404
        when(orderService.getOrderById(id)).thenThrow(new EntityNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/orders/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void testDeleteOrder_Success() throws Exception {
        UUID id = UUID.randomUUID();

        // El servicio es void
        Mockito.doAnswer(invocation -> null).when(orderService).softDeleteOrder(id);

        mockMvc.perform(delete("/api/v1/orders/{id}", id))
                // CORRECCIÓN: Tu controlador devuelve 200 OK con ApiResponse, no 204 No Content
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        verify(orderService, times(1)).softDeleteOrder(id);
    }

    @Test
    void testDeleteOrder_NotFound() throws Exception {
        UUID id = UUID.randomUUID();

        // CORRECCIÓN: Usar EntityNotFoundException para disparar el 404
        Mockito.doThrow(new EntityNotFoundException("Not found"))
                .when(orderService).softDeleteOrder(id);

        mockMvc.perform(delete("/api/v1/orders/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));

        verify(orderService, times(1)).softDeleteOrder(id);
    }
}