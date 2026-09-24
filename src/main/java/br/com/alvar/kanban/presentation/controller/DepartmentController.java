package br.com.alvar.kanban.presentation.controller;

import java.net.URI;
import java.util.List;
import java.util.Set;

import br.com.alvar.kanban.application.dto.DepartmentRequest;
import br.com.alvar.kanban.application.dto.DepartmentResponse;
import br.com.alvar.kanban.application.dto.PageResponse;
import br.com.alvar.kanban.application.service.DepartmentService;
import br.com.alvar.kanban.presentation.utils.PageRequests;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/departments")
@Tag(name = "Secretarias", description = "Cadastro das secretarias associadas aos responsáveis")
public class DepartmentController {
    private static final Set<String> sortFields = Set.of("id", "name", "createdAt", "updatedAt");
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    @Operation(parameters = @Parameter(name = "sort",
            description = "Ordenação no formato campo,direção; pode ser repetido",
            example = "name,asc"))
    public PageResponse<DepartmentResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @Parameter(hidden = true) @RequestParam MultiValueMap<String, String> parameters) {
        return departmentService.list(PageRequests.create(page, size,
                parameters.getOrDefault("sort", List.of("name,asc")), sortFields));
    }

    @PostMapping
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse department = departmentService.create(request);
        return ResponseEntity.created(URI.create("/api/departments/" + department.id())).body(department);
    }

    @PutMapping("/{id}")
    public DepartmentResponse update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        return departmentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
