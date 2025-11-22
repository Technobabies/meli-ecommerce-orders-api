package com.meli.meli_ecommerce_orders_api.cards.exceptions;

public class MaxCardsException extends RuntimeException {
    public MaxCardsException(String message) {
        super(message);
    }
}
