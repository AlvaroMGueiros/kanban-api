package br.com.alvar.kanban.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import br.com.alvar.kanban.domain.model.ProjectMetrics;
import br.com.alvar.kanban.domain.model.ProjectSchedule;

public final class ProjectMetricsCalculator {
    private ProjectMetricsCalculator() {
    }

    public static ProjectMetrics calculate(ProjectSchedule schedule, LocalDate today) {
        if (schedule.getActualEndDate() != null) {
            return new ProjectMetrics(0, BigDecimal.ZERO);
        }
        long delayDays = 0;
        if (schedule.getPlannedEndDate() != null && schedule.getPlannedEndDate().isBefore(today)) {
            delayDays = ChronoUnit.DAYS.between(schedule.getPlannedEndDate(), today);
        }
        return new ProjectMetrics(delayDays, remainingPercentage(schedule, today));
    }

    private static BigDecimal remainingPercentage(ProjectSchedule schedule, LocalDate today) {
        if (schedule.getPlannedStartDate() == null || schedule.getPlannedEndDate() == null) {
            return BigDecimal.ZERO;
        }
        long totalDays = ChronoUnit.DAYS.between(schedule.getPlannedStartDate(), schedule.getPlannedEndDate());
        if (totalDays <= 0) {
            return BigDecimal.ZERO;
        }
        long remainingDays = ChronoUnit.DAYS.between(today, schedule.getPlannedEndDate());
        long boundedRemainingDays = Math.clamp(remainingDays, 0, totalDays);
        return BigDecimal.valueOf(boundedRemainingDays).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP);
    }
}
