package com.meli.meli_ecommerce_orders_api.repository;

import com.meli.meli_ecommerce_orders_api.model.Card;
import com.meli.meli_ecommerce_orders_api.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class CardRepositoryTest {

    @Autowired
    private CardRepository cardRepository;

    @Test
    void findByUserIdAndDeletedAtIsNull_shouldReturnOnlyActiveCards() {
        UUID userId = UUID.randomUUID();

        Card active = new Card(userId, "John", "1111222233334444", LocalDate.now().plusYears(1));
        cardRepository.save(active);

        Card deleted = new Card(userId, "Jane", "5555666677778888", LocalDate.now().plusYears(1));
        deleted.setDeletedAt(LocalDateTime.now());
        cardRepository.save(deleted);

        List<Card> result = cardRepository.findByUserIdAndDeletedAtIsNull(userId);
        assertEquals(1, result.size());
    }
}
