package com.meli.meli_ecommerce_orders_api.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateCardRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldFailValidation_whenCardNumberInvalid() {
        CreateCardRequest req = new CreateCardRequest();
        req.setCardholderName("John Doe");
        req.setCardNumber("123"); // invalid
        req.setExpirationDate(LocalDate.now().plusYears(1));

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldPassValidation_whenAllValid() {
        CreateCardRequest req = new CreateCardRequest();
        req.setCardholderName("John Doe");
        req.setCardNumber("1234567812345678");
        req.setExpirationDate(LocalDate.now().plusYears(1));

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }
}
