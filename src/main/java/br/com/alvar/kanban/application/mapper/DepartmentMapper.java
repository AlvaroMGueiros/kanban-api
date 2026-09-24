package br.com.alvar.kanban.application.mapper;

import br.com.alvar.kanban.application.dto.DepartmentResponse;
import br.com.alvar.kanban.domain.model.Department;

public final class DepartmentMapper {
    private DepartmentMapper() {
    }

    public static DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(department.getId(), department.getName(),
                department.getCreatedAt(), department.getUpdatedAt());
    }
}
