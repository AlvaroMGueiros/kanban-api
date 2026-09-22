package br.com.alvar.kanban.infrastructure.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import br.com.alvar.kanban.application.dto.ProjectFilters;
import br.com.alvar.kanban.domain.model.Project;
import br.com.alvar.kanban.domain.model.ProjectStatus;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.JoinType;
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

    public static Specification<Project> withFilters(ProjectFilters filters, LocalDate today) {
        Specification<Project> specification = Specification.unrestricted();
        if (filters.status() != null) {
            specification = specification.and(hasStatus(filters.status(), today));
        }
        if (filters.responsibleId() != null || hasText(filters.department())) {
            specification = specification.and((root, query, builder) -> {
                var responsibles = root.join("responsibles", JoinType.INNER);
                query.distinct(true);
                List<Predicate> predicates = new ArrayList<>(2);
                if (filters.responsibleId() != null) {
                    predicates.add(builder.equal(responsibles.get("id"), filters.responsibleId()));
                }
                if (hasText(filters.department())) {
                    predicates.add(builder.equal(builder.lower(responsibles.get("department")),
                            filters.department().strip().toLowerCase(java.util.Locale.ROOT)));
                }
                return builder.and(predicates.toArray(Predicate[]::new));
            });
        }
        if (hasText(filters.text())) {
            specification = specification.and((root, query, builder) -> builder.like(
                    builder.lower(root.get("name")), "%" + escapeLike(filters.text().strip()
                            .toLowerCase(java.util.Locale.ROOT)) + "%", '\\'));
        }
        return specification;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
