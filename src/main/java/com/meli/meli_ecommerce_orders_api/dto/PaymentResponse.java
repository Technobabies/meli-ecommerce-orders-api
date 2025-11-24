package com.meli.meli_ecommerce_orders_api.dto;

import com.meli.meli_ecommerce_orders_api.model.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID createdBy,
        UUID orderId,
        UUID cardId,
        BigDecimal totalPrice,
        PaymentStatus status
) {}
