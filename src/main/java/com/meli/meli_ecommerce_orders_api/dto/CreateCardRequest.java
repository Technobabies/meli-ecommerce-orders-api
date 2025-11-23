package com.meli.meli_ecommerce_orders_api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * DTO representing the data required to create a new credit/debit card.
 * This class includes validation annotations to ensure data integrity
 * before reaching the service layer.
 */
public class CreateCardRequest {

    /**
     * Name of the cardholder.
     * Cannot be null or empty.
     */
    @NotBlank(message = "Cardholder name is required")
    private String cardholderName;

    /**
     * Full card number.
     * Must contain exactly 16 digits and no other characters.
     * This value will be stored securely and masked when returned.
     */
    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{16}$", message = "Card number must be exactly 16 digits")
    private String cardNumber;

    /**
     * Expiration date of the card.
     * Must be provided and must represent a future date.
     */
    @NotNull(message = "Expiration date is required")
    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;

    // Getters and setters
    public String getCardholderName() { return cardholderName; }
    public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
}
