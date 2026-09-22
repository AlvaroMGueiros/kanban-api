package br.com.alvar.kanban.application.dto;

import br.com.alvar.kanban.domain.model.ProjectStatus;

public record ProjectFilters(ProjectStatus status, Long responsibleId, String department, String text) {
    public static ProjectFilters empty() {
        return new ProjectFilters(null, null, null, null);
    }
}
