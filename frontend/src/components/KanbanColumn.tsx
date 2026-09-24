import { useState } from 'react';
import { statusConfig } from '../config/status';
import type { Project, ProjectStatus } from '../types/project';
import { ProjectCard } from './ProjectCard';

interface KanbanColumnProps {
  status: ProjectStatus;
  projects: Project[];
  movingProjectId: number | null;
  draggedProject: Project | null;
  onDragStart: (project: Project) => void;
  onDragEnd: () => void;
  onDropProject: (status: ProjectStatus) => void;
  onMove: (project: Project, targetStatus: ProjectStatus) => void;
  onEdit: (project: Project) => void;
  onDelete: (project: Project) => void;
}

export function KanbanColumn({
  status,
  projects,
  movingProjectId,
  draggedProject,
  onDragStart,
  onDragEnd,
  onDropProject,
  onMove,
  onEdit,
  onDelete,
}: KanbanColumnProps) {
  const [dragOver, setDragOver] = useState(false);
  const config = statusConfig[status];
  const canReceiveProject = draggedProject !== null && draggedProject.status !== status;

  return (
    <section
      className={`kanban-column kanban-column--${config.tone}${dragOver && canReceiveProject ? ' kanban-column--drag-over' : ''}${canReceiveProject ? ' kanban-column--available' : ''}`}
      onDragOver={(event) => {
        event.preventDefault();
        event.dataTransfer.dropEffect = canReceiveProject ? 'move' : 'none';
        setDragOver(canReceiveProject);
      }}
      onDragLeave={(event) => {
        if (!event.currentTarget.contains(event.relatedTarget as Node | null)) {
          setDragOver(false);
        }
      }}
      onDrop={(event) => {
        event.preventDefault();
        setDragOver(false);
        if (canReceiveProject) {
          onDropProject(status);
        }
      }}
    >
      <header className="kanban-column__header">
        <div>
          <div className="kanban-column__title-row">
            <span className="status-dot" />
            <h2>{config.label}</h2>
            <span className="count-badge">{projects.length}</span>
          </div>
          <p>{config.description}</p>
        </div>
      </header>
      <div className="kanban-column__content">
        {dragOver && canReceiveProject && (
          <div className="kanban-drop-indicator" role="status">
            <span aria-hidden="true">↓</span>
            Soltar em {config.label}
          </div>
        )}
        {projects.length === 0 ? (
          <div className={`empty-column${canReceiveProject ? ' empty-column--available' : ''}`}>
            {canReceiveProject ? `Solte para mover para ${config.label}` : 'Arraste um projeto para esta coluna'}
          </div>
        ) : (
          projects.map((project) => (
            <ProjectCard
              key={project.id}
              project={project}
              moving={movingProjectId === project.id}
              onDragStart={onDragStart}
              onDragEnd={onDragEnd}
              onMove={onMove}
              onEdit={onEdit}
              onDelete={onDelete}
            />
          ))
        )}
      </div>
    </section>
  );
}
