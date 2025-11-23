package com.meli.meli_ecommerce_orders_api.dto;

import com.meli.meli_ecommerce_orders_api.model.Card;
import java.time.LocalDate;
import java.util.UUID;

public class CardResponse {
    private UUID id;
    private String cardholderName;
    private String maskedCardNumber;
    private LocalDate expirationDate;

    public static CardResponse fromEntity(Card card) {
        CardResponse response = new CardResponse();
        response.id = card.getId();
        response.cardholderName = card.getCardholderName();
        response.expirationDate = card.getExpirationDate();

        String fullNum = card.getCardNumber();
        if (fullNum == null) {
            response.maskedCardNumber = null;
        } else if (fullNum.length() <= 4) {
            response.maskedCardNumber = fullNum;
        } else {
            response.maskedCardNumber = "************" + fullNum.substring(fullNum.length() - 4);
        }

        return response;
    }

    public UUID getId() { return id; }
    public String getCardholderName() { return cardholderName; }
    public String getMaskedCardNumber() { return maskedCardNumber; }
    public LocalDate getExpirationDate() { return expirationDate; }
}
