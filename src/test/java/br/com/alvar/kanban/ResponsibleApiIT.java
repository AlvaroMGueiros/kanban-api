package br.com.alvar.kanban;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResponsibleApiIT extends ApiIntegrationTest {
    @Test
    void shouldCreateReadUpdateAndDeleteResponsible() throws Exception {
        String response = mockMvc.perform(post("/api/responsibles").contentType(MediaType.APPLICATION_JSON)
                        .content(responsibleJson("Ana", "ANA@example.com")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/responsibles/1"))
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String createdAt = objectMapper.readTree(response).get("createdAt").asText();

        mockMvc.perform(get("/api/responsibles/1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Ana"));

        mockMvc.perform(put("/api/responsibles/1").contentType(MediaType.APPLICATION_JSON)
                        .content(responsibleJson("Ana Silva", "ANA@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Silva"))
                .andExpect(jsonPath("$.createdAt").value(createdAt));

        mockMvc.perform(delete("/api/responsibles/1")).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/responsibles/1")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/responsibles/1"));
    }

    @Test
    void shouldRejectDuplicateEmailAndPreserveOriginalOnFailedUpdate() throws Exception {
        mockMvc.perform(post("/api/responsibles").contentType(MediaType.APPLICATION_JSON)
                .content(responsibleJson("Ana", "ana@example.com"))).andExpect(status().isCreated());
        mockMvc.perform(post("/api/responsibles").contentType(MediaType.APPLICATION_JSON)
                .content(responsibleJson("Bia", "bia@example.com"))).andExpect(status().isCreated());
        mockMvc.perform(post("/api/responsibles").contentType(MediaType.APPLICATION_JSON)
                .content(responsibleJson("Outra", "ANA@example.com"))).andExpect(status().isConflict());
        mockMvc.perform(put("/api/responsibles/2").contentType(MediaType.APPLICATION_JSON)
                .content(responsibleJson("Alterada", "ANA@example.com"))).andExpect(status().isConflict());
        mockMvc.perform(get("/api/responsibles/2")).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bia"))
                .andExpect(jsonPath("$.email").value("bia@example.com"));
    }

    @Test
    void shouldEnforceEmailUniquenessInPostgres() throws Exception {
        mockMvc.perform(post("/api/responsibles").contentType(MediaType.APPLICATION_JSON)
                .content(responsibleJson("Ana", "ana@example.com"))).andExpect(status().isCreated());

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO kanban.responsibles (name, email, role, department, "createdAt", "updatedAt")
                SELECT name, email, role, department, "createdAt", "updatedAt" FROM kanban.responsibles WHERE id = 1
                """)).isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("responsibleEmailUnique");
    }

    @Test
    void shouldPaginateAndSortWithStableIdTieBreaker() throws Exception {
        for (String name : new String[]{"Ana", "Carlos", "Bruno"}) {
            mockMvc.perform(post("/api/responsibles").contentType(MediaType.APPLICATION_JSON)
                    .content(responsibleJson(name, name + "@example.com"))).andExpect(status().isCreated());
        }
        mockMvc.perform(get("/api/responsibles").param("page", "1").param("size", "1").param("sort", "name,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Bruno"))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.page").value(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"?page=-1", "?size=0", "?size=101", "?sort=password,asc", "?sort=name,invalid", "?page=notNumber", "?page=2147483647&size=100"})
    void shouldRejectInvalidPaginationAndSort(String query) throws Exception {
        mockMvc.perform(get("/api/responsibles" + query)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }

    @Test
    void shouldValidatePayloadAndReturnSpecificFieldMessages() throws Exception {
        mockMvc.perform(post("/api/responsibles").contentType(MediaType.APPLICATION_JSON)
                        .content(responsibleJson("", "not-an-email")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("email")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("name")));
        mockMvc.perform(post("/api/responsibles").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM kanban.responsibles", Integer.class)).isZero();
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingOrDeletingMissingResponsible() throws Exception {
        mockMvc.perform(put("/api/responsibles/99").contentType(MediaType.APPLICATION_JSON)
                .content(responsibleJson("Ana", "ana@example.com"))).andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/responsibles/99")).andExpect(status().isNotFound());
    }

    private String responsibleJson(String name, String email) throws Exception {
        return objectMapper.writeValueAsString(Map.of("name", name, "email", email,
                "role", "Analista", "department", "Planejamento"));
    }
}
