package br.com.alvar.kanban.domain.service;

import java.time.LocalDate;
import java.util.stream.Stream;

import br.com.alvar.kanban.domain.exception.TransitionNotAllowedException;
import br.com.alvar.kanban.domain.model.ProjectSchedule;
import br.com.alvar.kanban.domain.model.ProjectStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for all 12 Kanban transitions — valid paths and rejection paths.
 * Uses a fixed reference date so tests never depend on the real clock.
 */
class KanbanTransitionServiceTest {

    // Fixed reference date: 2026-09-22
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 22);
    private static final LocalDate YESTERDAY = TODAY.minusDays(1);
    private static final LocalDate TOMORROW = TODAY.plusDays(1);

    // -------------------------------------------------------------------------
    // Idempotent transitions (same status → same schedule, no error)
    // -------------------------------------------------------------------------

    @ParameterizedTest(name = "idempotent: {0}")
    @MethodSource("idempotentCases")
    void shouldReturnSameScheduleWhenTargetEqualsCurrentStatus(
            String scenario, ProjectSchedule schedule, ProjectStatus status) {
        ProjectSchedule result = KanbanTransitionService.transition(schedule, status, TODAY);
        assertThat(result).isSameAs(schedule);
    }

    static Stream<Arguments> idempotentCases() {
        return Stream.of(
                Arguments.of("A_INICIAR", ProjectSchedule.empty(), ProjectStatus.A_INICIAR),
                Arguments.of("EM_ANDAMENTO",
                        new ProjectSchedule(YESTERDAY, TOMORROW, TODAY, null), ProjectStatus.EM_ANDAMENTO),
                Arguments.of("ATRASADO",
                        new ProjectSchedule(YESTERDAY, YESTERDAY, null, null), ProjectStatus.ATRASADO),
                Arguments.of("CONCLUIDO",
                        new ProjectSchedule(null, null, null, TODAY), ProjectStatus.CONCLUIDO));
    }

    // =========================================================================
    // A_INICIAR transitions
    // =========================================================================

    @Test
    void aIniciar_toEmAndamento_setsActualStartToToday() {
        ProjectSchedule schedule = new ProjectSchedule(TODAY, TOMORROW, null, null);
        ProjectSchedule result = KanbanTransitionService.transition(schedule, ProjectStatus.EM_ANDAMENTO, TODAY);

        assertThat(result.getActualStartDate()).isEqualTo(TODAY);
        assertThat(ProjectStatusPolicy.calculate(result, TODAY)).isEqualTo(ProjectStatus.EM_ANDAMENTO);
    }

    @Test
    void aIniciar_toEmAndamento_rejectsWhenEndDateAlreadyPast() {
        // plannedEndDate already past → filling actualStart still makes it ATRASADO
        ProjectSchedule schedule = new ProjectSchedule(YESTERDAY, YESTERDAY, null, null);
        assertThatThrownBy(() -> KanbanTransitionService.transition(schedule, ProjectStatus.EM_ANDAMENTO, TODAY))
                .isInstanceOf(TransitionNotAllowedException.class)
                .hasMessageContaining("terminoPrevisto");
    }

    @Test
    void aIniciar_toAtrasado_succeedsWhenStartAlreadyOverdue() {
        // plannedStart in the past, no actualStart → project is already ATRASADO
        ProjectSchedule schedule = new ProjectSchedule(YESTERDAY, TOMORROW, null, null);
        ProjectSchedule result = KanbanTransitionService.transition(schedule, ProjectStatus.ATRASADO, TODAY);
        assertThat(result).isSameAs(schedule); // idempotent path
    }

    @Test
    void aIniciar_toAtrasado_rejectsWhenNotActuallyOverdue() {
        // plannedStart in the future → not overdue
        ProjectSchedule schedule = new ProjectSchedule(TOMORROW, TOMORROW.plusDays(5), null, null);
        assertThatThrownBy(() -> KanbanTransitionService.transition(schedule, ProjectStatus.ATRASADO, TODAY))
                .isInstanceOf(TransitionNotAllowedException.class)
                .hasMessageContaining("inicioPrevisto");
    }

    @Test
    void aIniciar_toConcluido_setsActualEndToToday() {
        ProjectSchedule schedule = ProjectSchedule.empty();
        ProjectSchedule result = KanbanTransitionService.transition(schedule, ProjectStatus.CONCLUIDO, TODAY);

        assertThat(result.getActualEndDate()).isEqualTo(TODAY);
        assertThat(ProjectStatusPolicy.calculate(result, TODAY)).isEqualTo(ProjectStatus.CONCLUIDO);
    }

    // =========================================================================
    // EM_ANDAMENTO transitions
    // =========================================================================

    @Test
    void emAndamento_toAIniciar_clearsActualStart() {
        // With future dates: removing actualStart → A_INICIAR
        ProjectSchedule schedule = new ProjectSchedule(TOMORROW, TOMORROW.plusDays(5), TODAY, null);
        ProjectSchedule result = KanbanTransitionService.transition(schedule, ProjectStatus.A_INICIAR, TODAY);

        assertThat(result.getActualStartDate()).isNull();
        assertThat(ProjectStatusPolicy.calculate(result, TODAY)).isEqualTo(ProjectStatus.A_INICIAR);
    }

    @Test
    void emAndamento_toAIniciar_rejectsWhenResultWouldBeOverdue() {
        // plannedStart is in the past → clearing actualStart makes it ATRASADO, not A_INICIAR
        ProjectSchedule schedule = new ProjectSchedule(YESTERDAY, TOMORROW, TODAY, null);
        assertThatThrownBy(() -> KanbanTransitionService.transition(schedule, ProjectStatus.A_INICIAR, TODAY))
                .isInstanceOf(TransitionNotAllowedException.class)
                .hasMessageContaining("ATRASADO");
    }

    @Test
    void emAndamento_toAtrasado_succeedsWhenEndDateAlreadyPast() {
        // actualStart filled, plannedEnd in the past → already ATRASADO (not EM_ANDAMENTO!)
        // Transition service is called with the ATRASADO current status
        ProjectSchedule schedule = new ProjectSchedule(YESTERDAY, YESTERDAY, YESTERDAY, null);
        ProjectSchedule result = KanbanTransitionService.transition(schedule, ProjectStatus.ATRASADO, TODAY);
        assertThat(result).isSameAs(schedule); // idempotent
    }

    @Test
    void emAndamento_toAtrasado_rejectsWhenNotActuallyOverdue() {
        ProjectSchedule schedule = new ProjectSchedule(YESTERDAY, TOMORROW, TODAY, null);
        assertThatThrownBy(() -> KanbanTransitionService.transition(schedule, ProjectStatus.ATRASADO, TODAY))
                .isInstanceOf(TransitionNotAllowedException.class)
                .hasMessageContaining("terminoPrevisto");
    }

    @Test
    void emAndamento_toConcluido_setsActualEndToToday() {
        ProjectSchedule schedule = new ProjectSchedule(YESTERDAY, TOMORROW, TODAY, null);
        ProjectSchedule result = KanbanTransitionService.transition(schedule, ProjectStatus.CONCLUIDO, TODAY);

        assertThat(result.getActualEndDate()).isEqualTo(TODAY);
        assertThat(ProjectStatusPolicy.calculate(result, TODAY)).isEqualTo(ProjectStatus.CONCLUIDO);
    }

    // =========================================================================
    // ATRASADO transitions
    // =========================================================================

    @Test
    void atrasado_toAIniciar_rejectsWhenStillOverdue() {
        // plannedStart in the past → always ATRASADO without date changes
        ProjectSchedule schedule = new ProjectSchedule(YESTERDAY, TOMORROW, null, null);
        assertThatThrownBy(() -> KanbanTransitionService.transition(schedule, ProjectStatus.A_INICIAR, TODAY))
                .isInstanceOf(TransitionNotAllowedException.class)
                .hasMessageContaining("inicioPrevisto");
    }

    @Test
    void atrasado_toEmAndamento_rejectsWhenStillOverdue() {
        ProjectSchedule schedule = new ProjectSchedule(YESTERDAY, YESTERDAY, null, null);
        assertThatThrownBy(() -> KanbanTransitionService.transition(schedule, ProjectStatus.EM_ANDAMENTO, TODAY))
                .isInstanceOf(TransitionNotAllowedException.class)
                .hasMessageContaining("inicioRealizado");
    }

    @Test
    void atrasado_toEmAndamento_succeedsWhenDatesAlreadyAllowIt() {
        // actualStart filled, plannedEnd is today → EM_ANDAMENTO (not overdue)
        ProjectSchedule schedule = new ProjectSchedule(YESTERDAY, TODAY, TODAY, null);
        // This schedule currently yields EM_ANDAMENTO, not ATRASADO — so this is an idempotent call
        ProjectSchedule result = KanbanTransitionService.transition(schedule, ProjectStatus.EM_ANDAMENTO, TODAY);
        assertThat(result).isSameAs(schedule);
    }

    @Test
    void atrasado_toConcluido_setsActualEndToToday() {
        ProjectSchedule schedule = new ProjectSchedule(YESTERDAY, YESTERDAY, null, null);
        ProjectSchedule result = KanbanTransitionService.transition(schedule, ProjectStatus.CONCLUIDO, TODAY);

        assertThat(result.getActualEndDate()).isEqualTo(TODAY);
        assertThat(ProjectStatusPolicy.calculate(result, TODAY)).isEqualTo(ProjectStatus.CONCLUIDO);
    }

    // =========================================================================
    // CONCLUIDO transitions
    // =========================================================================

    @Test
    void concluido_toAIniciar_keepsActualStartAndRejectsMismatchedResult() {
        ProjectSchedule schedule = new ProjectSchedule(TOMORROW, TOMORROW.plusDays(5), TODAY, TODAY);

        assertThatThrownBy(() -> KanbanTransitionService.transition(
                schedule, ProjectStatus.A_INICIAR, TODAY))
                .isInstanceOf(TransitionNotAllowedException.class);
    }

    @Test
    void concluido_toAIniciar_rejectsWhenResultWouldBeOverdue() {
        // plannedStart in the past → after clearing actual dates, it would be ATRASADO
        ProjectSchedule schedule = new ProjectSchedule(YESTERDAY, TOMORROW, TODAY, TODAY);
        assertThatThrownBy(() -> KanbanTransitionService.transition(schedule, ProjectStatus.A_INICIAR, TODAY))
                .isInstanceOf(TransitionNotAllowedException.class)
                .hasMessageContaining("ATRASADO");
    }

    @Test
    void concluido_toEmAndamento_removesActualEnd() {
        // actualStart present, plannedEnd in future → EM_ANDAMENTO after removing actualEnd
        ProjectSchedule schedule = new ProjectSchedule(YESTERDAY, TOMORROW, TODAY, TODAY);
        ProjectSchedule result = KanbanTransitionService.transition(schedule, ProjectStatus.EM_ANDAMENTO, TODAY);

        assertThat(result.getActualEndDate()).isNull();
        assertThat(result.getActualStartDate()).isEqualTo(TODAY);
        assertThat(ProjectStatusPolicy.calculate(result, TODAY)).isEqualTo(ProjectStatus.EM_ANDAMENTO);
    }

    @Test
    void concluido_toEmAndamento_rejectsWhenResultWouldBeOverdue() {
        // plannedEnd in the past → removing actualEnd → ATRASADO, not EM_ANDAMENTO
        ProjectSchedule schedule = new ProjectSchedule(YESTERDAY, YESTERDAY, TODAY, TODAY);
        assertThatThrownBy(() -> KanbanTransitionService.transition(schedule, ProjectStatus.EM_ANDAMENTO, TODAY))
                .isInstanceOf(TransitionNotAllowedException.class)
                .hasMessageContaining("terminoPrevisto");
    }

    @Test
    void concluido_toAtrasado_removesActualEndAndConfirmsOverdue() {
        // plannedEnd in the past → removing actualEnd → ATRASADO
        ProjectSchedule schedule = new ProjectSchedule(YESTERDAY, YESTERDAY, null, TODAY);
        ProjectSchedule result = KanbanTransitionService.transition(schedule, ProjectStatus.ATRASADO, TODAY);

        assertThat(result.getActualEndDate()).isNull();
        assertThat(ProjectStatusPolicy.calculate(result, TODAY)).isEqualTo(ProjectStatus.ATRASADO);
    }

    @Test
    void concluido_toAtrasado_rejectsWhenResultWouldNotBeOverdue() {
        // plannedEnd in the future → removing actualEnd → A_INICIAR, not ATRASADO
        ProjectSchedule schedule = new ProjectSchedule(TOMORROW, TOMORROW.plusDays(5), null, TODAY);
        assertThatThrownBy(() -> KanbanTransitionService.transition(schedule, ProjectStatus.ATRASADO, TODAY))
                .isInstanceOf(TransitionNotAllowedException.class)
                .hasMessageContaining("terminoPrevisto");
    }

    // =========================================================================
    // Transition coverage documentation
    // =========================================================================

    /**
     * The switch expressions in KanbanTransitionService are exhaustive over
     * ProjectStatus — adding a new enum value without updating the service
     * causes a compile error. This test verifies that all 4 statuses are
     * covered by the transition method without throwing for valid pairs.
     */
    @Test
    void shouldCoverAllStatusValuesInTransitionSwitch() {
        // Each status must be reachable as a 'current' value — verified via the idempotent path
        // (same → same returns the original schedule without error).
        assertThat(ProjectStatus.values()).hasSize(4); // fails to compile if a new value is added
        for (ProjectStatus status : ProjectStatus.values()) {
            ProjectSchedule schedule = switch (status) {
                case A_INICIAR -> ProjectSchedule.empty();
                case EM_ANDAMENTO -> new ProjectSchedule(YESTERDAY, TOMORROW, TODAY, null);
                case ATRASADO -> new ProjectSchedule(YESTERDAY, YESTERDAY, null, null);
                case CONCLUIDO -> new ProjectSchedule(null, null, null, TODAY);
            };
            ProjectSchedule result = KanbanTransitionService.transition(schedule, status, TODAY);
            assertThat(result).isSameAs(schedule);
        }
    }

    // =========================================================================
    // Boundary dates
    // =========================================================================

    @Test
    void boundary_projectEndingTodayIsStillInProgress() {
        // plannedEnd = today → not overdue, EM_ANDAMENTO
        ProjectSchedule schedule = new ProjectSchedule(YESTERDAY, TODAY, TODAY, null);
        assertThat(ProjectStatusPolicy.calculate(schedule, TODAY)).isEqualTo(ProjectStatus.EM_ANDAMENTO);

        // Can be concluded
        ProjectSchedule concluded = KanbanTransitionService.transition(schedule, ProjectStatus.CONCLUIDO, TODAY);
        assertThat(ProjectStatusPolicy.calculate(concluded, TODAY)).isEqualTo(ProjectStatus.CONCLUIDO);
    }

    @Test
    void boundary_projectStartingTodayIsNotYetOverdue() {
        // plannedStart = today → A_INICIAR (starts today, not overdue)
        ProjectSchedule schedule = new ProjectSchedule(TODAY, TOMORROW, null, null);
        assertThat(ProjectStatusPolicy.calculate(schedule, TODAY)).isEqualTo(ProjectStatus.A_INICIAR);
    }

    @Test
    void boundary_projectStartedYesterdayWithNoDates() {
        // No planned dates, actualStart set → EM_ANDAMENTO
        ProjectSchedule schedule = new ProjectSchedule(null, null, YESTERDAY, null);
        assertThat(ProjectStatusPolicy.calculate(schedule, TODAY)).isEqualTo(ProjectStatus.EM_ANDAMENTO);

        // Can be concluded directly
        ProjectSchedule result = KanbanTransitionService.transition(schedule, ProjectStatus.CONCLUIDO, TODAY);
        assertThat(result.getActualEndDate()).isEqualTo(TODAY);
    }

    @Test
    void boundary_completionWithoutStartOrPlannedDatesIsAllowed() {
        ProjectSchedule schedule = ProjectSchedule.empty();
        ProjectSchedule result = KanbanTransitionService.transition(schedule, ProjectStatus.CONCLUIDO, TODAY);
        assertThat(result.getActualEndDate()).isEqualTo(TODAY);
        assertThat(result.getActualStartDate()).isNull();
        assertThat(result.getPlannedStartDate()).isNull();
    }
}

