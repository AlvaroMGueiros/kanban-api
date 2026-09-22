package br.com.alvar.kanban.domain.model;

import java.util.Locale;

import br.com.alvar.kanban.domain.utils.DomainValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "responsibles")
public class Responsible extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(nullable = false, length = 120)
    private String role;

    @Column(nullable = false, length = 120)
    private String department;

    protected Responsible() {
    }

    public Responsible(String name, String email, String role, String department) {
        updateDetails(name, email, role, department);
    }

    public void updateDetails(String name, String email, String role, String department) {
        this.name = DomainValidation.requireText(name, "Nome", 120);
        this.email = normalizeEmail(email);
        this.role = DomainValidation.requireText(role, "Cargo", 120);
        this.department = DomainValidation.requireText(department, "Secretaria", 120);
    }

    public static String normalizeEmail(String email) {
        return DomainValidation.requireText(email, "E-mail", 254).toLowerCase(Locale.ROOT);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getDepartment() { return department; }
}
