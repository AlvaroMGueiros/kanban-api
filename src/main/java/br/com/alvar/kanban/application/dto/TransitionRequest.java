package br.com.alvar.kanban.application.dto;

import br.com.alvar.kanban.domain.model.ProjectStatus;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

public record TransitionRequest(
        @Schema(description = "Status solicitado; o resultado continua sendo derivado das datas",
                example = "EM_ANDAMENTO")
        @NotNull(message = "O status de destino deve ser informado.") ProjectStatus targetStatus) {
}
