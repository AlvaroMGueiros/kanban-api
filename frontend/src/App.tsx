import { useState, type CSSProperties } from 'react';
import { Alert } from './components/Alert';
import { KanbanColumn } from './components/KanbanColumn';
import { ProjectFormModal } from './components/ProjectFormModal';
import { useKanbanBoard } from './hooks/useKanbanBoard';
import { createProject, deleteProject, listResponsibles, updateProject } from './api/kanbanApi';
import { colors } from './styles/colors';
import { projectStatuses, type Project, type ProjectRequest, type Responsible } from './types/project';

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
    showError,
    showSuccess,
    loadProjects,
    moveProject,
  } = useKanbanBoard();
  const [draggedProject, setDraggedProject] = useState<Project | null>(null);
  const [editingProject, setEditingProject] = useState<Project | null>(null);
  const [responsibles, setResponsibles] = useState<Responsible[]>([]);
  const [formOpen, setFormOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [formErrorMessage, setFormErrorMessage] = useState<string | null>(null);

  async function openProjectForm(project: Project | null) {
    setEditingProject(project);
    setFormErrorMessage(null);
    setFormOpen(true);
    try {
      setResponsibles(await listResponsibles());
    } catch (error) {
      setFormErrorMessage(error instanceof Error ? error.message : 'Não foi possível carregar os responsáveis.');
    }
  }

  async function saveProject(request: ProjectRequest) {
    setSaving(true);
    setFormErrorMessage(null);
    try {
      if (editingProject) {
        await updateProject(editingProject.id, request);
      } else {
        await createProject(request);
      }
      setFormOpen(false);
      await loadProjects();
      showSuccess(editingProject ? 'Projeto atualizado com sucesso.' : 'Projeto criado com sucesso.');
    } catch (error) {
      setFormErrorMessage(error instanceof Error ? error.message : 'Não foi possível salvar o projeto.');
    } finally {
      setSaving(false);
    }
  }

  async function removeProject(project: Project) {
    const confirmed = window.confirm(`Excluir o projeto “${project.name}”? Esta ação não pode ser desfeita.`);
    if (!confirmed) {
      return;
    }
    try {
      await deleteProject(project.id);
      await loadProjects();
      showSuccess(`Projeto “${project.name}” excluído com sucesso.`);
    } catch (error) {
      showError(error instanceof Error ? error.message : 'Não foi possível excluir o projeto.');
    }
  }

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
          <div className="header-actions">
            <button type="button" className="secondary-button" onClick={() => void loadProjects()} disabled={loading}>
              {loading ? 'Atualizando…' : 'Atualizar quadro'}
            </button>
            <button type="button" className="primary-button" onClick={() => void openProjectForm(null)}>
              + Novo projeto
            </button>
          </div>
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
                onEdit={(project) => void openProjectForm(project)}
                onDelete={(project) => void removeProject(project)}
              />
            ))}
          </div>
        )}
      </main>
      {formOpen && (
        <ProjectFormModal
          project={editingProject}
          responsibles={responsibles}
          saving={saving}
          errorMessage={formErrorMessage}
          onClose={() => setFormOpen(false)}
          onSubmit={(request) => void saveProject(request)}
        />
      )}
    </div>
  );
}
