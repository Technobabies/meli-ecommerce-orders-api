package com.meli.meli_ecommerce_orders_api.cards.service;

import com.meli.meli_ecommerce_orders_api.cards.dto.CardResponse;
import com.meli.meli_ecommerce_orders_api.cards.dto.CreateCardRequest;
import com.meli.meli_ecommerce_orders_api.cards.exceptions.MaxCardsException;
import com.meli.meli_ecommerce_orders_api.cards.model.Card;
import com.meli.meli_ecommerce_orders_api.cards.repository.CardRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class that implements business logic for card management operations.
 * Handles card retrieval, creation, and soft deletion with business rules enforcement.
 */
@Service
public class CardService {

    private final CardRepository cardRepository;

    /**
     * Constructor for CardService.
     * Uses constructor-based dependency injection for the CardRepository.
     *
     * @param cardRepository the repository interface for card data access operations
     */
    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    /**
     * Retrieves all active (non-deleted) cards associated with a specific user.
     * Only returns cards where deletedAt is null, indicating they haven't been soft deleted.
     *
     * @param userId the unique identifier of the user whose cards are to be retrieved
     * @return a list of CardResponse DTOs representing the user's active cards
     */
    public List<CardResponse> getCardsByUserId(UUID userId) {
        return cardRepository.findByUserIdAndDeletedAtIsNull(userId)
                .stream()
                .map(CardResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new card for a specific user with business rule validations.
     * Enforces the following rules:
     * - A user cannot have more than 3 active cards
     * - Card numbers must be unique per user
     *
     * @param userId the unique identifier of the user for whom the card is being created
     * @param req the card creation request containing card details (cardholder name, card number, expiration date)
     * @return a CardResponse DTO representing the newly created card
     * @throws MaxCardsException if the user already has 3 active cards
     * @throws IllegalArgumentException if the card number already exists for this user
     */
    @Transactional
    public CardResponse createCard(UUID userId, CreateCardRequest req) {
        // Check if user has reached the maximum limit of 3 active cards
        List<Card> activeCards = cardRepository.findByUserIdAndDeletedAtIsNull(userId);
        if (activeCards.size() >= 3) {
            throw new MaxCardsException("User has reached the maximum limit of 3 cards.");
        }

        // Validate that the card number is unique for this user
        if (cardRepository.existsByCardNumberAndUserIdAndDeletedAtIsNull(req.getCardNumber(), userId)) {
            throw new IllegalArgumentException("Card number already exists for this user.");
        }

        // Create and persist the new card
        Card newCard = new Card(userId, req.getCardholderName(), req.getCardNumber(), req.getExpirationDate());
        Card savedCard = cardRepository.save(newCard);

        return CardResponse.fromEntity(savedCard);
    }

    /**
     * Performs a soft delete on a card by setting its deletedAt timestamp.
     * Only cards that haven't been previously deleted can be deleted.
     * This operation is idempotent - attempting to delete an already deleted card will throw an exception.
     *
     * @param cardId the unique identifier of the card to be deleted
     * @throws EntityNotFoundException if the card doesn't exist or has already been deleted
     */
    @Transactional
    public void deleteCard(UUID cardId) {
        // Find the card and ensure it hasn't been deleted already
        Card card = cardRepository.findById(cardId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Card not found with ID: " + cardId));

        // Perform soft delete by setting the deletedAt timestamp
        card.setDeletedAt(LocalDateTime.now());
        cardRepository.save(card);
    }
}