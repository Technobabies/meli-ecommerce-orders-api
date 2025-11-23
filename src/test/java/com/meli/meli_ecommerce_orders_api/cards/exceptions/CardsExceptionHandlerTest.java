package com.meli.meli_ecommerce_orders_api.cards.exceptions;

import com.meli.meli_ecommerce_orders_api.cards.controller.CardController;
import com.meli.meli_ecommerce_orders_api.cards.service.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
class CardsExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @Test
    void shouldReturn409_whenMaxCardsExceptionThrown() throws Exception {
        UUID userId = UUID.randomUUID();

        when(cardService.getCardsByUserId(userId))
                .thenThrow(new MaxCardsException("Limit reached"));

        mockMvc.perform(get("/api/v1/cards/" + userId))
                .andExpect(status().isConflict());
    }
}
