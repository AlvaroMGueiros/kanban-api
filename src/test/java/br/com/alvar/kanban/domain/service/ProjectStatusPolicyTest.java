package br.com.alvar.kanban.domain.service;

import java.time.LocalDate;
import java.util.stream.Stream;

import br.com.alvar.kanban.domain.model.ProjectSchedule;
import br.com.alvar.kanban.domain.model.ProjectStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectStatusPolicyTest {
    private static final LocalDate today = LocalDate.of(2026, 9, 22);

    @ParameterizedTest(name = "{0}")
    @MethodSource("statusCases")
    void shouldCalculateWithExplicitPrecedence(String scenario, ProjectSchedule schedule, ProjectStatus expected) {
        assertThat(ProjectStatusPolicy.calculate(schedule, today)).isEqualTo(expected);
    }

    static Stream<Arguments> statusCases() {
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);
        return Stream.of(
                Arguments.of("sem datas", ProjectSchedule.empty(), ProjectStatus.A_INICIAR),
                Arguments.of("planejado no futuro", new ProjectSchedule(tomorrow, tomorrow, null, null), ProjectStatus.A_INICIAR),
                Arguments.of("início previsto hoje", new ProjectSchedule(today, tomorrow, null, null), ProjectStatus.A_INICIAR),
                Arguments.of("início vencido", new ProjectSchedule(yesterday, tomorrow, null, null), ProjectStatus.ATRASADO),
                Arguments.of("término vencido sem início", new ProjectSchedule(null, yesterday, null, null), ProjectStatus.ATRASADO),
                Arguments.of("iniciado com prazo vencido", new ProjectSchedule(null, yesterday, yesterday, null), ProjectStatus.ATRASADO),
                Arguments.of("iniciado dentro do prazo", new ProjectSchedule(yesterday, tomorrow, yesterday, null), ProjectStatus.EM_ANDAMENTO),
                Arguments.of("iniciado vence hoje", new ProjectSchedule(yesterday, today, yesterday, null), ProjectStatus.EM_ANDAMENTO),
                Arguments.of("iniciado sem previsão", new ProjectSchedule(null, null, today, null), ProjectStatus.EM_ANDAMENTO),
                Arguments.of("conclusão prevalece ao atraso", new ProjectSchedule(yesterday, yesterday, null, today), ProjectStatus.CONCLUIDO),
                Arguments.of("conclusão com início", new ProjectSchedule(yesterday, yesterday, yesterday, today), ProjectStatus.CONCLUIDO),
                Arguments.of("conclusão sem previsão e início", new ProjectSchedule(null, null, null, today), ProjectStatus.CONCLUIDO),
                Arguments.of("sem início com prazo hoje", new ProjectSchedule(null, today, null, null), ProjectStatus.A_INICIAR),
                Arguments.of("início vencido com prazo hoje", new ProjectSchedule(yesterday, today, null, null), ProjectStatus.ATRASADO));
    }
}
