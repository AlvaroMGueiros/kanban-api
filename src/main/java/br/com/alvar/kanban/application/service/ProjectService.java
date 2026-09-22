package br.com.alvar.kanban.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.alvar.kanban.application.dto.PageResponse;
import br.com.alvar.kanban.application.dto.ProjectRequest;
import br.com.alvar.kanban.application.dto.ProjectResponse;
import br.com.alvar.kanban.application.mapper.ProjectMapper;
import br.com.alvar.kanban.domain.exception.BusinessRuleException;
import br.com.alvar.kanban.domain.exception.ResourceNotFoundException;
import br.com.alvar.kanban.domain.model.Project;
import br.com.alvar.kanban.domain.model.ProjectSchedule;
import br.com.alvar.kanban.domain.model.Responsible;
import br.com.alvar.kanban.infrastructure.repository.ProjectRepository;
import br.com.alvar.kanban.infrastructure.repository.ResponsibleRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ResponsibleRepository responsibleRepository;
    private final Clock clock;

    public ProjectService(ProjectRepository projectRepository, ResponsibleRepository responsibleRepository, Clock clock) {
        this.projectRepository = projectRepository;
        this.responsibleRepository = responsibleRepository;
        this.clock = clock;
    }

    public PageResponse<ProjectResponse> list(Pageable pageable) {
        LocalDate today = LocalDate.now(clock);
        return PageResponse.from(projectRepository.findAll(pageable).map(project -> ProjectMapper.toResponse(project, today)));
    }

    public ProjectResponse find(Long id) {
        LocalDate today = LocalDate.now(clock);
        return ProjectMapper.toResponse(projectRepository.findRequired(id), today);
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        LocalDate today = LocalDate.now(clock);
        Project project = new Project(request.name(), scheduleFrom(request), findResponsibles(request.responsibleIds()), today);
        projectRepository.saveAndFlush(project);
        return ProjectMapper.toResponse(project, today);
    }

    @Transactional
    public ProjectResponse update(Long id, ProjectRequest request) {
        LocalDate today = LocalDate.now(clock);
        Project project = projectRepository.findRequired(id);
        project.updateDetails(request.name(), scheduleFrom(request), findResponsibles(request.responsibleIds()), today);
        projectRepository.flush();
        return ProjectMapper.toResponse(project, today);
    }

    @Transactional
    public void delete(Long id) {
        projectRepository.delete(projectRepository.findRequired(id));
        projectRepository.flush();
    }

    private ProjectSchedule scheduleFrom(ProjectRequest request) {
        return new ProjectSchedule(request.plannedStartDate(), request.plannedEndDate(),
                request.actualStartDate(), request.actualEndDate());
    }

    private List<Responsible> findResponsibles(List<Long> responsibleIds) {
        Set<Long> missingIds = new HashSet<>();
        for (Long responsibleId : responsibleIds) {
            if (!missingIds.add(responsibleId)) {
                throw new BusinessRuleException("Responsáveis não podem se repetir no projeto.");
            }
        }
        List<Responsible> responsibles = responsibleRepository.findAllById(missingIds);
        for (Responsible responsible : responsibles) {
            missingIds.remove(responsible.getId());
        }
        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException("Responsáveis", missingIds);
        }
        return responsibles;
    }
}
