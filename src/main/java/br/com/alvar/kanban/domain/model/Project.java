package br.com.alvar.kanban.domain.model;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import br.com.alvar.kanban.domain.exception.BusinessRuleException;
import br.com.alvar.kanban.domain.utils.DomainValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "projects")
public class Project extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Embedded
    private ProjectSchedule schedule;

    @ManyToMany
    @JoinTable(name = "projectResponsibles", joinColumns = @JoinColumn(name = "projectId"),
            inverseJoinColumns = @JoinColumn(name = "responsibleId"))
    @OrderBy("id ASC")
    @BatchSize(size = 100)
    private Set<Responsible> responsibles = new LinkedHashSet<>();

    @Version
    private long version;

    protected Project() {
    }

    public Project(String name, ProjectSchedule schedule, Collection<Responsible> responsibles, LocalDate today) {
        updateDetails(name, schedule, responsibles, today);
    }

    public void updateDetails(String name, ProjectSchedule schedule, Collection<Responsible> responsibles, LocalDate today) {
        String validatedName = DomainValidation.requireText(name, "Nome do projeto", 200);
        if (responsibles == null || responsibles.isEmpty() || responsibles.size() > 100 || responsibles.stream().anyMatch(java.util.Objects::isNull)) {
            throw new BusinessRuleException("Projeto deve ter entre 1 e 100 responsáveis válidos.");
        }
        schedule.validateAt(today);
        this.name = validatedName;
        this.schedule = schedule;
        this.responsibles.clear();
        this.responsibles.addAll(responsibles);
    }

    public void changeSchedule(ProjectSchedule schedule, LocalDate today) {
        schedule.validateAt(today);
        this.schedule = schedule;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Set<Responsible> getResponsibles() { return Collections.unmodifiableSet(responsibles); }

    public ProjectSchedule getSchedule() {
        if (schedule == null) {
            return ProjectSchedule.empty();
        }
        return schedule;
    }
}
