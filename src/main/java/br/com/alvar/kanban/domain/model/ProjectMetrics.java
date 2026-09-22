package br.com.alvar.kanban.domain.model;

import java.math.BigDecimal;

public record ProjectMetrics(long delayDays, BigDecimal remainingTimePercentage) {
}
