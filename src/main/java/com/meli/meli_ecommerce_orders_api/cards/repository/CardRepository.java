package com.meli.meli_ecommerce_orders_api.cards.repository;

import com.meli.meli_ecommerce_orders_api.cards.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
    List<Card> findByUserIdAndDeletedAtIsNull(UUID userId);
    boolean existsByCardNumberAndUserIdAndDeletedAtIsNull(String cardNumber, UUID userId);
}
