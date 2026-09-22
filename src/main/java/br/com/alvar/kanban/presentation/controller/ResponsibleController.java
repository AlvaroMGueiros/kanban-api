package br.com.alvar.kanban.presentation.controller;

import java.net.URI;
import java.util.List;
import java.util.Set;

import br.com.alvar.kanban.application.dto.PageResponse;
import br.com.alvar.kanban.application.dto.ResponsibleRequest;
import br.com.alvar.kanban.application.dto.ResponsibleResponse;
import br.com.alvar.kanban.application.service.ResponsibleService;
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
@RequestMapping("/api/responsibles")
@Tag(name = "Responsáveis", description = "Cadastro e consulta de responsáveis por projetos")
public class ResponsibleController {
    private static final Set<String> sortFields = Set.of("id", "name", "email", "role", "department", "createdAt", "updatedAt");
    private final ResponsibleService responsibleService;

    public ResponsibleController(ResponsibleService responsibleService) {
        this.responsibleService = responsibleService;
    }

    @PostMapping
    public ResponseEntity<ResponsibleResponse> create(@Valid @RequestBody ResponsibleRequest request) {
        ResponsibleResponse responsible = responsibleService.create(request);
        return ResponseEntity.created(URI.create("/api/responsibles/" + responsible.id())).body(responsible);
    }

    @GetMapping
    public PageResponse<ResponsibleResponse> list(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size,
                                                @RequestParam MultiValueMap<String, String> parameters) {
        return responsibleService.list(PageRequests.create(page, size,
                parameters.getOrDefault("sort", List.of("id,asc")), sortFields));
    }

    @GetMapping("/{id}")
    public ResponsibleResponse find(@PathVariable Long id) {
        return responsibleService.find(id);
    }

    @PutMapping("/{id}")
    public ResponsibleResponse update(@PathVariable Long id, @Valid @RequestBody ResponsibleRequest request) {
        return responsibleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        responsibleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
