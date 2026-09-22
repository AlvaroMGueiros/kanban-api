package br.com.alvar.kanban.application.mapper;

import java.time.LocalDate;

import br.com.alvar.kanban.application.dto.ProjectResponse;
import br.com.alvar.kanban.domain.model.Project;
import br.com.alvar.kanban.domain.model.ProjectMetrics;
import br.com.alvar.kanban.domain.model.ProjectSchedule;
import br.com.alvar.kanban.domain.service.ProjectMetricsCalculator;
import br.com.alvar.kanban.domain.service.ProjectStatusPolicy;

public final class ProjectMapper {
    private ProjectMapper() {
    }

    public static ProjectResponse toResponse(Project project, LocalDate today) {
        ProjectSchedule schedule = project.getSchedule();
        ProjectMetrics metrics = ProjectMetricsCalculator.calculate(schedule, today);
        return new ProjectResponse(project.getId(), project.getName(), ProjectStatusPolicy.calculate(schedule, today),
                project.getResponsibles().stream().map(ResponsibleMapper::toResponse).toList(),
                schedule.getPlannedStartDate(), schedule.getPlannedEndDate(),
                schedule.getActualStartDate(), schedule.getActualEndDate(),
                metrics.delayDays(), metrics.remainingTimePercentage(), project.getCreatedAt(), project.getUpdatedAt());
    }
}
