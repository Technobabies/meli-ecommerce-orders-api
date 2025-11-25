package com.meli.meli_ecommerce_orders_api.controller;

import com.meli.meli_ecommerce_orders_api.dto.CreatePaymentRequest;
import com.meli.meli_ecommerce_orders_api.dto.PaymentResponse;
import com.meli.meli_ecommerce_orders_api.dto.ApiResponse;
import com.meli.meli_ecommerce_orders_api.service.PaymentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @PathVariable UUID userId,
            @Valid @RequestBody CreatePaymentRequest request) {

        PaymentResponse response = paymentService.createPayment(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Payment created successfully", response));
    }
}

