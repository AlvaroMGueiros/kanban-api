package br.com.alvar.kanban.domain.model;

import java.time.LocalDate;

import br.com.alvar.kanban.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectScheduleTest {
    private final LocalDate today = LocalDate.of(2026, 9, 22);

    @Test
    void shouldRejectReversedPlannedDates() {
        assertThatThrownBy(() -> new ProjectSchedule(today, today.minusDays(1), null, null))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("Término previsto");
    }

    @Test
    void shouldRejectReversedActualDates() {
        assertThatThrownBy(() -> new ProjectSchedule(null, null, today, today.minusDays(1)))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("Término realizado");
    }

    @Test
    void shouldRejectFutureActualStart() {
        assertThatThrownBy(() -> new ProjectSchedule(null, null, today.plusDays(1), null).validateAt(today))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("Início realizado");
    }

    @Test
    void shouldRejectFutureActualEnd() {
        assertThatThrownBy(() -> new ProjectSchedule(null, null, null, today.plusDays(1)).validateAt(today))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("Término realizado");
    }

    @Test
    void shouldAllowZeroDurationNullDatesAndDirectCompletion() {
        assertThatCode(() -> {
            new ProjectSchedule(today, today, today, today).validateAt(today);
            ProjectSchedule.empty().validateAt(today);
            new ProjectSchedule(null, null, null, today).validateAt(today);
            new ProjectSchedule(today.plusDays(1), today.plusDays(2), null, today).validateAt(today);
        }).doesNotThrowAnyException();
    }
}
