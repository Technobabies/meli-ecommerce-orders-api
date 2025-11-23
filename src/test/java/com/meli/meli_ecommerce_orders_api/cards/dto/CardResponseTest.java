package com.meli.meli_ecommerce_orders_api.cards.dto;

import com.meli.meli_ecommerce_orders_api.cards.model.Card;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CardResponseTest {

    @Test
    void fromEntity_shouldMaskCardNumberCorrectly() {
        Card card = new Card(UUID.randomUUID(), "John Doe", "1234567890123456",
                LocalDate.now().plusYears(1));

        CardResponse res = CardResponse.fromEntity(card);

        assertEquals("************3456", res.getMaskedCardNumber());
        assertEquals("John Doe", res.getCardholderName());
    }
}
