package com.meli.meli_ecommerce_orders_api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Request DTO used for updating an existing card.
 *
 * <p>This class contains the fields that can be modified for a card, along with
 * validation constraints to ensure the provided data is valid. It is typically
 * used in update operations where the user wants to change the cardholder name
 * or the card's expiration date.</p>
 */
public class UpdateCardRequest {

    /**
     * The full name of the cardholder.
     *
     * <p>This field cannot be blank and must contain at least one
     * non-whitespace character.</p>
     */
    @NotBlank(message = "Cardholder name is required")
    private String cardholderName;

    /**
     * The date when the card expires.
     *
     * <p>This field is required and must represent a date in the future.</p>
     */
    @NotNull(message = "Expiration date is required")
    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;

    /** @return the cardholder's name */
    public String getCardholderName() { return cardholderName; }

    /**
     * Sets the cardholder's name.
     *
     * @param cardholderName the new cardholder name
     */
    public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }

    /** @return the expiration date of the card */
    public LocalDate getExpirationDate() { return expirationDate; }

    /**
     * Sets the card's expiration date.
     *
     * @param expirationDate the new expiration date, which must be in the future
     */
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
}
