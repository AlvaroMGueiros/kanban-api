package br.com.alvar.kanban.presentation.controller;

import java.util.List;
import java.util.Set;

import br.com.alvar.kanban.application.dto.PageResponse;
import br.com.alvar.kanban.application.dto.ProjectResponse;
import br.com.alvar.kanban.application.dto.TransitionRequest;
import br.com.alvar.kanban.application.service.KanbanService;
import br.com.alvar.kanban.application.service.ProjectService;
import br.com.alvar.kanban.domain.model.ProjectStatus;
import br.com.alvar.kanban.presentation.utils.PageRequests;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kanban")
@Tag(name = "Kanban", description = "Colunas e transições validadas do quadro Kanban")
public class KanbanController {
    private static final Set<String> SORT_FIELDS = Set.of("id", "name", "createdAt", "updatedAt");

    private final ProjectService projectService;
    private final KanbanService kanbanService;

    public KanbanController(ProjectService projectService, KanbanService kanbanService) {
        this.projectService = projectService;
        this.kanbanService = kanbanService;
    }

    @GetMapping("/projects")
    public PageResponse<ProjectResponse> listByStatus(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam MultiValueMap<String, String> parameters) {
        return projectService.listByStatus(
                status,
                PageRequests.create(page, size,
                        parameters.getOrDefault("sort", List.of("id,asc")), SORT_FIELDS));
    }

    @PatchMapping("/projects/{id}/status")
    public ProjectResponse transition(
            @PathVariable Long id,
            @Valid @RequestBody TransitionRequest request) {
        return kanbanService.transition(id, request);
    }
}
