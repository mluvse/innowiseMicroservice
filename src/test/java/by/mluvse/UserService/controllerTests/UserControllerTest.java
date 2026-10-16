package by.mluvse.UserService.controllerTests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("User Controller Integration Tests")
class UserControllerTest extends AbstractIntegrationTest {

    private String userJson(String name, String email) {
        return """
            {
                "name": "%s",
                "surname": "Petrov",
                "birthDate": "1995-05-20",
                "email": "%s",
                "active": true,
                "cards": []
            }
            """.formatted(name, email);
    }

    private long createUser(String email) throws Exception {
        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Ivan", email)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private void createCard(long userId, String number) throws Exception {
        String json = """
            {
                "userId": %d,
                "number": "%s",
                "holder": "IVAN PETROV",
                "expirationDate": "2030-12-31",
                "active": true
            }
            """.formatted(userId, number);

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/users ")
    void createUser_Returns201() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Ivan", "ivan@mail.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@mail.com"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("POST /api/users")
    void createUser_DuplicateEmail_Returns400() throws Exception {
        createUser("ivan@mail.com");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Ivan", "ivan@mail.com")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users ")
    void createUser_InvalidEmail_Returns400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Ivan", "not-an-email")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/users/{id} ")
    void getUser_Returns200AndCaches() throws Exception {
        long id = createUser("ivan@mail.com");

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ivan"));

        assertTrue(isUserCached(id), "Пользователь должен быть закэширован");
    }

    @Test
    @DisplayName("GET /api/users/{id} ")
    void getUser_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/users ")
    void getAll_WithPagination() throws Exception {
        createUser("a@mail.com");
        createUser("b@mail.com");

        mockMvc.perform(get("/api/users")
                        .param("name", "iva")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @DisplayName("GET /api/users/{id}/cards ")
    void getCards_ReturnsUserCards() throws Exception {
        long userId = createUser("ivan@mail.com");
        createCard(userId, "4111111111111111");

        mockMvc.perform(get("/api/users/{id}/cards", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].number").value("4111111111111111"));
    }

    @Test
    @DisplayName("PUT /api/users/{id} ")
    void updateUser_ChangesFields() throws Exception {
        long id = createUser("ivan@mail.com");

        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Petr", "ivan@mail.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Petr"));

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(jsonPath("$.name").value("Petr"));
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/deactivate")
    void deactivateUser_ChangesStatus() throws Exception {
        long id = createUser("ivan@mail.com");

        mockMvc.perform(patch("/api/users/{id}/deactivate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(patch("/api/users/{id}/deactivate", id))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/users/delete/{id}")
    void deleteUser_RemovesFromCache() throws Exception {
        long id = createUser("ivan@mail.com");

        mockMvc.perform(get("/api/users/{id}", id)).andExpect(status().isOk());
        assertTrue(isUserCached(id));

        mockMvc.perform(delete("/api/users/delete/{id}", id))
                .andExpect(status().isOk());

        assertFalse(isUserCached(id), "Кэш пользователя должен быть очищен");

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound());
    }
}
