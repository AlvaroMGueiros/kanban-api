package br.com.alvar.kanban.domain.model;

import br.com.alvar.kanban.domain.utils.DomainValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "departments")
public class Department extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    protected Department() {
    }

    public Department(String name) {
        rename(name);
    }

    public void rename(String name) {
        this.name = DomainValidation.requireText(name, "Nome da secretaria", 120);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
}
