package com.meli.meli_ecommerce_orders_api.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UpdateCardRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldFailValidation_whenExpirationDateIsPast() {
        UpdateCardRequest req = new UpdateCardRequest();
        req.setCardholderName("John Doe");
        req.setExpirationDate(LocalDate.now().minusDays(1));

        var violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldPassValidation_whenInputIsValid() {
        UpdateCardRequest req = new UpdateCardRequest();
        req.setCardholderName("John Doe");
        req.setExpirationDate(LocalDate.now().plusYears(1));

        var violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }
}
