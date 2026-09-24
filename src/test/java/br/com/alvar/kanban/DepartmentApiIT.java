package br.com.alvar.kanban;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DepartmentApiIT extends ApiIntegrationTest {
    @Test
    void shouldCreateListUpdateAndDeleteDepartment() throws Exception {
        mockMvc.perform(post("/api/departments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Obras\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Obras"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        long id = jdbcTemplate.queryForObject(
                "SELECT id FROM kanban.departments WHERE name = 'Obras'", Long.class);
        mockMvc.perform(get("/api/departments").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.name == 'Obras')]").exists());
        mockMvc.perform(put("/api/departments/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Infraestrutura\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Infraestrutura"));
        mockMvc.perform(delete("/api/departments/" + id)).andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectDuplicateAndDepartmentInUse() throws Exception {
        mockMvc.perform(post("/api/departments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"planejamento\"}"))
                .andExpect(status().isConflict());
        createResponsible("Ana", "ana@example.com");
        long id = jdbcTemplate.queryForObject(
                "SELECT id FROM kanban.departments WHERE name = 'Planejamento'", Long.class);
        mockMvc.perform(delete("/api/departments/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("vinculada a responsável")));
    }
}
