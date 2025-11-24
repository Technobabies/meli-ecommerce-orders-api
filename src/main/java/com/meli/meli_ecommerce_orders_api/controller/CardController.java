package com.meli.meli_ecommerce_orders_api.controller;

import com.meli.meli_ecommerce_orders_api.dto.CardResponse;
import com.meli.meli_ecommerce_orders_api.dto.CreateCardRequest;
import com.meli.meli_ecommerce_orders_api.dto.UpdateCardRequest;
import com.meli.meli_ecommerce_orders_api.service.CardService;
import com.meli.meli_ecommerce_orders_api.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
@Tag(name = "Cards Management", description = "APIs for creating, reading, updating, and deleting cards")
public class CardController {

    // Injected service responsible for card-related business logic
    private final CardService cardService;

    @Autowired
    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    /**
     * Retrieves all cards belonging to a specific user.
     *
     * @param userId The UUID of the user whose cards should be fetched.
     * @return A ResponseEntity containing an ApiResponse with a list of CardResponse objects.
     */
    @GetMapping("/{userId}")
    @Operation(summary = "List of cards by user", description = "Obtain the list of cards associated to a user by its id.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List of card retrieve successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<ApiResponse<List<CardResponse>>> getCards(@PathVariable UUID userId) {
        List<CardResponse> cards = cardService.getCardsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Cards fetched successfully", cards));
    }

    /**
     * Creates a new card for a specific user.
     *
     * @param userId The UUID of the user to whom the new card will belong.
     * @param request A validated CreateCardRequest containing card details.
     * @return A ResponseEntity containing an ApiResponse with the created CardResponse.
     */
    @PostMapping("/{userId}")
    @Operation(summary = "Create a card for a user", description = "Create a cards to be associated to a user by its id.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Card created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<ApiResponse<CardResponse>> createCard(
            @PathVariable UUID userId,
            @Valid @RequestBody CreateCardRequest request) {

        CardResponse createdCard = cardService.createCard(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Card created successfully", createdCard));
    }

    /**
     * Deletes an existing card by its ID.
     *
     * @param cardId The UUID of the card to delete.
     * @return A ResponseEntity containing a success message with no data.
     */
    @DeleteMapping("/{cardId}")
    @Operation(summary = "Delete a card of a user", description = "Soft delete a cards associated to a user by cardId.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Card deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<ApiResponse<Void>> deleteCard(@PathVariable UUID cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok(ApiResponse.success("Card deleted successfully", null));
    }

    /**
     * Updates an existing card identified by its ID using the data provided in the request body.
     *
     * <p>This endpoint receives a {@link UpdateCardRequest} object containing the
     * fields to update, validates it, and delegates the update operation to the
     * card service. If the update is successful, it returns an {@link ApiResponse}
     * wrapping the updated card information.</p>
     *
     * @param cardId the UUID of the card to update
     * @param request the validated request body containing the card fields to update
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} with the updated card data
     */
    @Operation(
            summary = "Update a card",
            description = "Updates the cardholder name and expiration date of an existing card."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Card updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Card not found")
    })
    @PutMapping("/{cardId}")
    public ResponseEntity<ApiResponse<CardResponse>> updateCard(
            @PathVariable UUID cardId,
            @Valid @RequestBody UpdateCardRequest request) {

        CardResponse updatedCard = cardService.updateCard(cardId, request);
        return ResponseEntity.ok(ApiResponse.success("Card updated successfully", updatedCard));
    }


}
