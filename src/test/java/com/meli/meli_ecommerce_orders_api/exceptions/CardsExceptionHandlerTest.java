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
    void handleMaxCards_ShouldReturnBadRequestStatusAndErrorMessage() {
        // Arrange
        String errorMessage = "Maximum number of cards exceeded";
        MaxCardsException exception = new MaxCardsException(errorMessage);

        // Act
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleMaxCards(exception);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status code should be 400 BAD_REQUEST");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertFalse(response.getBody().isSuccess(), "ApiResponse should indicate failure");
        assertEquals(errorMessage, response.getBody().getMessage(), "Error message should match the exception message");
    }

    @Test
    void handleIllegalArgument_ShouldReturnBadRequestStatusAndErrorMessage() {
        // Arrange
        String errorMessage = "Card number already exists for this user.";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        // Act
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleIllegalArgument(exception);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status code should be 400 BAD_REQUEST");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertFalse(response.getBody().isSuccess(), "ApiResponse should indicate failure");
        assertEquals(errorMessage, response.getBody().getMessage(), "Error message should match the exception message");
    }

}
