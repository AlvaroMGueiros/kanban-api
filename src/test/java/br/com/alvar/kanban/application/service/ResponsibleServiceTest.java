package br.com.alvar.kanban.application.service;

import java.util.Optional;

import br.com.alvar.kanban.application.dto.ResponsibleRequest;
import br.com.alvar.kanban.domain.exception.ConflictException;
import br.com.alvar.kanban.domain.exception.ResourceNotFoundException;
import br.com.alvar.kanban.domain.model.Responsible;
import br.com.alvar.kanban.infrastructure.repository.ResponsibleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResponsibleServiceTest {
    @Mock
    private ResponsibleRepository responsibleRepository;

    @InjectMocks
    private ResponsibleService responsibleService;

    @Test
    void shouldNormalizeResponsibleBeforePersisting() {
        when(responsibleRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var response = responsibleService.create(new ResponsibleRequest(" Ana ", "ANA@example.com", " Analista ", " Obras "));
        assertThat(response.name()).isEqualTo("Ana");
        assertThat(response.email()).isEqualTo("ana@example.com");
        assertThat(response.department()).isEqualTo("Obras");
        verify(responsibleRepository).existsByEmail("ana@example.com");
    }

    @Test
    void shouldRejectDuplicateWithoutSaving() {
        when(responsibleRepository.existsByEmail("ana@example.com")).thenReturn(true);
        assertThatThrownBy(() -> responsibleService.create(
                new ResponsibleRequest("Ana", "ANA@example.com", "Analista", "Obras")))
                .isInstanceOf(ConflictException.class);
        verify(responsibleRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldRejectMissingResponsible() {
        when(responsibleRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> responsibleService.find(9L))
                .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("9");
    }

    @Test
    void shouldAllowKeepingOwnEmailDuringUpdate() {
        Responsible responsible = new Responsible("Ana", "ana@example.com", "Analista", "Obras");
        when(responsibleRepository.findById(1L)).thenReturn(Optional.of(responsible));
        responsibleService.update(1L, new ResponsibleRequest("Ana Silva", "ANA@example.com", "Analista", "Obras"));
        verify(responsibleRepository).existsByEmailAndIdNot("ana@example.com", 1L);
        assertThat(responsible.getName()).isEqualTo("Ana Silva");
    }
}
