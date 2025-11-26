package com.meli.meli_ecommerce_orders_api.service;

import com.meli.meli_ecommerce_orders_api.dto.CardResponse;
import com.meli.meli_ecommerce_orders_api.dto.CreateCardRequest;
import com.meli.meli_ecommerce_orders_api.dto.UpdateCardRequest;
import com.meli.meli_ecommerce_orders_api.exceptions.MaxCardsException;
import com.meli.meli_ecommerce_orders_api.model.Card;
import com.meli.meli_ecommerce_orders_api.repository.CardRepository;
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
     * Retrieves a single card by its ID.
     * Only returns active (non-deleted) cards.
     *
     * @param cardId the unique identifier of the card to retrieve
     * @return a CardResponse DTO representing the card
     * @throws EntityNotFoundException if the card doesn't exist or has been deleted
     */
    public CardResponse getCardById(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Card not found with ID: " + cardId));
        
        return CardResponse.fromEntity(card);
    }

    /**
     * Sets a specific card as the default payment method for a user.
     * Automatically removes the default status from any other cards belonging to the same user.
     * Only one card per user can be marked as default at a time.
     *
     * @param cardId the unique identifier of the card to set as default
     * @return a CardResponse DTO representing the updated card
     * @throws EntityNotFoundException if the card doesn't exist or has been deleted
     */
    @Transactional
    public CardResponse setDefaultCard(UUID cardId) {
        // Find the card and ensure it exists and hasn't been deleted
        Card card = cardRepository.findById(cardId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Card not found with ID: " + cardId));

        // Remove default status from all other cards belonging to this user
        List<Card> userCards = cardRepository.findByUserIdAndDeletedAtIsNull(card.getUserId());
        for (Card userCard : userCards) {
            if (!userCard.getId().equals(cardId) && userCard.getIsDefault()) {
                userCard.setIsDefault(false);
                cardRepository.save(userCard);
            }
        }

        // Set the selected card as default
        card.setIsDefault(true);
        Card updatedCard = cardRepository.save(card);

        return CardResponse.fromEntity(updatedCard);
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

    /**
     * Updates an existing card with the provided information.
     *
     * <p>This method retrieves a card by its ID, ensuring that it has not been
     * soft-deleted (i.e., {@code deletedAt} is {@code null}). If the card exists,
     * it updates its cardholder name and expiration date using the values provided
     * in the {@link UpdateCardRequest}. The updated entity is then saved to the
     * repository and returned as a {@link CardResponse}.</p>
     *
     * @param cardId the unique identifier of the card to update
     * @param req the request object containing the new cardholder name and expiration date
     * @return a {@link CardResponse} representing the updated card
     * @throws EntityNotFoundException if no active card is found with the given ID
     */
    @Transactional
    public CardResponse updateCard(UUID cardId, UpdateCardRequest req) {
        Card card = cardRepository.findById(cardId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Card not found with ID: " + cardId));

        card.setCardholderName(req.getCardholderName());
        card.setExpirationDate(req.getExpirationDate());

        Card updatedCard = cardRepository.save(card);
        return CardResponse.fromEntity(updatedCard);
    }


}