package br.com.alvar.kanban.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResponsibleRequest(
        @NotBlank(message = "Nome deve ser preenchido.") @Size(max = 120) String name,
        @NotBlank(message = "E-mail deve ser preenchido.") @Email(message = "E-mail inválido.") @Size(max = 254) String email,
        @NotBlank(message = "Cargo deve ser preenchido.") @Size(max = 120) String role,
        @NotBlank(message = "Secretaria deve ser preenchida.") @Size(max = 120) String department) {
}
