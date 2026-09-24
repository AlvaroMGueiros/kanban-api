package br.com.alvar.kanban.application.service;

import br.com.alvar.kanban.application.dto.PageResponse;
import br.com.alvar.kanban.application.dto.ResponsibleRequest;
import br.com.alvar.kanban.application.dto.ResponsibleResponse;
import br.com.alvar.kanban.application.mapper.ResponsibleMapper;
import br.com.alvar.kanban.domain.exception.ConflictException;
import br.com.alvar.kanban.domain.exception.ResourceNotFoundException;
import br.com.alvar.kanban.domain.model.Responsible;
import br.com.alvar.kanban.infrastructure.repository.DepartmentRepository;
import br.com.alvar.kanban.infrastructure.repository.ResponsibleRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ResponsibleService {
    private final ResponsibleRepository responsibleRepository;
    private final DepartmentRepository departmentRepository;

    public ResponsibleService(ResponsibleRepository responsibleRepository, DepartmentRepository departmentRepository) {
        this.responsibleRepository = responsibleRepository;
        this.departmentRepository = departmentRepository;
    }

    public PageResponse<ResponsibleResponse> list(Pageable pageable) {
        return PageResponse.from(responsibleRepository.findAll(pageable).map(ResponsibleMapper::toResponse));
    }

    public ResponsibleResponse find(Long id) {
        return ResponsibleMapper.toResponse(findResponsible(id));
    }

    @Transactional
    public ResponsibleResponse create(ResponsibleRequest request) {
        Responsible responsible = new Responsible(request.name(), request.email(), request.role(),
                findDepartmentName(request.department()));
        if (responsibleRepository.existsByEmail(responsible.getEmail())) {
            throw new ConflictException("Já existe um responsável com este e-mail.");
        }
        return ResponsibleMapper.toResponse(responsibleRepository.saveAndFlush(responsible));
    }

    @Transactional
    public ResponsibleResponse update(Long id, ResponsibleRequest request) {
        Responsible responsible = findResponsible(id);
        String normalizedEmail = Responsible.normalizeEmail(request.email());
        if (responsibleRepository.existsByEmailAndIdNot(normalizedEmail, id)) {
            throw new ConflictException("Já existe outro responsável com este e-mail.");
        }
        responsible.updateDetails(request.name(), normalizedEmail, request.role(), findDepartmentName(request.department()));
        responsibleRepository.flush();
        return ResponsibleMapper.toResponse(responsible);
    }

    @Transactional
    public void delete(Long id) {
        responsibleRepository.delete(findResponsible(id));
        responsibleRepository.flush();
    }

    private Responsible findResponsible(Long id) {
        return responsibleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Responsável", id));
    }

    private String findDepartmentName(String name) {
        return departmentRepository.findByNameIgnoreCase(name.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Secretaria", name))
                .getName();
    }
}
