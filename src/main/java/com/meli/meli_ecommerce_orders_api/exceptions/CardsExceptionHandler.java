package com.meli.meli_ecommerce_orders_api.exceptions;

import com.meli.meli_ecommerce_orders_api.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for the "cards" package.
 *
 * This class intercepts exceptions thrown within controllers under the specified
 * package and converts them into unified API responses, improving error handling
 * consistency for card-related operations.
 */
@RestControllerAdvice(basePackages = "com.meli.meli_ecommerce_orders_api.cards")
public class CardsExceptionHandler {

    /**
     * Handles cases where the user exceeds the allowed number of stored cards.
     *
     * When a MaxCardsException is thrown, this method returns a standardized
     * error response with HTTP 409 (Conflict), indicating that the operation
     * cannot be completed due to a business rule violation.
     *
     * @param ex The MaxCardsException thrown by the service or controller.
     * @return A ResponseEntity containing an ApiResponse with an error message.
     */
    @ExceptionHandler(MaxCardsException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxCards(MaxCardsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(ex.getMessage()));
    }
}
