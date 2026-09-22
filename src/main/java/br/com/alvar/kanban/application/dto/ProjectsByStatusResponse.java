package br.com.alvar.kanban.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

@io.swagger.v3.oas.annotations.media.Schema(description = "Quantidade de projetos por status",
        example = "{\"A_INICIAR\":10,\"EM_ANDAMENTO\":4,\"ATRASADO\":2,\"CONCLUIDO\":8}")
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
