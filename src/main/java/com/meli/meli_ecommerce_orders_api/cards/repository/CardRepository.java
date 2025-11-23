package com.meli.meli_ecommerce_orders_api.cards.repository;

import com.meli.meli_ecommerce_orders_api.cards.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for performing database operations on Card entities.
 *
 * Extends JpaRepository to inherit standard CRUD operations, while also
 * defining custom queries used to enforce business rules and retrieve
 * user-specific card data.
 */
@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    /**
     * Retrieves all active (non-deleted) cards belonging to a specific user.
     *
     * @param userId The UUID of the user.
     * @return A list of Card entities that are not soft-deleted.
     */
    List<Card> findByUserIdAndDeletedAtIsNull(UUID userId);

    /**
     * Checks whether a user already has a specific card number stored.
     * This is used to prevent duplicate card entries.
     *
     * Only non-deleted cards are considered.
     *
     * @param cardNumber Full card number.
     * @param userId     Identifier of the user.
     * @return True if the card already exists for the user, false otherwise.
     */
    boolean existsByCardNumberAndUserIdAndDeletedAtIsNull(String cardNumber, UUID userId);
}

