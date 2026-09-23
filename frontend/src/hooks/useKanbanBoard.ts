import { useCallback, useEffect, useState } from 'react';
import { listProjectsByStatus, transitionProject } from '../api/kanbanApi';
import { projectStatuses, type Project, type ProjectStatus } from '../types/project';

type ProjectColumns = Record<ProjectStatus, Project[]>;

const emptyColumns = (): ProjectColumns => ({
  A_INICIAR: [],
  EM_ANDAMENTO: [],
  ATRASADO: [],
  CONCLUIDO: [],
});

function getErrorMessage(error: unknown): string {
  return error instanceof Error ? error.message : 'Não foi possível concluir a operação.';
}

export function useKanbanBoard() {
  const [columns, setColumns] = useState<ProjectColumns>(emptyColumns);
  const [loading, setLoading] = useState(true);
  const [movingProjectId, setMovingProjectId] = useState<number | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const loadProjects = useCallback(async () => {
    setLoading(true);
    setErrorMessage(null);
    try {
      const results = await Promise.all(
        projectStatuses.map(async (status) => [status, await listProjectsByStatus(status)] as const),
      );
      setColumns(Object.fromEntries(results) as ProjectColumns);
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadProjects();
  }, [loadProjects]);

  const moveProject = useCallback(async (project: Project, targetStatus: ProjectStatus) => {
    if (project.status === targetStatus || movingProjectId !== null) {
      return;
    }

    setMovingProjectId(project.id);
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      await transitionProject(project.id, targetStatus);
      setSuccessMessage(`Projeto “${project.name}” movido com sucesso.`);
      await loadProjects();
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setMovingProjectId(null);
    }
  }, [loadProjects, movingProjectId]);

  return {
    columns,
    loading,
    movingProjectId,
    errorMessage,
    successMessage,
    dismissError: () => setErrorMessage(null),
    dismissSuccess: () => setSuccessMessage(null),
    showError: (message: string) => setErrorMessage(message),
    showSuccess: (message: string) => setSuccessMessage(message),
    loadProjects,
    moveProject,
  };
}
