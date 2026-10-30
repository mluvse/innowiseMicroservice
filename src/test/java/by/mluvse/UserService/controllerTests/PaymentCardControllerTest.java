package by.mluvse.UserService.controllerTests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("PaymentCard Controller Integration Tests")
class PaymentCardControllerTest extends AbstractIntegrationTest {

    private static final String NUMBER = "4111111111111111";
    private static final String HOLDER = "IVAN PETROV";

    private long createUser(String name, String surname, String email) throws Exception {
        String json = """
            {"name":"%s","surname":"%s","birthDate":"1995-05-20","email":"%s"}
            """.formatted(name, surname, email);
        String body = mockMvc.perform(post("/api/users").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private long createUser(String email) throws Exception {
        return createUser("Ivan", "Petrov", email);
    }

    private String cardJson(String number, String holder) {
        return """
            {"number":"%s","holder":"%s","expirationDate":"2030-12-31"}
            """.formatted(number, holder);
    }

    private long createCard(long userId, String number) throws Exception {
        String body = mockMvc.perform(post("/api/users/{id}/cards", userId).with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(number, HOLDER)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    @Test
    void createCard_returns201_activeByDefault() throws Exception {
        long userId = createUser("ivan@mail.com");
        mockMvc.perform(post("/api/users/{id}/cards", userId).with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(NUMBER, HOLDER)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.number").value(NUMBER))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createCard_userNotFound_returns404() throws Exception {
        mockMvc.perform(post("/api/users/{id}/cards", 999999).with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(NUMBER, HOLDER)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCard_duplicateNumber_returns409() throws Exception {
        long userId = createUser("ivan@mail.com");
        createCard(userId, NUMBER);
        mockMvc.perform(post("/api/users/{id}/cards", userId).with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(NUMBER, HOLDER)))
                .andExpect(status().isConflict());
    }

    @Test
    void createCard_sixthActiveCard_returns400() throws Exception {
        long userId = createUser("ivan@mail.com");
        for (int i = 0; i < 5; i++) {
            createCard(userId, "411111111111111" + i);
        }
        mockMvc.perform(post("/api/users/{id}/cards", userId).with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson("4111111111111119", HOLDER)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCard_inactiveCardsDoNotCountToLimit() throws Exception {
        long userId = createUser("ivan@mail.com");
        for (int i = 0; i < 5; i++) {
            long cardId = createCard(userId, "411111111111111" + i);
            mockMvc.perform(patch("/api/cards/{id}/status", cardId).with(asAdmin())
                            .param("active", "false"))
                    .andExpect(status().isOk());
        }
        createCard(userId, "4111111111111119");   // должно пройти
    }

    @Test
    void activateCard_whenFiveActive_returns400() throws Exception {
        long userId = createUser("ivan@mail.com");
        long first = createCard(userId, "4111111111111110");
        mockMvc.perform(patch("/api/cards/{id}/status", first).with(asAdmin())
                .param("active", "false")).andExpect(status().isOk());
        for (int i = 1; i <= 5; i++) {
            createCard(userId, "411111111111111" + i);   // 5 активных
        }
        mockMvc.perform(patch("/api/cards/{id}/status", first).with(asAdmin())
                        .param("active", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCard_blankHolder_returns400() throws Exception {
        long userId = createUser("ivan@mail.com");
        mockMvc.perform(post("/api/users/{id}/cards", userId).with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(NUMBER, "")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCard_forAnotherUser_returns403() throws Exception {
        long userId = createUser("ivan@mail.com");
        mockMvc.perform(post("/api/users/{id}/cards", userId).with(asUser(userId + 1, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(NUMBER, HOLDER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCard_refreshesUserCache() throws Exception {
        long userId = createUser("ivan@mail.com");
        mockMvc.perform(get("/api/users/{id}", userId).with(asAdmin()))
                .andExpect(jsonPath("$.cards.length()").value(0));
        createCard(userId, NUMBER);
        mockMvc.perform(get("/api/users/{id}", userId).with(asAdmin()))
                .andExpect(jsonPath("$.cards.length()").value(1));
    }

    @Test
    void getCard_returns200_owner_and403_stranger() throws Exception {
        long userId = createUser("ivan@mail.com");
        long cardId = createCard(userId, NUMBER);
        mockMvc.perform(get("/api/cards/{id}", cardId).with(asUser(userId, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(NUMBER));
        mockMvc.perform(get("/api/cards/{id}", cardId).with(asUser(userId + 1, "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCard_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/cards/{id}", 999999).with(asAdmin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_filterByOwnerNameAndSurname_withPagination() throws Exception {
        long u1 = createUser("Ivan", "Petrov", "a@mail.com");
        long u2 = createUser("Ivan", "Sidorov", "b@mail.com");
        createCard(u1, "4111111111111111");
        createCard(u1, "4111111111111112");
        createCard(u2, "4111111111111113");

        mockMvc.perform(get("/api/cards").with(asAdmin())
                        .param("name", "iva").param("surname", "petr")
                        .param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getAll_userRole_returns403() throws Exception {
        mockMvc.perform(get("/api/cards").with(asUser(1L, "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateCard_changesFields_andRefreshesUserCache() throws Exception {
        long userId = createUser("ivan@mail.com");
        long cardId = createCard(userId, NUMBER);
        mockMvc.perform(get("/api/users/{id}", userId).with(asAdmin())).andExpect(status().isOk());

        mockMvc.perform(put("/api/cards/{id}", cardId).with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(NUMBER, "PETR IVANOV")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holder").value("PETR IVANOV"));

        mockMvc.perform(get("/api/users/{id}", userId).with(asAdmin()))
                .andExpect(jsonPath("$.cards[0].holder").value("PETR IVANOV"));
    }

    @Test
    void updateCard_notFound_returns404() throws Exception {
        mockMvc.perform(put("/api/cards/{id}", 999999).with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(NUMBER, HOLDER)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCard_numberOfAnotherCard_returns409() throws Exception {
        long userId = createUser("ivan@mail.com");
        createCard(userId, "4111111111111111");
        long second = createCard(userId, "4111111111111112");
        mockMvc.perform(put("/api/cards/{id}", second).with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson("4111111111111111", HOLDER)))
                .andExpect(status().isConflict());
    }

    @Test
    void changeStatus_togglesAndRejectsSame() throws Exception {
        long userId = createUser("ivan@mail.com");
        long cardId = createCard(userId, NUMBER);
        mockMvc.perform(patch("/api/cards/{id}/status", cardId).with(asAdmin()).param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        mockMvc.perform(patch("/api/cards/{id}/status", cardId).with(asAdmin()).param("active", "false"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/cards/{id}/status", cardId).with(asAdmin()).param("active", "true"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void changeStatus_notFound_returns404() throws Exception {
        mockMvc.perform(patch("/api/cards/{id}/status", 999999).with(asAdmin()).param("active", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCard_returns204_andRemovesFromUser() throws Exception {
        long userId = createUser("ivan@mail.com");
        long cardId = createCard(userId, NUMBER);
        mockMvc.perform(get("/api/users/{id}", userId).with(asAdmin()))
                .andExpect(jsonPath("$.cards.length()").value(1));

        mockMvc.perform(delete("/api/cards/{id}", cardId).with(asAdmin()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cards/{id}", cardId).with(asAdmin()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/users/{id}", userId).with(asAdmin()))
                .andExpect(jsonPath("$.cards.length()").value(0));
    }

    @Test
    void deleteCard_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/cards/{id}", 999999).with(asAdmin()))
                .andExpect(status().isNotFound());
    }
}
