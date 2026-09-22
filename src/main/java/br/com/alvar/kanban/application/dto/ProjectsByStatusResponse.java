package br.com.alvar.kanban.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProjectsByStatusResponse(
        @JsonProperty("A_INICIAR")
        long aIniciar,
        @JsonProperty("EM_ANDAMENTO")
        long emAndamento,
        @JsonProperty("ATRASADO")
        long atrasado,
        @JsonProperty("CONCLUIDO")
        long concluido) {
}
