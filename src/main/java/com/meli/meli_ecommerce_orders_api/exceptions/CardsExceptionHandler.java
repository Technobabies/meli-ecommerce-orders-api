package com.meli.meli_ecommerce_orders_api.exceptions;

import com.meli.meli_ecommerce_orders_api.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.meli.meli_ecommerce_orders_api.cards")
public class CardsExceptionHandler {

    @ExceptionHandler(MaxCardsException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxCards(MaxCardsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(ex.getMessage()));
    }
}
