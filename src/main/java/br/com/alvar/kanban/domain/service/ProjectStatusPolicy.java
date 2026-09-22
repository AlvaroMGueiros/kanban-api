package br.com.alvar.kanban.domain.service;

import java.time.LocalDate;

import br.com.alvar.kanban.domain.model.ProjectSchedule;
import br.com.alvar.kanban.domain.model.ProjectStatus;

public final class ProjectStatusPolicy {
    private ProjectStatusPolicy() {
    }

    public static ProjectStatus calculate(ProjectSchedule schedule, LocalDate today) {
        schedule.validateAt(today);
        if (schedule.getActualEndDate() != null) {
            return ProjectStatus.CONCLUIDO;
        }
        boolean startOverdue = schedule.getActualStartDate() == null
                && schedule.getPlannedStartDate() != null
                && schedule.getPlannedStartDate().isBefore(today);
        boolean endOverdue = schedule.getPlannedEndDate() != null
                && schedule.getPlannedEndDate().isBefore(today);
        if (startOverdue || endOverdue) {
            return ProjectStatus.ATRASADO;
        }
        if (schedule.getActualStartDate() != null) {
            return ProjectStatus.EM_ANDAMENTO;
        }
        return ProjectStatus.A_INICIAR;
    }
}
