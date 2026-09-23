package br.com.alvar.kanban;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class KanbanApplicationIT extends ApiIntegrationTest {

    @Test
    void shouldPublishIndicatorEndpointInOpenApi() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/indicators/projects-by-status']").exists());
    }

    @Autowired
    private Flyway flyway;

    @Test
    void shouldReportHealthyApplicationWithPostgres() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void shouldExposePrometheusMetrics() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/plain"))
                .andExpect(content().string(containsString("application=\"kanban-api\"")))
                .andExpect(content().string(containsString("jvm_info")));
    }

    @Test
    void shouldApplyMigrationsToEmptyPostgresAndAvoidReapplyingThem() {
        String schemaName = jdbcTemplate.queryForObject(
                "SELECT schema_name FROM information_schema.schemata WHERE schema_name = 'kanban'",
                String.class);

        assertThat(schemaName).isEqualTo("kanban");
        assertThat(flyway.info().applied())
                .extracting(migration -> migration.getVersion().getVersion())
                .contains("1", "2", "3", "4");
        Integer filterIndexCount = jdbcTemplate.queryForObject("""
                SELECT count(*) FROM pg_indexes
                WHERE schemaname = 'kanban'
                  AND indexname IN ('responsiblesByDepartment', 'projectsByPlannedStartDate',
                                    'projectsByPlannedEndDate', 'projectsByActualStartDate',
                                    'projectsByActualEndDate')
                """, Integer.class);
        assertThat(filterIndexCount).isEqualTo(5);
        assertThat(flyway.info().pending()).isEmpty();
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
        assertThat(flyway.migrate().migrationsExecuted).isZero();
    }

    @Test
    void shouldPublishOpenApiSchemasEndpointsAndErrors() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Kanban API"))
                .andExpect(jsonPath("$.paths['/api/projects']").exists())
                .andExpect(jsonPath("$.paths['/api/projects'].get.parameters[?(@.name == 'sort')]").exists())
                .andExpect(jsonPath("$.paths['/api/projects'].get.parameters[?(@.name == 'all')]").isEmpty())
                .andExpect(jsonPath("$.paths['/api/projects'].get.parameters[?(@.name == 'empty')]").isEmpty())
                .andExpect(jsonPath("$.paths['/api/kanban/projects/{id}/status'].patch.responses['422']").exists())
                .andExpect(jsonPath("$.components.schemas.ApiError").exists())
                .andExpect(jsonPath("$.components.schemas.ApiError.example").exists())
                .andExpect(jsonPath("$.components.schemas.ProjectRequest.example").exists())
                .andExpect(jsonPath("$.components.schemas.ProjectResponse.example").exists());

        mockMvc.perform(get("/swagger-ui"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
    }
}
