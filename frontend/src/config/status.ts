import type { ProjectStatus } from '../types/project';

export interface StatusConfig {
  label: string;
  description: string;
  tone: 'neutral' | 'info' | 'danger' | 'success';
}

export const statusConfig: Record<ProjectStatus, StatusConfig> = {
  A_INICIAR: {
    label: 'A iniciar',
    description: 'Projetos aguardando início',
    tone: 'neutral',
  },
  EM_ANDAMENTO: {
    label: 'Em andamento',
    description: 'Projetos em execução',
    tone: 'info',
  },
  ATRASADO: {
    label: 'Atrasado',
    description: 'Projetos fora do prazo',
    tone: 'danger',
  },
  CONCLUIDO: {
    label: 'Concluído',
    description: 'Projetos finalizados',
    tone: 'success',
  },
};
