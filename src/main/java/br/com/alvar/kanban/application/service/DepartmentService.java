package br.com.alvar.kanban.application.service;

import br.com.alvar.kanban.application.dto.DepartmentRequest;
import br.com.alvar.kanban.application.dto.DepartmentResponse;
import br.com.alvar.kanban.application.dto.PageResponse;
import br.com.alvar.kanban.application.mapper.DepartmentMapper;
import br.com.alvar.kanban.domain.exception.ConflictException;
import br.com.alvar.kanban.domain.exception.ResourceNotFoundException;
import br.com.alvar.kanban.domain.model.Department;
import br.com.alvar.kanban.infrastructure.repository.DepartmentRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public PageResponse<DepartmentResponse> list(Pageable pageable) {
        return PageResponse.from(departmentRepository.findAll(pageable).map(DepartmentMapper::toResponse));
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        Department department = new Department(request.name());
        ensureUniqueName(department.getName(), null);
        return DepartmentMapper.toResponse(departmentRepository.saveAndFlush(department));
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = findDepartment(id);
        ensureUniqueName(request.name(), id);
        department.rename(request.name());
        departmentRepository.flush();
        return DepartmentMapper.toResponse(department);
    }

    @Transactional
    public void delete(Long id) {
        departmentRepository.delete(findDepartment(id));
        departmentRepository.flush();
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Secretaria", id));
    }

    private void ensureUniqueName(String name, Long ignoredId) {
        boolean exists = ignoredId == null
                ? departmentRepository.existsByNameIgnoreCase(name.trim())
                : departmentRepository.existsByNameIgnoreCaseAndIdNot(name.trim(), ignoredId);
        if (exists) {
            throw new ConflictException("Já existe uma secretaria com este nome.");
        }
    }
}
