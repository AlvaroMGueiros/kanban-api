package br.com.alvar.kanban.presentation.exception;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Erro padronizado da API",
        example = "{\"timestamp\":\"2026-09-22T15:00:00Z\",\"status\":422,"
                + "\"error\":\"TRANSITION_NOT_ALLOWED\",\"message\":\"Ajuste as datas do projeto.\","
                + "\"path\":\"/api/kanban/projects/1/status\"}")
public record ApiError(Instant timestamp, int status, String error, String message, String path) {
}
