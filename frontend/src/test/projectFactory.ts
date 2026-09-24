import type { Project, Responsible } from '../types/project';

export const responsible: Responsible = {
  id: 7,
  name: 'Ana Demo',
  email: 'ana@example.com',
  role: 'Gerente',
  department: 'Planejamento',
};

export const project: Project = {
  id: 2,
  name: 'Projeto em andamento',
  status: 'EM_ANDAMENTO',
  responsibles: [responsible],
  plannedStartDate: '2026-09-20',
  plannedEndDate: '2026-10-20',
  actualStartDate: '2026-09-23',
  actualEndDate: null,
  delayDays: 0,
  remainingTimePercentage: 90,
};
