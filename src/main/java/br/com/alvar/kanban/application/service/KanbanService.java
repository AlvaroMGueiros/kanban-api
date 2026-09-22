package br.com.alvar.kanban.application.service;

import java.time.Clock;
import java.time.LocalDate;

import br.com.alvar.kanban.application.dto.ProjectResponse;
import br.com.alvar.kanban.application.dto.TransitionRequest;
import br.com.alvar.kanban.application.mapper.ProjectMapper;
import br.com.alvar.kanban.domain.model.Project;
import br.com.alvar.kanban.domain.model.ProjectSchedule;
import br.com.alvar.kanban.domain.service.KanbanTransitionService;
import br.com.alvar.kanban.infrastructure.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KanbanService {
    private final ProjectRepository projectRepository;
    private final Clock clock;

    public KanbanService(ProjectRepository projectRepository, Clock clock) {
        this.projectRepository = projectRepository;
        this.clock = clock;
    }

    @Transactional
    public ProjectResponse transition(Long projectId, TransitionRequest request) {
        LocalDate today = LocalDate.now(clock);
        Project project = projectRepository.findRequired(projectId);

        ProjectSchedule updatedSchedule = KanbanTransitionService.transition(
                project.getSchedule(), request.targetStatus(), today);

        project.changeSchedule(updatedSchedule, today);
        projectRepository.flush();

        return ProjectMapper.toResponse(project, today);
    }
}
