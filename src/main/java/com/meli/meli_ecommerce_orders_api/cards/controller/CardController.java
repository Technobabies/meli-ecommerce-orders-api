package com.meli.meli_ecommerce_orders_api.cards.controller;

import com.meli.meli_ecommerce_orders_api.cards.dto.CardResponse;
import com.meli.meli_ecommerce_orders_api.cards.dto.CreateCardRequest;
import com.meli.meli_ecommerce_orders_api.cards.service.CardService;
import com.meli.meli_ecommerce_orders_api.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<CardResponse>>> getCards(@PathVariable UUID userId) {
        List<CardResponse> cards = cardService.getCardsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Cards fetched successfully", cards));
    }

    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse<CardResponse>> createCard(
            @PathVariable UUID userId,
            @Valid @RequestBody CreateCardRequest request) {

        CardResponse createdCard = cardService.createCard(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Card created successfully", createdCard));
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<ApiResponse<Void>> deleteCard(@PathVariable UUID cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok(ApiResponse.success("Card deleted successfully", null));
    }
}
