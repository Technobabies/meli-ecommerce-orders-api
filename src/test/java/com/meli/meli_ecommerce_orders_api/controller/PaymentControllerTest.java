package com.meli.meli_ecommerce_orders_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meli.meli_ecommerce_orders_api.dto.CreatePaymentRequest;
import com.meli.meli_ecommerce_orders_api.dto.PaymentResponse;
import com.meli.meli_ecommerce_orders_api.model.PaymentStatus;
import com.meli.meli_ecommerce_orders_api.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Servicio mockeado
    @MockBean
    private PaymentService paymentService;

    // Caso válido: servicio devuelve PaymentResponse y el controller responde 201
    @Test
    void shouldCreatePaymentWhenRequestIsValid() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(orderId, cardId);

        PaymentResponse fakeResponse = new PaymentResponse(
                UUID.randomUUID(),          // id del pago
                userId,                     // createdBy
                orderId,
                cardId,
                new BigDecimal("500.00"),   // totalPrice
                PaymentStatus.APPROVED
        );

        // Cuando el controller llame al servicio, devolvemos este fakeResponse
        when(paymentService.createPayment(eq(userId), any(CreatePaymentRequest.class)))
                .thenReturn(fakeResponse);

        mockMvc.perform(post("/api/v1/payments/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment created successfully"))
                .andExpect(jsonPath("$.data.id").value(fakeResponse.id().toString()))
                .andExpect(jsonPath("$.data.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.data.cardId").value(cardId.toString()))
                // 👇 aquí la corrección: comparar como número, no como string
                .andExpect(jsonPath("$.data.totalPrice").value(500.0))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    // Falta orderId → 400 Bad Request
    @Test
    void shouldReturnBadRequestWhenOrderIdIsMissing() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        String body = """
                {
                  "cardId": "%s"
                }
                """.formatted(cardId);

        mockMvc.perform(post("/api/v1/payments/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // Falta cardId → 400 Bad Request
    @Test
    void shouldReturnBadRequestWhenCardIdIsMissing() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        String body = """
                {
                  "orderId": "%s"
                }
                """.formatted(orderId);

        mockMvc.perform(post("/api/v1/payments/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
