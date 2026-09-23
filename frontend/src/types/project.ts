export const projectStatuses = ['A_INICIAR', 'EM_ANDAMENTO', 'ATRASADO', 'CONCLUIDO'] as const;

export type ProjectStatus = (typeof projectStatuses)[number];

export interface Responsible {
  id: number;
  name: string;
  email: string;
  role: string;
  department: string;
}

export interface Project {
  id: number;
  name: string;
  status: ProjectStatus;
  responsibles: Responsible[];
  plannedStartDate: string | null;
  plannedEndDate: string | null;
  actualStartDate: string | null;
  actualEndDate: string | null;
  delayDays: number;
  remainingTimePercentage: number;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ApiError {
  message?: string;
}
