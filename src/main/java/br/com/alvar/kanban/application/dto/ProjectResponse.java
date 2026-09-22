package br.com.alvar.kanban.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import br.com.alvar.kanban.domain.model.ProjectStatus;

public record ProjectResponse(Long id, String name, ProjectStatus status, List<ResponsibleResponse> responsibles,
                              LocalDate plannedStartDate, LocalDate plannedEndDate,
                              LocalDate actualStartDate, LocalDate actualEndDate,
                              long delayDays, BigDecimal remainingTimePercentage,
                              Instant createdAt, Instant updatedAt) {
}
