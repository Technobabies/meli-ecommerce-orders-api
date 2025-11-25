package com.meli.meli_ecommerce_orders_api.service;

import com.meli.meli_ecommerce_orders_api.dto.CreatePaymentRequest;
import com.meli.meli_ecommerce_orders_api.dto.PaymentResponse;
import com.meli.meli_ecommerce_orders_api.exceptions.CardExpiredException;
import com.meli.meli_ecommerce_orders_api.exceptions.EntityNotFoundException;
import com.meli.meli_ecommerce_orders_api.model.*;
import com.meli.meli_ecommerce_orders_api.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final CardRepository cardRepository;
    private final PaymentRepository paymentRepository;

    public PaymentService(OrderRepository orderRepository,
                        CardRepository cardRepository,
                        PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.cardRepository = cardRepository;
        this.paymentRepository = paymentRepository;
    }

    public PaymentResponse createPayment(UUID userId, CreatePaymentRequest request) {

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        Card card = cardRepository.findById(request.cardId())
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        // --- VALIDACIÓN: tarjeta expirada ---
        if (card.getExpirationDate().isBefore(LocalDate.now())) {
            throw new CardExpiredException();
        }

        Payment payment = new Payment();
        payment.setCreatedBy(userId);
        payment.setOrderId(order.getId());
        payment.setCardId(card.getId());
        payment.setTotalPrice(order.getTotalPrice());
        payment.setStatus(PaymentStatus.APPROVED);

        paymentRepository.save(payment);

        return new PaymentResponse(
                payment.getId(),
                payment.getCreatedBy(),
                payment.getOrderId(),
                payment.getCardId(),
                payment.getTotalPrice(),
                payment.getStatus()
        );
    }
}
