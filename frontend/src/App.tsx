import { useState, type CSSProperties } from 'react';
import { Alert } from './components/Alert';
import { CatalogPage } from './components/CatalogPage';
import { ConfirmDeleteModal } from './components/ConfirmDeleteModal';
import { KanbanColumn } from './components/KanbanColumn';
import { ProjectFormModal } from './components/ProjectFormModal';
import { useKanbanBoard } from './hooks/useKanbanBoard';
import { useProjectManagement } from './hooks/useProjectManagement';
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
  const [currentView, setCurrentView] = useState<'kanban' | 'responsibles' | 'departments'>('kanban');
  const {
    columns,
    loading,
    movingProjectId,
    errorMessage,
    successMessage,
    dismissError,
    dismissSuccess,
    showError,
    showSuccess,
    loadProjects,
    moveProject,
  } = useKanbanBoard();
  const [draggedProject, setDraggedProject] = useState<Project | null>(null);
  const projectManagement = useProjectManagement({
    refreshProjects: loadProjects,
    showError,
    showSuccess,
  });

  return (
    <div className="app-shell" style={colorVariables}>
      <aside className="sidebar">
        <div className="sidebar__brand"><div><span>Gestão de projetos</span></div></div>
        <nav className="sidebar__nav" aria-label="Navegação principal">
          <button type="button" className={currentView === 'kanban' ? 'active' : ''} onClick={() => setCurrentView('kanban')}>Quadro Kanban</button>
          <button type="button" className={currentView === 'responsibles' ? 'active' : ''} onClick={() => setCurrentView('responsibles')}>Responsáveis</button>
          <button type="button" className={currentView === 'departments' ? 'active' : ''} onClick={() => setCurrentView('departments')}>Secretarias</button>
        </nav>
      </aside>
      <main>
        {currentView === 'kanban' ? (
          <>
        <header className="page-header">
          <div>
            <span className="eyebrow">VISÃO OPERACIONAL</span>
            <h1>Quadro de projetos</h1>
            <p>Acompanhe prazos e mova os projetos conforme as regras do fluxo.</p>
          </div>
          <div className="header-actions">
            <button type="button" className="secondary-button" onClick={() => void loadProjects()} disabled={loading}>
              {loading ? 'Atualizando…' : 'Atualizar quadro'}
            </button>
            <button type="button" className="primary-button" onClick={() => void projectManagement.openProjectForm(null)}>
              + Novo projeto
            </button>
          </div>
        </header>

        {errorMessage && <Alert message={errorMessage} tone="danger" onClose={dismissError} />}
        {successMessage && <Alert message={successMessage} tone="success" onClose={dismissSuccess} />}

        {loading ? (
          <div className="loading-state" role="status">Carregando projetos…</div>
        ) : (
          <div className={`kanban-board${draggedProject ? ' kanban-board--dragging' : ''}`}>
            {projectStatuses.map((status) => (
              <KanbanColumn
                key={status}
                status={status}
                projects={columns[status]}
                movingProjectId={movingProjectId}
                draggedProject={draggedProject}
                onDragStart={setDraggedProject}
                onDragEnd={() => setDraggedProject(null)}
                onDropProject={(targetStatus) => {
                  if (draggedProject) {
                    void moveProject(draggedProject, targetStatus);
                    setDraggedProject(null);
                  }
                }}
                onMove={(project, targetStatus) => void moveProject(project, targetStatus)}
                onEdit={(project) => void projectManagement.openProjectForm(project)}
                onDelete={projectManagement.requestProjectDeletion}
              />
            ))}
          </div>
        )}
          </>
        ) : (
          <CatalogPage mode={currentView} />
        )}
      </main>
      {projectManagement.formOpen && (
        <ProjectFormModal
          project={projectManagement.editingProject}
          responsibles={projectManagement.responsibles}
          saving={projectManagement.saving}
          errorMessage={projectManagement.formErrorMessage}
          onClose={projectManagement.closeProjectForm}
          onSubmit={(request) => void projectManagement.saveProject(request)}
        />
      )}
      {projectManagement.projectToDelete && (
        <ConfirmDeleteModal
          subjectName={projectManagement.projectToDelete.name}
          subjectLabel="projeto"
          deleting={projectManagement.deleting}
          onCancel={projectManagement.cancelProjectDeletion}
          onConfirm={() => void projectManagement.confirmProjectDeletion()}
        />
      )}
    </div>
  );
}
