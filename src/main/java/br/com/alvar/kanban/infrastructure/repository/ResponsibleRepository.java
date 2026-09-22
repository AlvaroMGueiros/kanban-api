package br.com.alvar.kanban.infrastructure.repository;

import br.com.alvar.kanban.domain.model.Responsible;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResponsibleRepository extends JpaRepository<Responsible, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
}
