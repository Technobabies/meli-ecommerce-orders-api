package com.meli.meli_ecommerce_orders_api.service;

import com.meli.meli_ecommerce_orders_api.dto.CardResponse;
import com.meli.meli_ecommerce_orders_api.dto.CreateCardRequest;
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

@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public List<CardResponse> getCardsByUserId(UUID userId) {
        return cardRepository.findByUserIdAndDeletedAtIsNull(userId)
                .stream()
                .map(CardResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CardResponse createCard(UUID userId, CreateCardRequest req) {
        List<Card> activeCards = cardRepository.findByUserIdAndDeletedAtIsNull(userId);
        if (activeCards.size() >= 3) {
            throw new MaxCardsException("User has reached the maximum limit of 3 cards.");
        }

        if (cardRepository.existsByCardNumberAndUserIdAndDeletedAtIsNull(req.getCardNumber(), userId)) {
            throw new IllegalArgumentException("Card number already exists for this user.");
        }

        Card newCard = new Card(userId, req.getCardholderName(), req.getCardNumber(), req.getExpirationDate());
        Card savedCard = cardRepository.save(newCard);

        return CardResponse.fromEntity(savedCard);
    }

    @Transactional
    public void deleteCard(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Card not found with ID: " + cardId));

        card.setDeletedAt(LocalDateTime.now());
        cardRepository.save(card);
    }
}
