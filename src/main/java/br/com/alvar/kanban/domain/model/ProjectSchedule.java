package br.com.alvar.kanban.domain.model;

import java.time.LocalDate;

import br.com.alvar.kanban.domain.exception.BusinessRuleException;
import jakarta.persistence.Embeddable;

@Embeddable
public class ProjectSchedule {
    private static final ProjectSchedule emptySchedule = new ProjectSchedule(null, null, null, null);

    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;

    protected ProjectSchedule() {
    }

    public ProjectSchedule(LocalDate plannedStartDate, LocalDate plannedEndDate,
                           LocalDate actualStartDate, LocalDate actualEndDate) {
        if (plannedStartDate != null && plannedEndDate != null && plannedEndDate.isBefore(plannedStartDate)) {
            throw new BusinessRuleException("Término previsto não pode ser anterior ao início previsto.");
        }
        if (actualStartDate != null && actualEndDate != null && actualEndDate.isBefore(actualStartDate)) {
            throw new BusinessRuleException("Término realizado não pode ser anterior ao início realizado.");
        }
        this.plannedStartDate = plannedStartDate;
        this.plannedEndDate = plannedEndDate;
        this.actualStartDate = actualStartDate;
        this.actualEndDate = actualEndDate;
    }

    public static ProjectSchedule empty() {
        return emptySchedule;
    }

    public void validateAt(LocalDate today) {
        if (actualStartDate != null && actualStartDate.isAfter(today)) {
            throw new BusinessRuleException("Início realizado não pode estar no futuro.");
        }
        if (actualEndDate != null && actualEndDate.isAfter(today)) {
            throw new BusinessRuleException("Término realizado não pode estar no futuro.");
        }
    }

    public ProjectSchedule withActualStartDate(LocalDate startDate) {
        return new ProjectSchedule(plannedStartDate, plannedEndDate, startDate, actualEndDate);
    }

    public ProjectSchedule withActualEndDate(LocalDate endDate) {
        return new ProjectSchedule(plannedStartDate, plannedEndDate, actualStartDate, endDate);
    }

    public LocalDate getPlannedStartDate() { return plannedStartDate; }
    public LocalDate getPlannedEndDate() { return plannedEndDate; }
    public LocalDate getActualStartDate() { return actualStartDate; }
    public LocalDate getActualEndDate() { return actualEndDate; }
}
