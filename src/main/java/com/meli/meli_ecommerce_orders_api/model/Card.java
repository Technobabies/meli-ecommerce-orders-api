package com.meli.meli_ecommerce_orders_api.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a stored payment card for a user.
 *
 * This class maps to the "cards" table in the database and contains
 * both sensitive and non-sensitive card information. Sensitive data
 * such as the full card number must always be handled securely and
 * only returned to clients in masked form.
 */
@Entity
@Table(name = "cards")
public class Card {

    /**
     * Primary key of the card record.
     * Automatically generated using UUID strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * Identifier of the user who owns this card.
     * Stored as a UUID to match the users table.
     */
    @Column(nullable = false, name = "user_id")
    private UUID userId;

    /**
     * Name of the cardholder as printed on the card.
     */
    @Column(nullable = false, name = "cardholder_name")
    private String cardholderName;

    /**
     * Full card number.
     * WARNING: This value is sensitive and should never be exposed
     * directly in API responses. Masked versions should be used instead.
     */
    @Column(nullable = false, name = "card_number")
    private String cardNumber;

    /**
     * Expiration date of the card (month/year).
     */
    @Column(nullable = false, name = "expiration_date")
    private LocalDate expirationDate;

    /**
     * Timestamp indicating when the card was created.
     * Automatically set at creation and cannot be updated.
     */
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Indicates if this card is set as the default payment method for the user.
     * Only one card per user should be marked as default at a time.
     */
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    /**
     * Soft delete field.
     * If not null, it indicates when the card was logically deleted.
     * The record remains in the database for auditing or recovery.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Default constructor required by JPA.
     */
    public Card() {}

    /**
     * Convenience constructor for creating a card entity.
     * Sets the creation timestamp automatically.
     * New cards are not set as default by default (false).
     *
     * @param userId          Identifier of the user who owns the card.
     * @param cardholderName  Name on the card.
     * @param cardNumber      Full card number.
     * @param expirationDate  Card expiration date.
     */
    public Card(UUID userId, String cardholderName, String cardNumber, LocalDate expirationDate) {
        this.userId = userId;
        this.cardholderName = cardholderName;
        this.cardNumber = cardNumber;
        this.expirationDate = expirationDate;
        this.createdAt = LocalDateTime.now();
        this.isDefault = false;
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getCardholderName() { return cardholderName; }
    public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
