package br.com.alvar.kanban.application.dto;

import br.com.alvar.kanban.domain.model.ProjectStatus;
import jakarta.validation.constraints.NotNull;

public record TransitionRequest(
        @NotNull(message = "O status de destino deve ser informado.") ProjectStatus targetStatus) {
}
