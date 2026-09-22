package br.com.alvar.kanban.infrastructure.repository;

import br.com.alvar.kanban.domain.exception.ResourceNotFoundException;
import br.com.alvar.kanban.domain.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    default Project findRequired(Long id) {
        return findById(id).orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
    }
}
