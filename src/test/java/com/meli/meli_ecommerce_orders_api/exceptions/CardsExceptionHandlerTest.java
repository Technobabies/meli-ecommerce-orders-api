package com.meli.meli_ecommerce_orders_api.exceptions;

import com.meli.meli_ecommerce_orders_api.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class CardsExceptionHandlerTest {

    private CardsExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new CardsExceptionHandler();
    }

    @Test
    void handleMaxCards_ShouldReturnConflictStatusAndErrorMessage() {
        // Arrange
        String errorMessage = "Maximum number of cards exceeded";
        MaxCardsException exception = new MaxCardsException(errorMessage);

        // Act
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleMaxCards(exception);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode(), "Status code should be 409 CONFLICT");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertFalse(response.getBody().isSuccess(), "ApiResponse should indicate failure");
        assertEquals(errorMessage, response.getBody().getMessage(), "Error message should match the exception message");
    }

}
