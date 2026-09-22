package br.com.alvar.kanban;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class KanbanApplicationIT extends ApiIntegrationTest {

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
    void shouldApplyMigrationsToEmptyPostgresAndAvoidReapplyingThem() {
        String schemaName = jdbcTemplate.queryForObject(
                "SELECT schema_name FROM information_schema.schemata WHERE schema_name = 'kanban'",
                String.class);

        assertThat(schemaName).isEqualTo("kanban");
        assertThat(flyway.info().applied())
                .extracting(migration -> migration.getVersion().getVersion())
                .contains("1");
        assertThat(flyway.info().pending()).isEmpty();
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
        assertThat(flyway.migrate().migrationsExecuted).isZero();
    }
}
