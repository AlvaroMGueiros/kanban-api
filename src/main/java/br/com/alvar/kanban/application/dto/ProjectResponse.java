package br.com.alvar.kanban.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import br.com.alvar.kanban.domain.model.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Projeto com status e métricas calculados",
        example = "{\"id\":1,\"name\":\"Reforma da escola\",\"status\":\"A_INICIAR\","
                + "\"responsibles\":[],\"plannedStartDate\":\"2026-10-01\","
                + "\"plannedEndDate\":\"2026-12-20\",\"actualStartDate\":null,"
                + "\"actualEndDate\":null,\"delayDays\":0,\"remainingTimePercentage\":100.00,"
                + "\"createdAt\":\"2026-09-22T15:00:00Z\",\"updatedAt\":\"2026-09-22T15:00:00Z\"}")
public record ProjectResponse(Long id, String name, ProjectStatus status, List<ResponsibleResponse> responsibles,
                              LocalDate plannedStartDate, LocalDate plannedEndDate,
                              LocalDate actualStartDate, LocalDate actualEndDate,
                              long delayDays, BigDecimal remainingTimePercentage,
                              Instant createdAt, Instant updatedAt) {
}
