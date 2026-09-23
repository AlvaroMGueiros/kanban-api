import { useState, type CSSProperties } from 'react';
import { Alert } from './components/Alert';
import { KanbanColumn } from './components/KanbanColumn';
import { useKanbanBoard } from './hooks/useKanbanBoard';
import { colors } from './styles/colors';
import { projectStatuses, type Project } from './types/project';

type ColorVariables = CSSProperties & Record<`--color-${string}`, string>;

const colorVariables: ColorVariables = {
  '--color-primary': colors.primary,
  '--color-primary-strong': colors.primaryStrong,
  '--color-primary-soft': colors.primarySoft,
  '--color-secondary': colors.secondary,
  '--color-surface': colors.surface,
  '--color-background': colors.background,
  '--color-border': colors.border,
  '--color-text': colors.text,
  '--color-text-muted': colors.textMuted,
  '--color-danger': colors.danger,
  '--color-danger-soft': colors.dangerSoft,
  '--color-warning': colors.warning,
  '--color-warning-soft': colors.warningSoft,
  '--color-success': colors.success,
  '--color-success-soft': colors.successSoft,
  '--color-shadow': colors.shadow,
};

export default function App() {
  const {
    columns,
    loading,
    movingProjectId,
    errorMessage,
    successMessage,
    dismissError,
    dismissSuccess,
    loadProjects,
    moveProject,
  } = useKanbanBoard();
  const [draggedProject, setDraggedProject] = useState<Project | null>(null);

  return (
    <div className="app-shell" style={colorVariables}>
      <aside className="sidebar">
        <div className="brand-mark">K</div>
        <div>
          <strong>Kanban</strong>
          <span>Gestão de projetos</span>
        </div>
      </aside>
      <main>
        <header className="page-header">
          <div>
            <span className="eyebrow">VISÃO OPERACIONAL</span>
            <h1>Quadro de projetos</h1>
            <p>Acompanhe prazos e mova os projetos conforme as regras do fluxo.</p>
          </div>
          <button type="button" className="refresh-button" onClick={() => void loadProjects()} disabled={loading}>
            {loading ? 'Atualizando…' : 'Atualizar quadro'}
          </button>
        </header>

        {errorMessage && <Alert message={errorMessage} tone="danger" onClose={dismissError} />}
        {successMessage && <Alert message={successMessage} tone="success" onClose={dismissSuccess} />}

        {loading ? (
          <div className="loading-state" role="status">Carregando projetos…</div>
        ) : (
          <div className="kanban-board">
            {projectStatuses.map((status) => (
              <KanbanColumn
                key={status}
                status={status}
                projects={columns[status]}
                movingProjectId={movingProjectId}
                onDragStart={setDraggedProject}
                onDropProject={(targetStatus) => {
                  if (draggedProject) {
                    void moveProject(draggedProject, targetStatus);
                    setDraggedProject(null);
                  }
                }}
                onMove={(project, targetStatus) => void moveProject(project, targetStatus)}
              />
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
