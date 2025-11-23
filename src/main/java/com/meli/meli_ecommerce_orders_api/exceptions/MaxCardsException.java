package com.meli.meli_ecommerce_orders_api.exceptions;

/**
 * Custom exception thrown when a user attempts to add more cards
 * than the system allows.
 *
 * This exception represents a business rule violation rather than
 * a technical error, and it is typically handled by a global
 * exception handler to return an appropriate API response.
 */
public class MaxCardsException extends RuntimeException {

    /**
     * Creates a new MaxCardsException with the given error message.
     *
     * @param message Descriptive message explaining why the exception occurred.
     */
    public MaxCardsException(String message) {
        super(message);
    }
}
