package br.com.alvar.kanban.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record ResponsibleRequest(
        @Schema(example = "Ana Silva")
        @NotBlank(message = "Nome deve ser preenchido.") @Size(max = 120) String name,
        @Schema(example = "ana.silva@example.com")
        @NotBlank(message = "E-mail deve ser preenchido.") @Email(message = "E-mail inválido.") @Size(max = 254) String email,
        @Schema(example = "Gerente de projetos")
        @NotBlank(message = "Cargo deve ser preenchido.") @Size(max = 120) String role,
        @Schema(example = "Planejamento")
        @NotBlank(message = "Secretaria deve ser preenchida.") @Size(max = 120) String department) {
}
