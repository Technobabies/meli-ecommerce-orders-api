package com.meli.meli_ecommerce_orders_api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meli.meli_ecommerce_orders_api.dto.CreateCardRequest;
import com.meli.meli_ecommerce_orders_api.model.Card;
import com.meli.meli_ecommerce_orders_api.repository.CardRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Cards API endpoints.
 * These tests validate the complete flow from HTTP request to database operations.
 * Uses H2 in-memory database for testing.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Card API Integration Tests")
class CardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testUserId;
    private CreateCardRequest validCardRequest;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        cardRepository.deleteAll();
        
        // Setup test data
        testUserId = UUID.randomUUID();
        validCardRequest = new CreateCardRequest();
        validCardRequest.setCardholderName("John Doe");
        validCardRequest.setCardNumber("4532015112830366");
        validCardRequest.setExpirationDate(LocalDate.now().plusYears(2));
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        cardRepository.deleteAll();
    }

    // ========== CREATE CARD TESTS ==========

    @Test
    @DisplayName("POST /api/v1/cards/{userId} - Should create card successfully")
    void createCard_Success() throws Exception {
        // When: Create a new card
        MvcResult result = mockMvc.perform(post("/api/v1/cards/" + testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCardRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Card created successfully"))
                .andExpect(jsonPath("$.data.cardholderName").value("John Doe"))
                .andExpect(jsonPath("$.data.maskedCardNumber").value("************0366"))
                .andExpect(jsonPath("$.data.isDefault").value(false))
                .andReturn();

        // Then: Verify card was saved in database
        List<Card> cards = cardRepository.findByUserIdAndDeletedAtIsNull(testUserId);
        assertThat(cards).hasSize(1);
        assertThat(cards.get(0).getCardholderName()).isEqualTo("John Doe");
        assertThat(cards.get(0).getCardNumber()).isEqualTo("4532015112830366");
        assertThat(cards.get(0).getIsDefault()).isFalse();
    }

    @Test
    @DisplayName("POST /api/v1/cards/user/{userId} - Should create card with alternate route")
    void createCard_AlternateRoute_Success() throws Exception {
        // When: Create a card using alternate route
        mockMvc.perform(post("/api/v1/cards/user/" + testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCardRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.cardholderName").value("John Doe"));

        // Then: Verify card exists
        assertThat(cardRepository.findByUserIdAndDeletedAtIsNull(testUserId)).hasSize(1);
    }

    @Test
    @DisplayName("POST /api/v1/cards/{userId} - Should fail with invalid card number")
    void createCard_InvalidCardNumber_Fails() throws Exception {
        // Given: Invalid card number (less than 16 digits)
        validCardRequest.setCardNumber("123");

        // When/Then: Request should fail validation
        mockMvc.perform(post("/api/v1/cards/" + testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCardRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/cards/{userId} - Should fail when user has 3 cards already")
    void createCard_MaxCardsReached_Fails() throws Exception {
        // Given: User already has 3 cards
        for (int i = 0; i < 3; i++) {
            CreateCardRequest request = new CreateCardRequest();
            request.setCardholderName("Card " + i);
            request.setCardNumber("453201511283036" + i);
            request.setExpirationDate(LocalDate.now().plusYears(2));

            mockMvc.perform(post("/api/v1/cards/" + testUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // When: Try to add 4th card
        CreateCardRequest fourthCard = new CreateCardRequest();
        fourthCard.setCardholderName("Fourth Card");
        fourthCard.setCardNumber("4532015112830369");
        fourthCard.setExpirationDate(LocalDate.now().plusYears(2));

        // Then: Should fail with max cards error
        mockMvc.perform(post("/api/v1/cards/" + testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fourthCard)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User has reached the maximum limit of 3 cards."));

        // Verify still only 3 cards in database
        assertThat(cardRepository.findByUserIdAndDeletedAtIsNull(testUserId)).hasSize(3);
    }

    @Test
    @DisplayName("POST /api/v1/cards/{userId} - Should fail with duplicate card number")
    void createCard_DuplicateCardNumber_Fails() throws Exception {
        // Given: Card already exists
        mockMvc.perform(post("/api/v1/cards/" + testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCardRequest)))
                .andExpect(status().isCreated());

        // When: Try to create card with same number
        // Then: Should fail
        mockMvc.perform(post("/api/v1/cards/" + testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCardRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Card number already exists for this user."));

        // Verify only one card in database
        assertThat(cardRepository.findByUserIdAndDeletedAtIsNull(testUserId)).hasSize(1);
    }

    // ========== GET CARDS LIST TESTS ==========

    @Test
    @DisplayName("GET /api/v1/cards/{userId} - Should return list of user cards")
    void getCards_Success() throws Exception {
        // Given: User has 2 cards
        createTestCard(testUserId, "Card 1", "4532015112830361");
        createTestCard(testUserId, "Card 2", "4532015112830362");

        // When/Then: Get cards list
        mockMvc.perform(get("/api/v1/cards/" + testUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].cardholderName").exists())
                .andExpect(jsonPath("$.data[0].maskedCardNumber").exists());
    }

    @Test
    @DisplayName("GET /api/v1/cards/user/{userId} - Should return cards with alternate route")
    void getCards_AlternateRoute_Success() throws Exception {
        // Given: User has cards
        createTestCard(testUserId, "Test Card", "4532015112830366");

        // When/Then: Get cards using alternate route
        mockMvc.perform(get("/api/v1/cards/user/" + testUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/cards/{userId} - Should return empty list when no cards")
    void getCards_EmptyList_Success() throws Exception {
        // When/Then: Get cards for user with no cards
        mockMvc.perform(get("/api/v1/cards/" + testUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/cards/{userId} - Should not return soft-deleted cards")
    void getCards_ExcludesDeletedCards_Success() throws Exception {
        // Given: User has 1 active card and 1 deleted card
        Card activeCard = createTestCard(testUserId, "Active Card", "4532015112830361");
        Card deletedCard = createTestCard(testUserId, "Deleted Card", "4532015112830362");
        
        // Soft delete one card
        mockMvc.perform(delete("/api/v1/cards/" + deletedCard.getId()))
                .andExpect(status().isOk());

        // When/Then: Should only return active card
        mockMvc.perform(get("/api/v1/cards/" + testUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].cardholderName").value("Active Card"));
    }

    // ========== GET SINGLE CARD TESTS ==========

    @Test
    @DisplayName("GET /api/v1/cards/card/{id} - Should return single card")
    void getCardById_Success() throws Exception {
        // Given: Card exists
        Card card = createTestCard(testUserId, "John Doe", "4532015112830366");

        // When/Then: Get single card
        mockMvc.perform(get("/api/v1/cards/card/" + card.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.cardholderName").value("John Doe"))
                .andExpect(jsonPath("$.data.maskedCardNumber").value("************0366"))
                .andExpect(jsonPath("$.data.id").value(card.getId().toString()));
    }

    @Test
    @DisplayName("GET /api/v1/cards/card/{id} - Should fail when card not found")
    void getCardById_NotFound_Fails() throws Exception {
        // Given: Non-existent card ID
        UUID nonExistentId = UUID.randomUUID();

        // When/Then: Should return 404
        mockMvc.perform(get("/api/v1/cards/card/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/cards/card/{id} - Should fail when card is deleted")
    void getCardById_DeletedCard_Fails() throws Exception {
        // Given: Deleted card
        Card card = createTestCard(testUserId, "Deleted Card", "4532015112830366");
        mockMvc.perform(delete("/api/v1/cards/" + card.getId()))
                .andExpect(status().isOk());

        // When/Then: Should not find deleted card
        mockMvc.perform(get("/api/v1/cards/card/" + card.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ========== SET DEFAULT CARD TESTS ==========

    @Test
    @DisplayName("PUT /api/v1/cards/{id}/set-default - Should set card as default")
    void setDefaultCard_Success() throws Exception {
        // Given: User has 2 cards
        Card card1 = createTestCard(testUserId, "Card 1", "4532015112830361");
        Card card2 = createTestCard(testUserId, "Card 2", "4532015112830362");

        // When: Set card2 as default
        mockMvc.perform(put("/api/v1/cards/" + card2.getId() + "/set-default")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isDefault").value(true))
                .andExpect(jsonPath("$.data.cardholderName").value("Card 2"));

        // Then: Verify in database
        Card updatedCard1 = cardRepository.findById(card1.getId()).orElseThrow();
        Card updatedCard2 = cardRepository.findById(card2.getId()).orElseThrow();
        
        assertThat(updatedCard1.getIsDefault()).isFalse();
        assertThat(updatedCard2.getIsDefault()).isTrue();
    }

    @Test
    @DisplayName("PUT /api/v1/cards/{id}/set-default - Should unset previous default")
    void setDefaultCard_UnsetsOldDefault_Success() throws Exception {
        // Given: User has 3 cards, card1 is default
        Card card1 = createTestCard(testUserId, "Card 1", "4532015112830361");
        Card card2 = createTestCard(testUserId, "Card 2", "4532015112830362");
        Card card3 = createTestCard(testUserId, "Card 3", "4532015112830363");
        
        // Set card1 as default
        mockMvc.perform(put("/api/v1/cards/" + card1.getId() + "/set-default"))
                .andExpect(status().isOk());

        // When: Set card3 as default
        mockMvc.perform(put("/api/v1/cards/" + card3.getId() + "/set-default"))
                .andExpect(status().isOk());

        // Then: Only card3 should be default
        Card updatedCard1 = cardRepository.findById(card1.getId()).orElseThrow();
        Card updatedCard2 = cardRepository.findById(card2.getId()).orElseThrow();
        Card updatedCard3 = cardRepository.findById(card3.getId()).orElseThrow();
        
        assertThat(updatedCard1.getIsDefault()).isFalse();
        assertThat(updatedCard2.getIsDefault()).isFalse();
        assertThat(updatedCard3.getIsDefault()).isTrue();
    }

    @Test
    @DisplayName("PUT /api/v1/cards/{id}/set-default - Should fail when card not found")
    void setDefaultCard_NotFound_Fails() throws Exception {
        // Given: Non-existent card ID
        UUID nonExistentId = UUID.randomUUID();

        // When/Then: Should return 404
        mockMvc.perform(put("/api/v1/cards/" + nonExistentId + "/set-default")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ========== DELETE CARD TESTS ==========

    @Test
    @DisplayName("DELETE /api/v1/cards/{id} - Should soft delete card")
    void deleteCard_Success() throws Exception {
        // Given: Card exists
        Card card = createTestCard(testUserId, "Test Card", "4532015112830366");

        // When: Delete card
        mockMvc.perform(delete("/api/v1/cards/" + card.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Card deleted successfully"));

        // Then: Card should be soft deleted
        Card deletedCard = cardRepository.findById(card.getId()).orElseThrow();
        assertThat(deletedCard.getDeletedAt()).isNotNull();
        
        // Verify card is not returned in list
        List<Card> activeCards = cardRepository.findByUserIdAndDeletedAtIsNull(testUserId);
        assertThat(activeCards).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/v1/cards/{id} - Should fail when card not found")
    void deleteCard_NotFound_Fails() throws Exception {
        // Given: Non-existent card ID
        UUID nonExistentId = UUID.randomUUID();

        // When/Then: Should return 404
        mockMvc.perform(delete("/api/v1/cards/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/cards/{id} - Should fail when card already deleted")
    void deleteCard_AlreadyDeleted_Fails() throws Exception {
        // Given: Card already deleted
        Card card = createTestCard(testUserId, "Test Card", "4532015112830366");
        mockMvc.perform(delete("/api/v1/cards/" + card.getId()))
                .andExpect(status().isOk());

        // When/Then: Try to delete again should fail
        mockMvc.perform(delete("/api/v1/cards/" + card.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ========== COMPLETE FLOW TESTS ==========

    @Test
    @DisplayName("Complete Flow: Create → List → Get → Set Default → Delete")
    void completeCardFlow_Success() throws Exception {
        // Step 1: Create card
        MvcResult createResult = mockMvc.perform(post("/api/v1/cards/" + testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCardRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        String cardId = objectMapper.readTree(responseJson).get("data").get("id").asText();

        // Step 2: List cards - should have 1
        mockMvc.perform(get("/api/v1/cards/" + testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        // Step 3: Get single card
        mockMvc.perform(get("/api/v1/cards/card/" + cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isDefault").value(false));

        // Step 4: Set as default
        mockMvc.perform(put("/api/v1/cards/" + cardId + "/set-default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isDefault").value(true));

        // Step 5: Verify default in list
        mockMvc.perform(get("/api/v1/cards/" + testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].isDefault").value(true));

        // Step 6: Delete card
        mockMvc.perform(delete("/api/v1/cards/" + cardId))
                .andExpect(status().isOk());

        // Step 7: List should be empty
        mockMvc.perform(get("/api/v1/cards/" + testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("Complete Flow: Create 3 cards → Try 4th → Validate error → Delete 1 → Create new")
    void maxCardsFlow_Success() throws Exception {
        // Step 1: Create 3 cards
        UUID card1Id = createCardViaAPI(testUserId, "Card 1", "4532015112830361");
        UUID card2Id = createCardViaAPI(testUserId, "Card 2", "4532015112830362");
        UUID card3Id = createCardViaAPI(testUserId, "Card 3", "4532015112830363");

        // Step 2: Try to create 4th card - should fail
        CreateCardRequest fourthCard = new CreateCardRequest();
        fourthCard.setCardholderName("Fourth Card");
        fourthCard.setCardNumber("4532015112830364");
        fourthCard.setExpirationDate(LocalDate.now().plusYears(2));

        mockMvc.perform(post("/api/v1/cards/" + testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fourthCard)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User has reached the maximum limit of 3 cards."));

        // Step 3: Delete one card
        mockMvc.perform(delete("/api/v1/cards/" + card1Id))
                .andExpect(status().isOk());

        // Step 4: Now should be able to create 4th card
        mockMvc.perform(post("/api/v1/cards/" + testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fourthCard)))
                .andExpect(status().isCreated());

        // Step 5: Verify we have 3 cards (card2, card3, card4)
        mockMvc.perform(get("/api/v1/cards/" + testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    // ========== HELPER METHODS ==========

    /**
     * Helper method to create a test card directly in the database
     */
    private Card createTestCard(UUID userId, String cardholderName, String cardNumber) {
        Card card = new Card(userId, cardholderName, cardNumber, LocalDate.now().plusYears(2));
        return cardRepository.save(card);
    }

    /**
     * Helper method to create a card via API and return its ID
     */
    private UUID createCardViaAPI(UUID userId, String cardholderName, String cardNumber) throws Exception {
        CreateCardRequest request = new CreateCardRequest();
        request.setCardholderName(cardholderName);
        request.setCardNumber(cardNumber);
        request.setExpirationDate(LocalDate.now().plusYears(2));

        MvcResult result = mockMvc.perform(post("/api/v1/cards/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        String cardId = objectMapper.readTree(responseJson).get("data").get("id").asText();
        return UUID.fromString(cardId);
    }
}

