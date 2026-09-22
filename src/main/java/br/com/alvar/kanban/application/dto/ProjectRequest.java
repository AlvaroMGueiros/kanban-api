package br.com.alvar.kanban.application.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProjectRequest(
        @NotBlank(message = "Nome do projeto deve ser preenchido.") @Size(max = 200) String name,
        @NotEmpty(message = "Informe pelo menos um responsável.") @Size(max = 100) List<@NotNull @Positive Long> responsibleIds,
        LocalDate plannedStartDate, LocalDate plannedEndDate,
        LocalDate actualStartDate, LocalDate actualEndDate) {
}
