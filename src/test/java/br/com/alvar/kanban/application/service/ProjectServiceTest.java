package br.com.alvar.kanban.application.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import br.com.alvar.kanban.domain.model.Project;
import br.com.alvar.kanban.domain.model.ProjectSchedule;
import br.com.alvar.kanban.domain.model.ProjectStatus;
import br.com.alvar.kanban.domain.model.Responsible;
import br.com.alvar.kanban.infrastructure.repository.ProjectRepository;
import br.com.alvar.kanban.infrastructure.repository.ResponsibleRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProjectServiceTest {
    @Test
    void shouldRecalculateStatusWhenDayChangesWithoutEditingProject() {
        LocalDate startDate = LocalDate.of(2026, 9, 22);
        Project project = new Project("Obra", new ProjectSchedule(startDate, startDate.plusDays(3), null, null),
                List.of(new Responsible("Ana", "ana@example.com", "Analista", "Obras")), startDate);
        ProjectRepository projects = mock(ProjectRepository.class);
        ResponsibleRepository responsibles = mock(ResponsibleRepository.class);
        when(projects.findRequired(1L)).thenReturn(project);

        Clock firstDay = Clock.fixed(Instant.parse("2026-09-22T15:00:00Z"), ZoneId.of("America/Fortaleza"));
        Clock nextDay = Clock.offset(firstDay, java.time.Duration.ofDays(1));
        assertThat(new ProjectService(projects, responsibles, firstDay).find(1L).status()).isEqualTo(ProjectStatus.A_INICIAR);
        assertThat(new ProjectService(projects, responsibles, nextDay).find(1L).status()).isEqualTo(ProjectStatus.ATRASADO);
    }

    @Test
    void shouldUseBusinessTimeZoneNearMidnightUtc() {
        LocalDate localDay = LocalDate.of(2026, 9, 21);
        Project project = new Project("Obra", new ProjectSchedule(localDay, localDay.plusDays(3), null, null),
                List.of(new Responsible("Ana", "ana@example.com", "Analista", "Obras")), localDay);
        ProjectRepository projects = mock(ProjectRepository.class);
        when(projects.findRequired(1L)).thenReturn(project);
        Clock clock = Clock.fixed(Instant.parse("2026-09-22T01:00:00Z"), ZoneId.of("America/Fortaleza"));
        assertThat(new ProjectService(projects, mock(ResponsibleRepository.class), clock).find(1L).status())
                .isEqualTo(ProjectStatus.A_INICIAR);
    }
}
