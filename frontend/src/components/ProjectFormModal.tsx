import { useEffect, useState, type FormEvent } from 'react';
import type { Project, ProjectRequest, Responsible } from '../types/project';

interface ProjectFormModalProps {
  project: Project | null;
  responsibles: Responsible[];
  saving: boolean;
  errorMessage: string | null;
  onClose: () => void;
  onSubmit: (request: ProjectRequest) => void;
}

interface ProjectFormState {
  name: string;
  responsibleIds: number[];
  plannedStartDate: string;
  plannedEndDate: string;
  actualStartDate: string;
  actualEndDate: string;
}

function createFormState(project: Project | null): ProjectFormState {
  return {
    name: project?.name ?? '',
    responsibleIds: project?.responsibles.map((responsible) => responsible.id) ?? [],
    plannedStartDate: project?.plannedStartDate ?? '',
    plannedEndDate: project?.plannedEndDate ?? '',
    actualStartDate: project?.actualStartDate ?? '',
    actualEndDate: project?.actualEndDate ?? '',
  };
}

function optionalDate(value: string): string | null {
  return value || null;
}

export function ProjectFormModal({
  project,
  responsibles,
  saving,
  errorMessage,
  onClose,
  onSubmit,
}: ProjectFormModalProps) {
  const [formState, setFormState] = useState(() => createFormState(project));

  useEffect(() => {
    setFormState(createFormState(project));
  }, [project]);

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    onSubmit({
      name: formState.name.trim(),
      responsibleIds: formState.responsibleIds,
      plannedStartDate: optionalDate(formState.plannedStartDate),
      plannedEndDate: optionalDate(formState.plannedEndDate),
      actualStartDate: optionalDate(formState.actualStartDate),
      actualEndDate: optionalDate(formState.actualEndDate),
    });
  }

  function toggleResponsible(responsibleId: number) {
    setFormState((current) => {
      const selected = current.responsibleIds.includes(responsibleId);
      const responsibleIds = selected
        ? current.responsibleIds.filter((id) => id !== responsibleId)
        : [...current.responsibleIds, responsibleId];
      return { ...current, responsibleIds };
    });
  }

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="project-form-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <header className="modal__header">
          <div>
            <span className="eyebrow">PROJETO</span>
            <h2 id="project-form-title">{project ? 'Editar projeto' : 'Novo projeto'}</h2>
          </div>
          <button type="button" className="icon-button" onClick={onClose} aria-label="Fechar formulário">×</button>
        </header>

        <form onSubmit={handleSubmit}>
          {errorMessage && <div className="form-error" role="alert">{errorMessage}</div>}

          <label className="form-field form-field--full">
            <span>Nome do projeto</span>
            <input
              required
              maxLength={200}
              value={formState.name}
              onChange={(event) => setFormState((current) => ({ ...current, name: event.target.value }))}
              placeholder="Ex.: Reforma da escola"
            />
          </label>

          <fieldset className="responsible-fieldset">
            <legend>Responsáveis</legend>
            {responsibles.length === 0 ? (
              <p>Nenhum responsável cadastrado. Cadastre um responsável pela API antes de criar o projeto.</p>
            ) : (
              <div className="responsible-options">
                {responsibles.map((responsible) => (
                  <label key={responsible.id} className="responsible-option">
                    <input
                      type="checkbox"
                      checked={formState.responsibleIds.includes(responsible.id)}
                      onChange={() => toggleResponsible(responsible.id)}
                    />
                    <span>
                      <strong>{responsible.name}</strong>
                      <small>{responsible.department} · {responsible.role}</small>
                    </span>
                  </label>
                ))}
              </div>
            )}
          </fieldset>

          <div className="form-grid">
            <label className="form-field">
              <span>Início previsto</span>
              <input
                type="date"
                value={formState.plannedStartDate}
                onChange={(event) => setFormState((current) => ({ ...current, plannedStartDate: event.target.value }))}
              />
            </label>
            <label className="form-field">
              <span>Término previsto</span>
              <input
                type="date"
                value={formState.plannedEndDate}
                onChange={(event) => setFormState((current) => ({ ...current, plannedEndDate: event.target.value }))}
              />
            </label>
            <label className="form-field">
              <span>Início realizado</span>
              <input
                type="date"
                value={formState.actualStartDate}
                onChange={(event) => setFormState((current) => ({ ...current, actualStartDate: event.target.value }))}
              />
            </label>
            <label className="form-field">
              <span>Término realizado</span>
              <input
                type="date"
                value={formState.actualEndDate}
                onChange={(event) => setFormState((current) => ({ ...current, actualEndDate: event.target.value }))}
              />
            </label>
          </div>

          <footer className="modal__footer">
            <button type="button" className="secondary-button" onClick={onClose} disabled={saving}>Cancelar</button>
            <button
              type="submit"
              className="primary-button"
              disabled={saving || responsibles.length === 0 || formState.responsibleIds.length === 0}
            >
              {saving ? 'Salvando…' : 'Salvar projeto'}
            </button>
          </footer>
        </form>
      </section>
    </div>
  );
}
