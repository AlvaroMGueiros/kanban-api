package br.com.alvar.kanban.application.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para criar ou atualizar um projeto",
        example = "{\"name\":\"Reforma da escola\",\"responsibleIds\":[1],"
                + "\"plannedStartDate\":\"2026-10-01\",\"plannedEndDate\":\"2026-12-20\","
                + "\"actualStartDate\":null,\"actualEndDate\":null}")
public record ProjectRequest(
        @Schema(example = "Reforma da escola")
        @NotBlank(message = "Nome do projeto deve ser preenchido.") @Size(max = 200) String name,
        @Schema(example = "[1, 2]")
        @NotEmpty(message = "Informe pelo menos um responsável.") @Size(max = 100) List<@NotNull @Positive Long> responsibleIds,
        @Schema(example = "2026-10-01")
        LocalDate plannedStartDate, LocalDate plannedEndDate,
        LocalDate actualStartDate, LocalDate actualEndDate) {
}
