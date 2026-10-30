package by.mluvse.UserService.controllerTests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("User Controller Integration Tests")
class UserControllerTest extends AbstractIntegrationTest {

    private String userJson(String name, String surname, String email) {
        return """
            {"name":"%s","surname":"%s","birthDate":"1995-05-20","email":"%s"}
            """.formatted(name, surname, email);
    }

    private long createUser(String name, String surname, String email) throws Exception {
        String body = mockMvc.perform(post("/api/users").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(name, surname, email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private long createUser(String email) throws Exception {
        return createUser("Ivan", "Petrov", email);
    }

    private void createCard(long userId, String number) throws Exception {
        mockMvc.perform(post("/api/users/{id}/cards", userId).with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"number":"%s","holder":"IVAN PETROV","expirationDate":"2030-12-31"}
                            """.formatted(number)))
                .andExpect(status().isCreated());
    }

    @Test
    void createUser_returns201_andActiveByDefault() throws Exception {
        mockMvc.perform(post("/api/users").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Ivan", "Petrov", "ivan@mail.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createUser_duplicateEmail_returns409() throws Exception {
        createUser("ivan@mail.com");
        mockMvc.perform(post("/api/users").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Ivan", "Petrov", "ivan@mail.com")))
                .andExpect(status().isConflict());
    }

    @Test
    void createUser_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/users").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Ivan", "Petrov", "not-an-email")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Ivan", "Petrov", "ivan@mail.com")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUser_returns200_andCaches() throws Exception {
        long id = createUser("ivan@mail.com");
        mockMvc.perform(get("/api/users/{id}", id).with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ivan"));
        assertTrue(isCached("users", id));
    }

    @Test
    void getUser_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999999).with(asAdmin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUser_ownerAllowed_otherForbidden() throws Exception {
        long id = createUser("ivan@mail.com");
        mockMvc.perform(get("/api/users/{id}", id).with(asUser(id, "USER")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/users/{id}", id).with(asUser(id + 1, "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAll_userRole_returns403() throws Exception {
        mockMvc.perform(get("/api/users").with(asUser(1L, "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAll_filterByNameWithPagination() throws Exception {
        createUser("Ivan", "Petrov", "a@mail.com");
        createUser("Ivan", "Sidorov", "b@mail.com");
        mockMvc.perform(get("/api/users").with(asAdmin())
                        .param("name", "iva").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getAll_filterBySurnameAlone() throws Exception {
        createUser("Petr", "Ivanov", "a@mail.com");     // "ivan" есть в фамилии
        createUser("Ivan", "Sidorov", "b@mail.com");    // "ivan" есть в имени
        mockMvc.perform(get("/api/users").with(asAdmin()).param("surname", "ivan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].surname").value("Ivanov"));
    }

    @Test
    void getCards_returnsUserCards() throws Exception {
        long id = createUser("ivan@mail.com");
        createCard(id, "4111111111111111");
        mockMvc.perform(get("/api/users/{id}/cards", id).with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].number").value("4111111111111111"));
    }

    @Test
    void getCards_userNotFound_returns404() throws Exception {
        mockMvc.perform(get("/api/users/{id}/cards", 999999).with(asAdmin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_changesFields_keepsActive_refreshesCache() throws Exception {
        long id = createUser("ivan@mail.com");
        mockMvc.perform(get("/api/users/{id}", id).with(asAdmin())).andExpect(status().isOk());

        mockMvc.perform(put("/api/users/{id}", id).with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Petr", "Petrov", "ivan@mail.com")))   // без "active"
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Petr"))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(get("/api/users/{id}", id).with(asAdmin()))
                .andExpect(jsonPath("$.name").value("Petr"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void updateUser_duplicateEmail_returns409() throws Exception {
        createUser("a@mail.com");
        long second = createUser("b@mail.com");
        mockMvc.perform(put("/api/users/{id}", second).with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Ivan", "Petrov", "a@mail.com")))
                .andExpect(status().isConflict());
    }

    @Test
    void updateUser_notFound_returns404() throws Exception {
        mockMvc.perform(put("/api/users/{id}", 999999).with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Ivan", "Petrov", "x@mail.com")))
                .andExpect(status().isNotFound());
    }

    @Test
    void changeStatus_togglesAndRejectsSame() throws Exception {
        long id = createUser("ivan@mail.com");
        mockMvc.perform(patch("/api/users/{id}/status", id).with(asAdmin()).param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        mockMvc.perform(patch("/api/users/{id}/status", id).with(asAdmin()).param("active", "false"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/users/{id}/status", id).with(asAdmin()).param("active", "true"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void changeStatus_userRole_returns403() throws Exception {
        long id = createUser("ivan@mail.com");
        mockMvc.perform(patch("/api/users/{id}/status", id).with(asUser(id, "USER"))
                        .param("active", "false"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_returns204_andClearsUserAndCardCaches() throws Exception {
        long id = createUser("ivan@mail.com");
        createCard(id, "4111111111111111");
        mockMvc.perform(get("/api/users/{id}", id).with(asAdmin())).andExpect(status().isOk());
        assertTrue(isCached("users", id));

        mockMvc.perform(delete("/api/users/{id}", id).with(asAdmin()))
                .andExpect(status().isNoContent());

        assertFalse(isCached("users", id));
        mockMvc.perform(get("/api/users/{id}", id).with(asAdmin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 999999).with(asAdmin()))
                .andExpect(status().isNotFound());
    }
}
