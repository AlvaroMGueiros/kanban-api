package br.com.alvar.kanban;

import java.time.LocalDate;
import java.util.List;

import br.com.alvar.kanban.application.dto.ProjectRequest;
import br.com.alvar.kanban.application.dto.ProjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProjectApiIT extends ApiIntegrationTest {
    private final LocalDate today = LocalDate.of(2026, 9, 22);

    @Test
    void shouldCreateReadUpdateAndDeleteProjectKeepingResponsibles() throws Exception {
        long responsibleId = createResponsible("Ana", "ana@example.com");
        ProjectResponse project = createProject(new ProjectRequest("Obra", List.of(responsibleId),
                today.minusDays(2), today.plusDays(2), null, null));
        assertThat(project.status().name()).isEqualTo("ATRASADO");
        assertThat(project.delayDays()).isZero();
        assertThat(project.remainingTimePercentage()).isEqualByComparingTo("50");

        mockMvc.perform(get("/api/projects/" + project.id())).andExpect(status().isOk())
                .andExpect(jsonPath("$.responsibles[0].id").value(responsibleId));
        mockMvc.perform(put("/api/projects/" + project.id()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProjectRequest("Obra iniciada", List.of(responsibleId),
                                today.minusDays(2), today.plusDays(2), today, null))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
        mockMvc.perform(delete("/api/projects/" + project.id())).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/projects/" + project.id())).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/responsibles/" + responsibleId)).andExpect(status().isOk());
    }

    @Test
    void shouldReadProjectWithoutAnyDatesAfterReloadingFromPostgres() throws Exception {
        long responsibleId = createResponsible("Ana", "ana@example.com");
        ProjectResponse project = createProject(new ProjectRequest("Sem prazo", List.of(responsibleId), null, null, null, null));
        mockMvc.perform(get("/api/projects/" + project.id())).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("A_INICIAR"))
                .andExpect(jsonPath("$.delayDays").value(0))
                .andExpect(jsonPath("$.remainingTimePercentage").value(0));
        mockMvc.perform(get("/api/projects").param("size", "1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Sem prazo"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldSupportSeveralResponsiblesAndBlockDeletingLinkedResponsible() throws Exception {
        long firstId = createResponsible("Ana", "ana@example.com");
        long secondId = createResponsible("Bia", "bia@example.com");
        ProjectResponse project = createProject(new ProjectRequest("Obra", List.of(firstId, secondId), null, null, null, null));
        assertThat(project.responsibles()).hasSize(2);

        mockMvc.perform(delete("/api/responsibles/" + firstId)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("vinculado")));
        mockMvc.perform(put("/api/projects/" + project.id()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProjectRequest("Obra", List.of(secondId), null, null, null, null))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.responsibles.length()").value(1));
        mockMvc.perform(delete("/api/responsibles/" + firstId)).andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectMissingRepeatedOrEmptyResponsibles() throws Exception {
        mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ProjectRequest("Obra", List.of(99L), null, null, null, null))))
                .andExpect(status().isNotFound());
        long responsibleId = createResponsible("Ana", "ana@example.com");
        mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ProjectRequest("Obra", List.of(responsibleId, responsibleId), null, null, null, null))))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ProjectRequest("Obra", List.of(), null, null, null, null))))
                .andExpect(status().isBadRequest());
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM kanban.projects", Integer.class)).isZero();
    }

    @Test
    void shouldRejectInvalidDatesAndRollBackUpdate() throws Exception {
        long responsibleId = createResponsible("Ana", "ana@example.com");
        ProjectResponse project = createProject(new ProjectRequest("Original", List.of(responsibleId), today, today, null, null));
        mockMvc.perform(put("/api/projects/" + project.id()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProjectRequest("Inválido", List.of(responsibleId),
                                today, today.minusDays(1), null, null))))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(put("/api/projects/" + project.id()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProjectRequest("Inválido", List.of(responsibleId),
                                today, today.plusDays(2), today.plusDays(1), null))))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get("/api/projects/" + project.id())).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Original"))
                .andExpect(jsonPath("$.plannedEndDate").value(today.toString()));
    }

    @Test
    void shouldAllowDirectCompletionAndZeroMetrics() throws Exception {
        long responsibleId = createResponsible("Ana", "ana@example.com");
        ProjectResponse project = createProject(new ProjectRequest("Concluído", List.of(responsibleId),
                today.minusDays(10), today.minusDays(2), null, today));
        assertThat(project.status().name()).isEqualTo("CONCLUIDO");
        assertThat(project.delayDays()).isZero();
        assertThat(project.remainingTimePercentage()).isZero();
    }

    @Test
    void shouldCombineProjectFiltersBeforePagination() throws Exception {
        long planningId = createResponsible("Ana", "ana@example.com");
        long worksId = createResponsible("Bia", "bia@example.com");
        jdbcTemplate.update("""
                INSERT INTO kanban.departments (name, "createdAt", "updatedAt")
                VALUES ('Obras', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """);
        jdbcTemplate.update("UPDATE kanban.responsibles SET department = 'Obras' WHERE id = ?", worksId);
        createProject(new ProjectRequest("Reforma da escola", List.of(planningId),
                today.minusDays(2), today.minusDays(1), null, null));
        createProject(new ProjectRequest("Reforma da praça", List.of(worksId),
                today.minusDays(2), today.minusDays(1), null, null));
        createProject(new ProjectRequest("Projeto futuro", List.of(worksId),
                today.plusDays(1), today.plusDays(2), null, null));

        mockMvc.perform(get("/api/projects")
                        .param("status", "ATRASADO")
                        .param("responsibleId", Long.toString(worksId))
                        .param("department", "obras")
                        .param("text", "PRAÇA")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Reforma da praça"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldTreatLikeWildcardsAsLiteralText() throws Exception {
        long responsibleId = createResponsible("Ana", "ana@example.com");
        createProject(new ProjectRequest("Projeto 100%", List.of(responsibleId), null, null, null, null));
        createProject(new ProjectRequest("Projeto comum", List.of(responsibleId), null, null, null, null));

        mockMvc.perform(get("/api/projects").param("text", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Projeto 100%"));
    }
}
