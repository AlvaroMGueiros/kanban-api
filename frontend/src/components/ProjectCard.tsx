import { useEffect, useRef, useState, type DragEvent } from 'react';
import { projectStatuses, type Project, type ProjectStatus } from '../types/project';
import { statusConfig } from '../config/status';

interface ProjectCardProps {
  project: Project;
  moving: boolean;
  onDragStart: (project: Project) => void;
  onDragEnd: () => void;
  onMove: (project: Project, targetStatus: ProjectStatus) => void;
  onEdit: (project: Project) => void;
  onDelete: (project: Project) => void;
}

function formatDate(value: string | null): string {
  if (!value) {
    return 'Não definida';
  }
  return new Intl.DateTimeFormat('pt-BR', { timeZone: 'UTC' }).format(new Date(`${value}T00:00:00Z`));
}

export function ProjectCard({ project, moving, onDragStart, onDragEnd, onMove, onEdit, onDelete }: ProjectCardProps) {
  const menuRef = useRef<HTMLDetailsElement>(null);
  const [dragging, setDragging] = useState(false);
  const department = project.responsibles[0]?.department ?? 'Sem secretaria';
  const responsibleNames = project.responsibles.map((responsible) => responsible.name).join(', ');

  useEffect(() => {
    function closeMenu(event: MouseEvent) {
      if (!menuRef.current?.contains(event.target as Node)) {
        menuRef.current?.removeAttribute('open');
      }
    }
    function closeMenuByKeyboard(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        menuRef.current?.removeAttribute('open');
      }
    }
    document.addEventListener('mousedown', closeMenu);
    document.addEventListener('keydown', closeMenuByKeyboard);
    return () => {
      document.removeEventListener('mousedown', closeMenu);
      document.removeEventListener('keydown', closeMenuByKeyboard);
    };
  }, []);

  function startDragging(event: DragEvent<HTMLElement>) {
    event.dataTransfer.effectAllowed = 'move';
    event.dataTransfer.setData('text/plain', String(project.id));
    setDragging(true);
    onDragStart(project);
  }

  function finishDragging() {
    setDragging(false);
    onDragEnd();
  }

  return (
    <article
      className={`project-card${moving ? ' project-card--moving' : ''}${dragging ? ' project-card--dragging' : ''}`}
      draggable={!moving}
      onDragStart={startDragging}
      onDragEnd={finishDragging}
    >
      <div className="project-card__header">
        <div className="project-card__identity">
          <span className="drag-handle" aria-hidden="true">⠿</span>
          <span className="project-card__id">#{project.id}</span>
        </div>
        <div className="project-card__header-actions">
          {project.delayDays > 0 && <span className="delay-badge">{project.delayDays}d em atraso</span>}
          <details ref={menuRef} className="project-menu">
            <summary aria-label={`Abrir opções do projeto ${project.name}`} title="Opções">•••</summary>
            <div className="project-menu__options">
              <button
                type="button"
                onClick={(event) => {
                  event.currentTarget.closest('details')?.removeAttribute('open');
                  onEdit(project);
                }}
                disabled={moving}
              >
                Editar
              </button>
              <button
                type="button"
                className="delete-button"
                onClick={(event) => {
                  event.currentTarget.closest('details')?.removeAttribute('open');
                  onDelete(project);
                }}
                disabled={moving}
              >
                Excluir
              </button>
            </div>
          </details>
        </div>
      </div>
      <h3>{project.name}</h3>
      <p className="project-card__department">{department}</p>
      <dl className="project-card__details">
        <div>
          <dt>Responsável</dt>
          <dd>{responsibleNames || 'Não informado'}</dd>
        </div>
        <div>
          <dt>Prazo previsto</dt>
          <dd>{formatDate(project.plannedEndDate)}</dd>
        </div>
      </dl>
      <div className="progress-row">
        <span>Tempo restante</span>
        <strong>{project.remainingTimePercentage}%</strong>
      </div>
      <div className="progress-track" aria-hidden="true">
        <span style={{ width: `${Math.min(100, Math.max(0, project.remainingTimePercentage))}%` }} />
      </div>
      <label className="move-control">
        <span>Mover para</span>
        <select
          value={project.status}
          disabled={moving}
          onChange={(event) => onMove(project, event.target.value as ProjectStatus)}
        >
          {projectStatuses.map((status) => (
            <option key={status} value={status}>{statusConfig[status].label}</option>
          ))}
        </select>
      </label>
    </article>
  );
}
