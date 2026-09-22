package br.com.alvar.kanban.application.dto;

import java.time.Instant;

public record ResponsibleResponse(Long id, String name, String email, String role,
                                  String department, Instant createdAt, Instant updatedAt) {
}
