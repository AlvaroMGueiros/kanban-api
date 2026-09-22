package br.com.alvar.kanban.application.dto;

public record ProjectsByStatusResponse(
        long aIniciar,
        long emAndamento,
        long atrasado,
        long concluido) {
}
