package com.meli.meli_ecommerce_orders_api.controller;

import com.meli.meli_ecommerce_orders_api.dto.CardResponse;
import com.meli.meli_ecommerce_orders_api.service.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @Test
    void getCards_shouldReturn200() throws Exception {
        UUID userId = UUID.randomUUID();
        when(cardService.getCardsByUserId(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/cards/" + userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateCard_shouldReturn200() throws Exception {
        UUID cardId = UUID.randomUUID();

        CardResponse mockResponse = new CardResponse();
        when(cardService.updateCard(eq(cardId), any())).thenReturn(mockResponse);

        mockMvc.perform(put("/api/v1/cards/" + cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "cardholderName": "Updated Name",
                          "expirationDate": "2030-01-01"
                        }
                    """))
                .andExpect(status().isOk());
    }


}
