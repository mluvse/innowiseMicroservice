package by.mluvse.UserService.controllerTests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("PaymentCard Controller Integration Tests")
class PaymentCardControllerTest extends AbstractIntegrationTest {

    private static final String VALID_NUMBER = "4111111111111111";
    private static final String VALID_HOLDER = "IVAN PETROV";

    private long createUser(String email) throws Exception {
        String json = """
            {
                "name": "Ivan",
                "surname": "Petrov",
                "birthDate": "1995-05-20",
                "email": "%s",
                "active": true,
                "cards": []
            }
            """.formatted(email);

        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private String cardJson(long userId, String number, String holder) {
        return """
            {
                "userId": %d,
                "number": "%s",
                "holder": "%s",
                "expirationDate": "2030-12-31",
                "active": true
            }
            """.formatted(userId, number, holder);
    }

    private long createCard(long userId, String number) throws Exception {
        String response = mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(userId, number, VALID_HOLDER)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    @DisplayName("POST /api/cards")
    void createCard_Returns201() throws Exception {
        long userId = createUser("ivan@mail.com");

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(userId, VALID_NUMBER, VALID_HOLDER)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.number").value(VALID_NUMBER))
                .andExpect(jsonPath("$.holder").value(VALID_HOLDER))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("POST /api/cards")
    void createCard_UserNotFound_Returns404() throws Exception {
        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(999999L, VALID_NUMBER, VALID_HOLDER)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/cards")
    void createCard_DuplicateNumber_Returns409() throws Exception {
        long userId = createUser("ivan@mail.com");
        createCard(userId, VALID_NUMBER);

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(userId, VALID_NUMBER, VALID_HOLDER)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/cards")
    void createCard_SixthCard_Returns400() throws Exception {
        long userId = createUser("ivan@mail.com");

        // Создаем 5 карт (максимум)
        for (int i = 0; i < 5; i++) {
            createCard(userId, "411111111111111" + i);
        }

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(userId, "4111111111111119", VALID_HOLDER)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/cards")
    void createCard_BlankHolder_Returns400() throws Exception {
        long userId = createUser("ivan@mail.com");

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(userId, VALID_NUMBER, "")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/cards/{id}")
    void getCard_Returns200() throws Exception {
        long userId = createUser("ivan@mail.com");
        long cardId = createCard(userId, VALID_NUMBER);

        mockMvc.perform(get("/api/cards/{id}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId))
                .andExpect(jsonPath("$.number").value(VALID_NUMBER))
                .andExpect(jsonPath("$.holder").value(VALID_HOLDER));
    }

    @Test
    @DisplayName("GET /api/cards/{id}")
    void getCard_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/cards/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/cards/{id}")
    void getCard_Cached() throws Exception {
        long userId = createUser("ivan@mail.com");
        long cardId = createCard(userId, VALID_NUMBER);

        mockMvc.perform(get("/api/cards/{id}", cardId))
                .andExpect(status().isOk());

        assertTrue(isCardCached(cardId), "Карта должна быть закэширована");
    }

    @Test
    @DisplayName("GET /api/cards ")
    void getAll_FilterByOwnerName() throws Exception {
        long user1 = createUser("a@mail.com"); // Ivan
        long user2 = createUser("b@mail.com"); // Ivan
        createCard(user1, "4111111111111111");
        createCard(user1, "4111111111111112");
        createCard(user2, "4111111111111113");

        mockMvc.perform(get("/api/cards")
                        .param("name", "iva")
                        .param("surname", "petr")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.size").value(2));
    }

    @Test
    @DisplayName("PUT /api/cards/{id}")
    void updateCard_ChangesFields() throws Exception {
        long userId = createUser("ivan@mail.com");
        long cardId = createCard(userId, VALID_NUMBER);

        String updateJson = cardJson(userId, VALID_NUMBER, "PETR IVANOV");

        mockMvc.perform(put("/api/cards/{id}", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holder").value("PETR IVANOV"));

        mockMvc.perform(get("/api/cards/{id}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holder").value("PETR IVANOV"));
    }

    @Test
    @DisplayName("PATCH /api/cards/{id}/deactivate")
    void deactivateCard_ChangesStatus() throws Exception {
        long userId = createUser("ivan@mail.com");
        long cardId = createCard(userId, VALID_NUMBER);

        mockMvc.perform(patch("/api/cards/{id}/deactivate", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(patch("/api/cards/{id}/deactivate", cardId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/cards/{id}/activate ")
    void activateCard_ChangesStatus() throws Exception {
        long userId = createUser("ivan@mail.com");
        long cardId = createCard(userId, VALID_NUMBER);

        mockMvc.perform(patch("/api/cards/{id}/deactivate", cardId))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/cards/{id}/activate", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("DELETE /api/cards/{id}")
    void deleteCard_RemovesFromCacheAndUser() throws Exception {
        long userId = createUser("ivan@mail.com");
        long cardId = createCard(userId, VALID_NUMBER);

        mockMvc.perform(get("/api/cards/{id}", cardId)).andExpect(status().isOk());
        assertTrue(isCardCached(cardId));

        mockMvc.perform(delete("/api/cards/{id}", cardId))
                .andExpect(status().isOk());

        assertFalse(isCardCached(cardId), "Кэш карты должен быть очищен");

        mockMvc.perform(get("/api/cards/{id}", cardId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/users/{id}/cards", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
