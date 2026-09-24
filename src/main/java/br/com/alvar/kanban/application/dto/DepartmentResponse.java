package br.com.alvar.kanban.application.dto;

import java.time.Instant;

public record DepartmentResponse(Long id, String name, Instant createdAt, Instant updatedAt) {
}
