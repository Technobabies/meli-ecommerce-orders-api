package com.meli.meli_ecommerce_orders_api.dto;

import com.meli.meli_ecommerce_orders_api.model.Card;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO representing the information returned to the client when querying card data.
 * Sensitive fields such as the full card number are masked for security reasons.
 */
public class CardResponse {

    // Unique identifier of the card
    private UUID id;

    // Name of the cardholder
    private String cardholderName;

    // Masked version of the card number (only last 4 digits visible)
    private String maskedCardNumber;

    // Expiration date of the card
    private LocalDate expirationDate;

    /**
     * Converts a Card entity into a CardResponse DTO.
     * The method also masks the card number to avoid exposing sensitive information.
     *
     * Masking logic:
     * - If the card number is null → maskedCardNumber = null
     * - If it has 4 or fewer digits → returned as is (assumed non-sensitive)
     * - Otherwise → mask all digits except the last 4
     *
     * @param card The Card entity to convert.
     * @return The corresponding CardResponse DTO.
     */
    public static CardResponse fromEntity(Card card) {
        CardResponse response = new CardResponse();

        // Basic field mapping
        response.id = card.getId();
        response.cardholderName = card.getCardholderName();
        response.expirationDate = card.getExpirationDate();

        // Masking logic for card number
        String fullNum = card.getCardNumber();

        if (fullNum == null) {
            response.maskedCardNumber = null;
        } else if (fullNum.length() <= 4) {
            // If the card number is too short, return it as is
            response.maskedCardNumber = fullNum;
        } else {
            // Mask all digits except the last four
            response.maskedCardNumber = "************" + fullNum.substring(fullNum.length() - 4);
        }

        return response;
    }

    // Getters
    public UUID getId() { return id; }
    public String getCardholderName() { return cardholderName; }
    public String getMaskedCardNumber() { return maskedCardNumber; }
    public LocalDate getExpirationDate() { return expirationDate; }
}
