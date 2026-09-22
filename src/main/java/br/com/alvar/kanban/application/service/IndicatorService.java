package br.com.alvar.kanban.application.service;

import java.time.Clock;
import java.time.LocalDate;

import br.com.alvar.kanban.application.dto.ProjectsByStatusResponse;
import br.com.alvar.kanban.domain.model.ProjectStatus;
import br.com.alvar.kanban.infrastructure.repository.ProjectRepository;
import br.com.alvar.kanban.infrastructure.repository.ProjectSpecifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IndicatorService {
    private final ProjectRepository projectRepository;
    private final Clock clock;

    public IndicatorService(ProjectRepository projectRepository, Clock clock) {
        this.projectRepository = projectRepository;
        this.clock = clock;
    }

    public ProjectsByStatusResponse countProjectsByStatus() {
        LocalDate today = LocalDate.now(clock);
        return new ProjectsByStatusResponse(
                count(ProjectStatus.A_INICIAR, today),
                count(ProjectStatus.EM_ANDAMENTO, today),
                count(ProjectStatus.ATRASADO, today),
                count(ProjectStatus.CONCLUIDO, today));
    }

    private long count(ProjectStatus status, LocalDate today) {
        return projectRepository.count(ProjectSpecifications.hasStatus(status, today));
    }
}
