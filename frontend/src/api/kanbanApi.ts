import type {
  ApiError,
  PageResponse,
  Project,
  ProjectRequest,
  ProjectStatus,
  Responsible,
} from '../types/project';

const pageSize = 100;

export async function readResponse<T>(response: Response): Promise<T> {
  const body = (await response.json()) as T | ApiError;
  if (!response.ok) {
    const apiError = body as ApiError;
    throw new Error(apiError.message ?? `A API retornou o status ${response.status}.`);
  }
  return body as T;
}

async function getProjectPage(status: ProjectStatus, page: number): Promise<PageResponse<Project>> {
  const parameters = new URLSearchParams({
    status,
    page: String(page),
    size: String(pageSize),
    sort: 'id,asc',
  });
  const response = await fetch(`/api/kanban/projects?${parameters.toString()}`);
  return readResponse<PageResponse<Project>>(response);
}

export async function listProjectsByStatus(status: ProjectStatus): Promise<Project[]> {
  const firstPage = await getProjectPage(status, 0);
  if (firstPage.totalPages <= 1) {
    return firstPage.content;
  }

  const remainingRequests = Array.from(
    { length: firstPage.totalPages - 1 },
    (_, index) => getProjectPage(status, index + 1),
  );
  const remainingPages = await Promise.all(remainingRequests);
  return [firstPage, ...remainingPages].flatMap((page) => page.content);
}

export async function transitionProject(projectId: number, targetStatus: ProjectStatus): Promise<Project> {
  const response = await fetch(`/api/kanban/projects/${projectId}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ targetStatus }),
  });
  return readResponse<Project>(response);
}

async function getResponsiblePage(page: number): Promise<PageResponse<Responsible>> {
  const parameters = new URLSearchParams({
    page: String(page),
    size: String(pageSize),
    sort: 'name,asc',
  });
  const response = await fetch(`/api/responsibles?${parameters.toString()}`);
  return readResponse<PageResponse<Responsible>>(response);
}

export async function listResponsibles(): Promise<Responsible[]> {
  const firstPage = await getResponsiblePage(0);
  if (firstPage.totalPages <= 1) {
    return firstPage.content;
  }

  const remainingRequests = Array.from(
    { length: firstPage.totalPages - 1 },
    (_, index) => getResponsiblePage(index + 1),
  );
  const remainingPages = await Promise.all(remainingRequests);
  return [firstPage, ...remainingPages].flatMap((page) => page.content);
}

export async function createProject(request: ProjectRequest): Promise<Project> {
  const response = await fetch('/api/projects', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  });
  return readResponse<Project>(response);
}

export async function updateProject(projectId: number, request: ProjectRequest): Promise<Project> {
  const response = await fetch(`/api/projects/${projectId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  });
  return readResponse<Project>(response);
}

export async function deleteProject(projectId: number): Promise<void> {
  const response = await fetch(`/api/projects/${projectId}`, { method: 'DELETE' });
  if (!response.ok) {
    await readResponse<never>(response);
  }
}
