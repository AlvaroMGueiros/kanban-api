import type { Department, PageResponse, Responsible, ResponsibleRequest } from '../types/project';
import { readResponse } from './kanbanApi';

async function listAll<T>(endpoint: string): Promise<T[]> {
  const records: T[] = [];
  let currentPage = 0;
  let totalPages = 1;

  while (currentPage < totalPages) {
    const response = await fetch(`${endpoint}?page=${currentPage}&size=100&sort=name,asc`);
    const page = await readResponse<PageResponse<T>>(response);
    records.push(...page.content);
    totalPages = page.totalPages;
    currentPage += 1;
  }

  return records;
}

async function send<T>(endpoint: string, method: 'POST' | 'PUT', body: unknown): Promise<T> {
  const response = await fetch(endpoint, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  return readResponse<T>(response);
}

async function remove(endpoint: string): Promise<void> {
  const response = await fetch(endpoint, { method: 'DELETE' });
  if (!response.ok) {
    await readResponse<never>(response);
  }
}

export const catalogApi = {
  listDepartments: () => listAll<Department>('/api/departments'),
  createDepartment: (name: string) => send<Department>('/api/departments', 'POST', { name }),
  updateDepartment: (id: number, name: string) => send<Department>(`/api/departments/${id}`, 'PUT', { name }),
  deleteDepartment: (id: number) => remove(`/api/departments/${id}`),
  listResponsibles: () => listAll<Responsible>('/api/responsibles'),
  createResponsible: (request: ResponsibleRequest) => send<Responsible>('/api/responsibles', 'POST', request),
  updateResponsible: (id: number, request: ResponsibleRequest) =>
    send<Responsible>(`/api/responsibles/${id}`, 'PUT', request),
  deleteResponsible: (id: number) => remove(`/api/responsibles/${id}`),
};
