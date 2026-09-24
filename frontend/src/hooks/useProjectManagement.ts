import { useCallback, useState } from 'react';
import { createProject, deleteProject, listResponsibles, updateProject } from '../api/kanbanApi';
import type { Project, ProjectRequest, Responsible } from '../types/project';

interface UseProjectManagementOptions {
  refreshProjects: () => Promise<void>;
  showError: (message: string) => void;
  showSuccess: (message: string) => void;
}

function getErrorMessage(error: unknown, fallback: string): string {
  return error instanceof Error ? error.message : fallback;
}

export function useProjectManagement({
  refreshProjects,
  showError,
  showSuccess,
}: UseProjectManagementOptions) {
  const [editingProject, setEditingProject] = useState<Project | null>(null);
  const [responsibles, setResponsibles] = useState<Responsible[]>([]);
  const [formOpen, setFormOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [formErrorMessage, setFormErrorMessage] = useState<string | null>(null);
  const [projectToDelete, setProjectToDelete] = useState<Project | null>(null);
  const [deleting, setDeleting] = useState(false);

  const openProjectForm = useCallback(async (project: Project | null) => {
    setEditingProject(project);
    setFormErrorMessage(null);
    setFormOpen(true);
    try {
      setResponsibles(await listResponsibles());
    } catch (error) {
      setFormErrorMessage(getErrorMessage(error, 'Não foi possível carregar os responsáveis.'));
    }
  }, []);

  const closeProjectForm = useCallback(() => {
    if (!saving) {
      setFormOpen(false);
    }
  }, [saving]);

  const saveProject = useCallback(async (request: ProjectRequest) => {
    setSaving(true);
    setFormErrorMessage(null);
    try {
      if (editingProject) {
        await updateProject(editingProject.id, request);
      } else {
        await createProject(request);
      }
      setFormOpen(false);
      await refreshProjects();
      showSuccess(editingProject ? 'Projeto atualizado com sucesso.' : 'Projeto criado com sucesso.');
    } catch (error) {
      setFormErrorMessage(getErrorMessage(error, 'Não foi possível salvar o projeto.'));
    } finally {
      setSaving(false);
    }
  }, [editingProject, refreshProjects, showSuccess]);

  const requestProjectDeletion = useCallback((project: Project) => {
    setProjectToDelete(project);
  }, []);

  const cancelProjectDeletion = useCallback(() => {
    if (!deleting) {
      setProjectToDelete(null);
    }
  }, [deleting]);

  const confirmProjectDeletion = useCallback(async () => {
    if (!projectToDelete) {
      return;
    }
    setDeleting(true);
    try {
      await deleteProject(projectToDelete.id);
      await refreshProjects();
      showSuccess(`Projeto “${projectToDelete.name}” excluído com sucesso.`);
      setProjectToDelete(null);
    } catch (error) {
      showError(getErrorMessage(error, 'Não foi possível excluir o projeto.'));
    } finally {
      setDeleting(false);
    }
  }, [projectToDelete, refreshProjects, showError, showSuccess]);

  return {
    editingProject,
    responsibles,
    formOpen,
    saving,
    formErrorMessage,
    projectToDelete,
    deleting,
    openProjectForm,
    closeProjectForm,
    saveProject,
    requestProjectDeletion,
    cancelProjectDeletion,
    confirmProjectDeletion,
  };
}
