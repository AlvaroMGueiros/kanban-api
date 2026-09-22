package br.com.alvar.kanban.application.mapper;

import br.com.alvar.kanban.application.dto.ResponsibleResponse;
import br.com.alvar.kanban.domain.model.Responsible;

public final class ResponsibleMapper {
    private ResponsibleMapper() {
    }

    public static ResponsibleResponse toResponse(Responsible responsible) {
        return new ResponsibleResponse(responsible.getId(), responsible.getName(),
                responsible.getEmail(), responsible.getRole(), responsible.getDepartment(),
                responsible.getCreatedAt(), responsible.getUpdatedAt());
    }
}
