package br.com.alvar.kanban.infrastructure.repository;

import java.time.LocalDate;

import br.com.alvar.kanban.domain.model.Project;
import br.com.alvar.kanban.domain.model.ProjectStatus;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public final class ProjectSpecifications {
    private ProjectSpecifications() {
    }

    public static Specification<Project> hasStatus(ProjectStatus status, LocalDate today) {
        return (root, query, builder) -> {
            Path<LocalDate> plannedStart = root.get("schedule").get("plannedStartDate");
            Path<LocalDate> plannedEnd = root.get("schedule").get("plannedEndDate");
            Path<LocalDate> actualStart = root.get("schedule").get("actualStartDate");
            Path<LocalDate> actualEnd = root.get("schedule").get("actualEndDate");

            Predicate concluded = builder.isNotNull(actualEnd);
            Predicate startOverdue = builder.and(builder.isNull(actualStart), builder.isNotNull(plannedStart),
                    builder.lessThan(plannedStart, today));
            Predicate endOverdue = builder.and(builder.isNotNull(plannedEnd), builder.lessThan(plannedEnd, today));
            Predicate overdue = builder.and(builder.isNull(actualEnd), builder.or(startOverdue, endOverdue));

            return switch (status) {
                case CONCLUIDO -> concluded;
                case ATRASADO -> overdue;
                case EM_ANDAMENTO -> builder.and(builder.not(concluded), builder.not(overdue),
                        builder.isNotNull(actualStart));
                case A_INICIAR -> builder.and(builder.not(concluded), builder.not(overdue),
                        builder.isNull(actualStart));
            };
        };
    }
}
