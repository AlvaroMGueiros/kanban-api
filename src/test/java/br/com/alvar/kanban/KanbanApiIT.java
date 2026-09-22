package br.com.alvar.kanban;

import java.time.LocalDate;
import java.util.List;

import br.com.alvar.kanban.application.dto.ProjectRequest;
import br.com.alvar.kanban.application.dto.ProjectResponse;
import br.com.alvar.kanban.application.dto.TransitionRequest;
import br.com.alvar.kanban.domain.model.ProjectStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class KanbanApiIT extends ApiIntegrationTest {

    // Clock is fixed at 2026-09-22 (America/Fortaleza) in PostgresTestConfiguration
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 22);

    // -------------------------------------------------------------------------
    // Kanban column listing
    // -------------------------------------------------------------------------

    @Test
    void shouldListProjectsByStatus() throws Exception {
        long r = createResponsible("Ana", "ana@example.com");
        createProject(new ProjectRequest("Obra A", List.of(r),
                TODAY.minusDays(2), TODAY.plusDays(3), TODAY, null)); // EM_ANDAMENTO
        createProject(new ProjectRequest("Obra B", List.of(r),
                TODAY.minusDays(5), TODAY.minusDays(1), null, null)); // ATRASADO
        createProject(new ProjectRequest("Obra C", List.of(r),
                TODAY.plusDays(1), TODAY.plusDays(5), null, null));   // A_INICIAR

        mockMvc.perform(get("/api/kanban/projects").param("status", "EM_ANDAMENTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Obra A"));

        mockMvc.perform(get("/api/kanban/projects").param("status", "ATRASADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Obra B"));

        mockMvc.perform(get("/api/kanban/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void shouldFilterBeforePaginatingAndKeepAccurateTotals() throws Exception {
        long responsibleId = createResponsible("Ana", "ana@example.com");
        createProject(new ProjectRequest("Primeiro", List.of(responsibleId),
                TODAY.plusDays(1), TODAY.plusDays(2), null, null));
        createProject(new ProjectRequest("Segundo", List.of(responsibleId),
                TODAY.minusDays(2), TODAY.minusDays(1), null, null));
        createProject(new ProjectRequest("Terceiro", List.of(responsibleId),
                TODAY.minusDays(2), TODAY.minusDays(1), null, null));

        mockMvc.perform(get("/api/kanban/projects").param("status", "ATRASADO")
                        .param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Terceiro"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void shouldRejectInvalidStatusFilter() throws Exception {
        mockMvc.perform(get("/api/kanban/projects").param("status", "INVALIDO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }

    // -------------------------------------------------------------------------
    // A_INICIAR transitions
    // -------------------------------------------------------------------------

    @Test
    void shouldTransitionFromAIniciarToEmAndamento() throws Exception {
        long r = createResponsible("Ana", "ana@example.com");
        ProjectResponse p = createProject(new ProjectRequest("Obra",
                List.of(r), TODAY.minusDays(1), TODAY.plusDays(5), null, null));
        assertThat(p.status()).isEqualTo(ProjectStatus.ATRASADO);

        // Fix: project was ATRASADO (planned start yesterday), transition to EM_ANDAMENTO
        // First update dates so planned start is today
        mockMvc.perform(put("/api/projects/" + p.id()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ProjectRequest("Obra",
                        List.of(r), TODAY, TODAY.plusDays(5), null, null))))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/kanban/projects/" + p.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransitionRequest(ProjectStatus.EM_ANDAMENTO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.actualStartDate").value(TODAY.toString()));
    }

    @Test
    void shouldTransitionFromAIniciarToConcluido() throws Exception {
        long r = createResponsible("Ana", "ana@example.com");
        ProjectResponse p = createProject(new ProjectRequest("Obra",
                List.of(r), null, null, null, null));

        mockMvc.perform(patch("/api/kanban/projects/" + p.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransitionRequest(ProjectStatus.CONCLUIDO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"))
                .andExpect(jsonPath("$.actualEndDate").value(TODAY.toString()))
                .andExpect(jsonPath("$.delayDays").value(0))
                .andExpect(jsonPath("$.remainingTimePercentage").value(0));
    }

    @Test
    void shouldRejectTransitionFromAIniciarToEmAndamentoWhenEndDatePast() throws Exception {
        long r = createResponsible("Ana", "ana@example.com");
        // plannedEnd in past → filling actualStart → ATRASADO, not EM_ANDAMENTO
        ProjectResponse p = createProject(new ProjectRequest("Obra",
                List.of(r), TODAY.minusDays(5), TODAY.minusDays(1), null, null));
        assertThat(p.status()).isEqualTo(ProjectStatus.ATRASADO);

        mockMvc.perform(patch("/api/kanban/projects/" + p.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransitionRequest(ProjectStatus.EM_ANDAMENTO))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("TRANSITION_NOT_ALLOWED"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // -------------------------------------------------------------------------
    // EM_ANDAMENTO transitions
    // -------------------------------------------------------------------------

    @Test
    void shouldTransitionFromEmAndamentoToConcluido() throws Exception {
        long r = createResponsible("Ana", "ana@example.com");
        ProjectResponse p = createProject(new ProjectRequest("Obra",
                List.of(r), TODAY.minusDays(3), TODAY.plusDays(3), TODAY, null));
        assertThat(p.status()).isEqualTo(ProjectStatus.EM_ANDAMENTO);

        mockMvc.perform(patch("/api/kanban/projects/" + p.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransitionRequest(ProjectStatus.CONCLUIDO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"))
                .andExpect(jsonPath("$.actualEndDate").value(TODAY.toString()));
    }

    @Test
    void shouldTransitionFromEmAndamentoToAIniciarWhenFutureDates() throws Exception {
        long r = createResponsible("Ana", "ana@example.com");
        // Future planned dates so that clearing actualStart → A_INICIAR
        ProjectResponse p = createProject(new ProjectRequest("Obra",
                List.of(r), TODAY.plusDays(1), TODAY.plusDays(5), TODAY, null));
        assertThat(p.status()).isEqualTo(ProjectStatus.EM_ANDAMENTO);

        mockMvc.perform(patch("/api/kanban/projects/" + p.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransitionRequest(ProjectStatus.A_INICIAR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("A_INICIAR"))
                .andExpect(jsonPath("$.actualStartDate").doesNotExist());
    }

    @Test
    void shouldRejectEmAndamentoToAIniciarWhenStartDatePast() throws Exception {
        long r = createResponsible("Ana", "ana@example.com");
        // plannedStart yesterday → clearing actualStart → ATRASADO
        ProjectResponse p = createProject(new ProjectRequest("Obra",
                List.of(r), TODAY.minusDays(1), TODAY.plusDays(5), TODAY, null));
        assertThat(p.status()).isEqualTo(ProjectStatus.EM_ANDAMENTO);

        mockMvc.perform(patch("/api/kanban/projects/" + p.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransitionRequest(ProjectStatus.A_INICIAR))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("TRANSITION_NOT_ALLOWED"));
    }

    // -------------------------------------------------------------------------
    // ATRASADO transitions
    // -------------------------------------------------------------------------

    @Test
    void shouldTransitionFromAtrasadoToConcluido() throws Exception {
        long r = createResponsible("Ana", "ana@example.com");
        ProjectResponse p = createProject(new ProjectRequest("Obra",
                List.of(r), TODAY.minusDays(5), TODAY.minusDays(1), null, null));
        assertThat(p.status()).isEqualTo(ProjectStatus.ATRASADO);

        mockMvc.perform(patch("/api/kanban/projects/" + p.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransitionRequest(ProjectStatus.CONCLUIDO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"))
                .andExpect(jsonPath("$.actualEndDate").value(TODAY.toString()));
    }

    @Test
    void shouldRejectAtrasadoToAIniciarWithoutDateFix() throws Exception {
        long r = createResponsible("Ana", "ana@example.com");
        ProjectResponse p = createProject(new ProjectRequest("Obra",
                List.of(r), TODAY.minusDays(3), TODAY.plusDays(2), null, null));
        assertThat(p.status()).isEqualTo(ProjectStatus.ATRASADO);

        mockMvc.perform(patch("/api/kanban/projects/" + p.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransitionRequest(ProjectStatus.A_INICIAR))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("TRANSITION_NOT_ALLOWED"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("inicioPrevisto")));
    }

    // -------------------------------------------------------------------------
    // CONCLUIDO transitions
    // -------------------------------------------------------------------------

    @Test
    void shouldTransitionFromConcluidoToAtrasado() throws Exception {
        long r = createResponsible("Ana", "ana@example.com");
        // Create already concluded with past planned dates
        ProjectResponse p = createProject(new ProjectRequest("Obra",
                List.of(r), TODAY.minusDays(5), TODAY.minusDays(2), null, TODAY));
        assertThat(p.status()).isEqualTo(ProjectStatus.CONCLUIDO);

        mockMvc.perform(patch("/api/kanban/projects/" + p.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransitionRequest(ProjectStatus.ATRASADO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATRASADO"))
                .andExpect(jsonPath("$.actualEndDate").doesNotExist());
    }

    @Test
    void shouldTransitionFromConcluidoToEmAndamento() throws Exception {
        long r = createResponsible("Ana", "ana@example.com");
        ProjectResponse p = createProject(new ProjectRequest("Obra",
                List.of(r), TODAY.minusDays(3), TODAY.plusDays(5), TODAY, TODAY));
        assertThat(p.status()).isEqualTo(ProjectStatus.CONCLUIDO);

        mockMvc.perform(patch("/api/kanban/projects/" + p.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransitionRequest(ProjectStatus.EM_ANDAMENTO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.actualEndDate").doesNotExist());
    }

    @Test
    void shouldRejectConcluidoToEmAndamentoWhenEndDatePast() throws Exception {
        long r = createResponsible("Ana", "ana@example.com");
        ProjectResponse p = createProject(new ProjectRequest("Obra",
                List.of(r), TODAY.minusDays(5), TODAY.minusDays(2), null, TODAY));
        assertThat(p.status()).isEqualTo(ProjectStatus.CONCLUIDO);

        mockMvc.perform(patch("/api/kanban/projects/" + p.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransitionRequest(ProjectStatus.EM_ANDAMENTO))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("TRANSITION_NOT_ALLOWED"));
    }

    @Test
    void shouldRejectTransitionWithNullStatus() throws Exception {
        long r = createResponsible("Ana", "ana@example.com");
        ProjectResponse p = createProject(new ProjectRequest("Obra", List.of(r), null, null, null, null));

        mockMvc.perform(patch("/api/kanban/projects/" + p.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetStatus\": null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404ForNonExistentProject() throws Exception {
        mockMvc.perform(patch("/api/kanban/projects/9999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransitionRequest(ProjectStatus.CONCLUIDO))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldBeIdempotentWhenTargetEqualsCurrentStatus() throws Exception {
        long r = createResponsible("Ana", "ana@example.com");
        ProjectResponse p = createProject(new ProjectRequest("Obra", List.of(r), null, null, null, null));
        assertThat(p.status()).isEqualTo(ProjectStatus.A_INICIAR);

        // Sending the same status returns 200 without changing anything
        mockMvc.perform(patch("/api/kanban/projects/" + p.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransitionRequest(ProjectStatus.A_INICIAR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("A_INICIAR"));
    }
}
