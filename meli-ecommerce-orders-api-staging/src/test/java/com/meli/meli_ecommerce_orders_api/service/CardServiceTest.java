package com.meli.meli_ecommerce_orders_api.service;

import com.meli.meli_ecommerce_orders_api.dto.CardResponse;
import com.meli.meli_ecommerce_orders_api.dto.CreateCardRequest;
import com.meli.meli_ecommerce_orders_api.exceptions.MaxCardsException;
import com.meli.meli_ecommerce_orders_api.model.Card;
import com.meli.meli_ecommerce_orders_api.repository.CardRepository;
import com.meli.meli_ecommerce_orders_api.service.CardService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardService cardService;

    @Test
    void createCard_shouldThrowMaxCardsException_whenUserHas3Cards() {
        UUID userId = UUID.randomUUID();
        List<Card> mockCards = List.of(
                new Card(userId, "Test1", "1111222233334444", LocalDate.now().plusYears(1)),
                new Card(userId, "Test2", "5555666677778888", LocalDate.now().plusYears(1)),
                new Card(userId, "Test3", "9999000011112222", LocalDate.now().plusYears(1))
        );

        when(cardRepository.findByUserIdAndDeletedAtIsNull(userId)).thenReturn(mockCards);

        CreateCardRequest req = new CreateCardRequest();
        req.setCardholderName("John Doe");
        req.setCardNumber("1234123412341234");
        req.setExpirationDate(LocalDate.now().plusYears(1));

        assertThrows(MaxCardsException.class, () -> cardService.createCard(userId, req));
    }

    @Test
    void createCard_shouldThrowIllegalArgument_whenCardNumberExists() {
        UUID userId = UUID.randomUUID();
        when(cardRepository.findByUserIdAndDeletedAtIsNull(userId)).thenReturn(List.of());
        when(cardRepository.existsByCardNumberAndUserIdAndDeletedAtIsNull("1234123412341234", userId)).thenReturn(true);

        CreateCardRequest req = new CreateCardRequest();
        req.setCardholderName("John Doe");
        req.setCardNumber("1234123412341234");
        req.setExpirationDate(LocalDate.now().plusYears(1));

        assertThrows(IllegalArgumentException.class, () -> cardService.createCard(userId, req));
    }

    @Test
    void createCard_shouldReturnCardResponse_whenValid() {
        UUID userId = UUID.randomUUID();
        when(cardRepository.findByUserIdAndDeletedAtIsNull(userId)).thenReturn(List.of());
        when(cardRepository.existsByCardNumberAndUserIdAndDeletedAtIsNull("1234123412341234", userId)).thenReturn(false);

        CreateCardRequest req = new CreateCardRequest();
        req.setCardholderName("John Doe");
        req.setCardNumber("1234123412341234");
        req.setExpirationDate(LocalDate.now().plusYears(1));

        Card savedCard = new Card(userId, req.getCardholderName(), req.getCardNumber(), req.getExpirationDate());
        savedCard.setId(UUID.randomUUID());
        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);

        CardResponse response = cardService.createCard(userId, req);
        assertEquals("************1234", response.getMaskedCardNumber());
        assertEquals(req.getCardholderName(), response.getCardholderName());
    }

    @Test
    void deleteCard_shouldThrowEntityNotFound_whenCardNotExists() {
        UUID cardId = UUID.randomUUID();
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> cardService.deleteCard(cardId));
    }

    @Test
    void deleteCard_shouldSetDeletedAt_whenCardExists() {
        UUID cardId = UUID.randomUUID();
        Card card = new Card(UUID.randomUUID(), "John", "1111222233334444", LocalDate.now().plusYears(1));
        card.setId(cardId);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        cardService.deleteCard(cardId);

        assertNotNull(card.getDeletedAt());
    }

    @Test
    void updateCard_shouldThrowEntityNotFound_whenCardNotExists() {
        UUID cardId = UUID.randomUUID();
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        CreateCardRequest req = new CreateCardRequest();
        req.setCardholderName("Jane Doe");
        req.setCardNumber("9876543210123456");
        req.setExpirationDate(LocalDate.now().plusYears(2));

        assertThrows(EntityNotFoundException.class, () -> cardService.updateCard(cardId, req));
    }

    @Test
    void updateCard_shouldThrowEntityNotFound_whenCardIsDeleted() {
        UUID cardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Card deletedCard = new Card(userId, "John", "1111222233334444", LocalDate.now().plusYears(1));
        deletedCard.setId(cardId);
        deletedCard.setDeletedAt(LocalDateTime.now());

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(deletedCard));

        CreateCardRequest req = new CreateCardRequest();
        req.setCardholderName("Jane Doe");
        req.setCardNumber("9876543210123456");
        req.setExpirationDate(LocalDate.now().plusYears(2));

        assertThrows(EntityNotFoundException.class, () -> cardService.updateCard(cardId, req));
    }

    @Test
    void updateCard_shouldThrowIllegalArgument_whenNewCardNumberExists() {
        UUID cardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Card card = new Card(userId, "John", "1111222233334444", LocalDate.now().plusYears(1));
        card.setId(cardId);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.existsByCardNumberAndUserIdAndDeletedAtIsNull("9876543210123456", userId)).thenReturn(true);

        CreateCardRequest req = new CreateCardRequest();
        req.setCardholderName("Jane Doe");
        req.setCardNumber("9876543210123456");
        req.setExpirationDate(LocalDate.now().plusYears(2));

        assertThrows(IllegalArgumentException.class, () -> cardService.updateCard(cardId, req));
    }

    @Test
    void updateCard_shouldUpdateAllFields_whenValid() {
        UUID cardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Card card = new Card(userId, "John Doe", "1111222233334444", LocalDate.of(2026, 12, 1));
        card.setId(cardId);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.existsByCardNumberAndUserIdAndDeletedAtIsNull("9876543210123456", userId)).thenReturn(false);

        Card updatedCard = new Card(userId, "Jane Doe", "9876543210123456", LocalDate.of(2027, 6, 15));
        updatedCard.setId(cardId);
        when(cardRepository.save(any(Card.class))).thenReturn(updatedCard);

        CreateCardRequest req = new CreateCardRequest();
        req.setCardholderName("Jane Doe");
        req.setCardNumber("9876543210123456");
        req.setExpirationDate(LocalDate.of(2027, 6, 15));

        CardResponse response = cardService.updateCard(cardId, req);

        assertEquals("Jane Doe", response.getCardholderName());
        assertEquals("************3456", response.getMaskedCardNumber());
        assertEquals(LocalDate.of(2027, 6, 15), response.getExpirationDate());
    }

    @Test
    void updateCard_shouldAllowSameCardNumber_whenUpdatingOtherFields() {
        UUID cardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Card card = new Card(userId, "John Doe", "1111222233334444", LocalDate.of(2026, 12, 1));
        card.setId(cardId);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        // No need to check for duplicates when keeping the same card number

        Card updatedCard = new Card(userId, "John Doe Updated", "1111222233334444", LocalDate.of(2027, 12, 1));
        updatedCard.setId(cardId);
        when(cardRepository.save(any(Card.class))).thenReturn(updatedCard);

        CreateCardRequest req = new CreateCardRequest();
        req.setCardholderName("John Doe Updated");
        req.setCardNumber("1111222233334444"); // Same card number
        req.setExpirationDate(LocalDate.of(2027, 12, 1));

        CardResponse response = cardService.updateCard(cardId, req);

        assertEquals("John Doe Updated", response.getCardholderName());
        assertEquals("************4444", response.getMaskedCardNumber());
    }
}
