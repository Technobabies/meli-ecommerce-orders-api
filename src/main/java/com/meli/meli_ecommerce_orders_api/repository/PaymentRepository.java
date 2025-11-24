package com.meli.meli_ecommerce_orders_api.repository;

import com.meli.meli_ecommerce_orders_api.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
