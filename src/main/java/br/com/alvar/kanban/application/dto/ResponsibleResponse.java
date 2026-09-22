package br.com.alvar.kanban.application.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Responsável cadastrado",
        example = "{\"id\":1,\"name\":\"Ana Silva\",\"email\":\"ana.silva@example.com\","
                + "\"role\":\"Gerente de projetos\",\"department\":\"Planejamento\","
                + "\"createdAt\":\"2026-09-22T15:00:00Z\",\"updatedAt\":\"2026-09-22T15:00:00Z\"}")
public record ResponsibleResponse(Long id, String name, String email, String role,
                                  String department, Instant createdAt, Instant updatedAt) {
}
