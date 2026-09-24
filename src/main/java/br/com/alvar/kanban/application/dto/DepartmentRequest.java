package br.com.alvar.kanban.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        @NotBlank(message = "Nome da secretaria deve ser preenchido.") @Size(max = 120) String name) {
}
