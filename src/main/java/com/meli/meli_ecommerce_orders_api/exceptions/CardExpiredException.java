package com.meli.meli_ecommerce_orders_api.exceptions;

public class CardExpiredException extends RuntimeException {
    public CardExpiredException() {
        super("La tarjeta elegida ya está expirada.");
    }
}
