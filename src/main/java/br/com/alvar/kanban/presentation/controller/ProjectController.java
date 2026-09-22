package br.com.alvar.kanban.presentation.controller;

import java.net.URI;
import java.util.List;
import java.util.Set;

import br.com.alvar.kanban.application.dto.PageResponse;
import br.com.alvar.kanban.application.dto.ProjectFilters;
import br.com.alvar.kanban.application.dto.ProjectRequest;
import br.com.alvar.kanban.application.dto.ProjectResponse;
import br.com.alvar.kanban.application.service.ProjectService;
import br.com.alvar.kanban.domain.model.ProjectStatus;
import br.com.alvar.kanban.presentation.utils.PageRequests;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projetos", description = "Cadastro, consulta, filtros e métricas derivadas de projetos")
public class ProjectController {
    private static final Set<String> sortFields = Set.of("id", "name", "createdAt", "updatedAt");
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request) {
        ProjectResponse project = projectService.create(request);
        return ResponseEntity.created(URI.create("/api/projects/" + project.id())).body(project);
    }

    @GetMapping
    public PageResponse<ProjectResponse> list(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(required = false) ProjectStatus status,
                                            @RequestParam(required = false) Long responsibleId,
                                            @RequestParam(required = false) String department,
                                            @RequestParam(required = false) String text,
                                            @RequestParam MultiValueMap<String, String> parameters) {
        return projectService.list(new ProjectFilters(status, responsibleId, department, text),
                PageRequests.create(page, size,
                parameters.getOrDefault("sort", List.of("id,asc")), sortFields));
    }

    @GetMapping("/{id}")
    public ProjectResponse find(@PathVariable Long id) {
        return projectService.find(id);
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        return projectService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
