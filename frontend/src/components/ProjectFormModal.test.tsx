import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { responsible } from '../test/projectFactory';
import { ProjectFormModal } from './ProjectFormModal';

describe('ProjectFormModal', () => {
  it('submits a new project using the selected responsible and normalized optional dates', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();

    render(
      <ProjectFormModal
        project={null}
        responsibles={[responsible]}
        saving={false}
        errorMessage={null}
        onClose={vi.fn()}
        onSubmit={onSubmit}
      />,
    );

    await user.type(screen.getByLabelText('Nome do projeto'), 'Nova escola');
    await user.type(screen.getByRole('combobox', { name: 'Buscar responsáveis' }), 'Ana');
    await user.click(screen.getByRole('option', { name: /Ana Demo/ }));
    await user.type(screen.getByLabelText('Início previsto'), '2026-10-01');
    await user.type(screen.getByLabelText('Término previsto'), '2026-12-20');
    await user.click(screen.getByRole('button', { name: 'Salvar projeto' }));

    expect(onSubmit).toHaveBeenCalledWith({
      name: 'Nova escola',
      responsibleIds: [7],
      plannedStartDate: '2026-10-01',
      plannedEndDate: '2026-12-20',
      actualStartDate: null,
      actualEndDate: null,
    });
  });

  it('filters responsibles and keeps selected people in a compact chip', async () => {
    const user = userEvent.setup();
    render(
      <ProjectFormModal
        project={null}
        responsibles={[responsible, { ...responsible, id: 8, name: 'Bruno Demo', email: 'bruno@example.com' }]}
        saving={false}
        errorMessage={null}
        onClose={vi.fn()}
        onSubmit={vi.fn()}
      />,
    );

    await user.type(screen.getByRole('combobox', { name: 'Buscar responsáveis' }), 'Bruno');
    expect(screen.queryByRole('option', { name: /Ana Demo/ })).not.toBeInTheDocument();
    await user.click(screen.getByRole('option', { name: /Bruno Demo/ }));
    expect(screen.getByRole('button', { name: 'Remover Bruno Demo' })).toBeInTheDocument();
  });

  it('prevents submission until at least one responsible is selected', async () => {
    render(
      <ProjectFormModal
        project={null}
        responsibles={[responsible]}
        saving={false}
        errorMessage={null}
        onClose={vi.fn()}
        onSubmit={vi.fn()}
      />,
    );

    expect(screen.getByRole('button', { name: 'Salvar projeto' })).toBeDisabled();
  });
});
