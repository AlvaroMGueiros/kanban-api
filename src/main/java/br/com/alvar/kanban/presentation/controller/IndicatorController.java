package br.com.alvar.kanban.presentation.controller;

import br.com.alvar.kanban.application.dto.ProjectsByStatusResponse;
import br.com.alvar.kanban.application.service.IndicatorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/indicators")
@Tag(name = "Indicadores", description = "Indicadores consolidados dos projetos")
public class IndicatorController {
    private final IndicatorService indicatorService;

    public IndicatorController(IndicatorService indicatorService) {
        this.indicatorService = indicatorService;
    }

    @GetMapping("/projects-by-status")
    public ProjectsByStatusResponse countProjectsByStatus() {
        return indicatorService.countProjectsByStatus();
    }
}
