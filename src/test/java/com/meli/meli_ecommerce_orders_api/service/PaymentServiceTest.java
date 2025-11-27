package com.meli.meli_ecommerce_orders_api.service;

import com.meli.meli_ecommerce_orders_api.dto.CreatePaymentRequest;
import com.meli.meli_ecommerce_orders_api.dto.PaymentResponse;
import com.meli.meli_ecommerce_orders_api.exceptions.CardExpiredException;
import com.meli.meli_ecommerce_orders_api.exceptions.EntityNotFoundException;
import com.meli.meli_ecommerce_orders_api.model.Card;
import com.meli.meli_ecommerce_orders_api.model.Order;
import com.meli.meli_ecommerce_orders_api.model.Payment;
import com.meli.meli_ecommerce_orders_api.model.PaymentStatus;
import com.meli.meli_ecommerce_orders_api.repository.CardRepository;
import com.meli.meli_ecommerce_orders_api.repository.OrderRepository;
import com.meli.meli_ecommerce_orders_api.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    // Orden y tarjeta válidas → Payment APPROVED
    @Test
    void shouldCreateApprovedPaymentWhenOrderAndCardAreValid() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(orderId, cardId);

        Order order = new Order();
        order.setId(orderId);
        order.setTotalPrice(new BigDecimal("500.00"));

        Card card = new Card();
        card.setId(cardId);
        card.setExpirationDate(LocalDate.now().plusDays(10)); // no expirada

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.createPayment(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.cardId()).isEqualTo(cardId);
        assertThat(response.totalPrice()).isEqualByComparingTo("500.00");
        assertThat(response.status()).isEqualTo(PaymentStatus.APPROVED);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();

        assertThat(savedPayment.getCreatedBy()).isEqualTo(userId);
        assertThat(savedPayment.getOrderId()).isEqualTo(orderId);
        assertThat(savedPayment.getCardId()).isEqualTo(cardId);
        assertThat(savedPayment.getTotalPrice()).isEqualByComparingTo("500.00");
        assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
    }

    // Orden no existe → EntityNotFoundException("Order not found")
    @Test
    void shouldThrowEntityNotFoundWhenOrderDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(orderId, cardId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> paymentService.createPayment(userId, request));

        assertThat(exception.getMessage()).isEqualTo("Order not found");
        verifyNoInteractions(cardRepository, paymentRepository);
    }

    // Tarjeta no existe → EntityNotFoundException("Card not found")
    @Test
    void shouldThrowEntityNotFoundWhenCardDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(orderId, cardId);

        Order order = new Order();
        order.setId(orderId);
        order.setTotalPrice(new BigDecimal("500.00"));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> paymentService.createPayment(userId, request));

        assertThat(exception.getMessage()).isEqualTo("Card not found");
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    // expirada → CardExpiredException
    @Test
    void shouldThrowCardExpiredExceptionWhenCardIsExpired() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(orderId, cardId);

        Order order = new Order();
        order.setId(orderId);
        order.setTotalPrice(new BigDecimal("500.00"));

        Card card = new Card();
        card.setId(cardId);
        card.setExpirationDate(LocalDate.now().minusDays(1)); // expirada

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThrows(CardExpiredException.class, () -> paymentService.createPayment(userId, request));

        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
