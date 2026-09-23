import { projectStatuses, type Project, type ProjectStatus } from '../types/project';
import { statusConfig } from '../config/status';

interface ProjectCardProps {
  project: Project;
  moving: boolean;
  onDragStart: (project: Project) => void;
  onMove: (project: Project, targetStatus: ProjectStatus) => void;
}

function formatDate(value: string | null): string {
  if (!value) {
    return 'Não definida';
  }
  return new Intl.DateTimeFormat('pt-BR', { timeZone: 'UTC' }).format(new Date(`${value}T00:00:00Z`));
}

export function ProjectCard({ project, moving, onDragStart, onMove }: ProjectCardProps) {
  const department = project.responsibles[0]?.department ?? 'Sem secretaria';
  const responsibleNames = project.responsibles.map((responsible) => responsible.name).join(', ');

  return (
    <article
      className={`project-card${moving ? ' project-card--moving' : ''}`}
      draggable={!moving}
      onDragStart={() => onDragStart(project)}
    >
      <div className="project-card__header">
        <span className="project-card__id">#{project.id}</span>
        {project.delayDays > 0 && <span className="delay-badge">{project.delayDays}d em atraso</span>}
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
