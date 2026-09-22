package br.com.alvar.kanban.domain.service;

import java.time.LocalDate;
import java.util.stream.Stream;

import br.com.alvar.kanban.domain.model.ProjectMetrics;
import br.com.alvar.kanban.domain.model.ProjectSchedule;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectMetricsCalculatorTest {
    private static final LocalDate today = LocalDate.of(2026, 9, 22);

    @ParameterizedTest(name = "{0}")
    @MethodSource("metricsCases")
    void shouldCalculateTimingBoundaries(String scenario, ProjectSchedule schedule, long delay, String percentage) {
        ProjectMetrics metrics = ProjectMetricsCalculator.calculate(schedule, today);
        assertThat(metrics.delayDays()).isEqualTo(delay);
        assertThat(metrics.remainingTimePercentage()).isEqualByComparingTo(percentage);
    }

    static Stream<Arguments> metricsCases() {
        return Stream.of(
                Arguments.of("sem datas", ProjectSchedule.empty(), 0L, "0"),
                Arguments.of("sem início previsto", new ProjectSchedule(null, today.plusDays(5), null, null), 0L, "0"),
                Arguments.of("sem término previsto", new ProjectSchedule(today, null, null, null), 0L, "0"),
                Arguments.of("duração zero", new ProjectSchedule(today, today, null, null), 0L, "0"),
                Arguments.of("antes do início", new ProjectSchedule(today.plusDays(1), today.plusDays(5), null, null), 0L, "100"),
                Arguments.of("no início", new ProjectSchedule(today, today.plusDays(5), null, null), 0L, "100"),
                Arguments.of("metade", new ProjectSchedule(today.minusDays(5), today.plusDays(5), today, null), 0L, "50"),
                Arguments.of("arredondamento", new ProjectSchedule(today.minusDays(1), today.plusDays(2), today, null), 0L, "66.67"),
                Arguments.of("vence hoje", new ProjectSchedule(today.minusDays(5), today, today, null), 0L, "0"),
                Arguments.of("venceu ontem", new ProjectSchedule(today.minusDays(5), today.minusDays(1), null, null), 1L, "0"),
                Arguments.of("atraso sem início previsto", new ProjectSchedule(null, today.minusDays(3), null, null), 3L, "0"),
                Arguments.of("concluído após prazo", new ProjectSchedule(today.minusDays(5), today.minusDays(2), null, today), 0L, "0"),
                Arguments.of("concluído antes prazo", new ProjectSchedule(today.minusDays(5), today.plusDays(5), null, today), 0L, "0"),
                Arguments.of("atrasado por início", new ProjectSchedule(today.minusDays(1), today.plusDays(1), null, null), 0L, "50"));
    }

    @ParameterizedTest
    @MethodSource("calendarCases")
    void shouldCountCalendarDays(LocalDate endDate, LocalDate referenceDate, long expectedDelay) {
        ProjectMetrics metrics = ProjectMetricsCalculator.calculate(new ProjectSchedule(null, endDate, null, null), referenceDate);
        assertThat(metrics.delayDays()).isEqualTo(expectedDelay);
    }

    static Stream<Arguments> calendarCases() {
        return Stream.of(
                Arguments.of(LocalDate.of(2024, 2, 28), LocalDate.of(2024, 3, 1), 2L),
                Arguments.of(LocalDate.of(2025, 12, 31), LocalDate.of(2026, 1, 1), 1L));
    }
}
