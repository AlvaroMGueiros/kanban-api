import { afterEach, describe, expect, it, vi } from 'vitest';
import { deleteProject, transitionProject } from './kanbanApi';

describe('kanbanApi', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('propagates the specific backend message when a transition is rejected', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify({
      message: 'Não é possível mover para Em andamento porque as datas ainda indicam atraso.',
    }), {
      status: 422,
      headers: { 'Content-Type': 'application/json' },
    })));

    await expect(transitionProject(2, 'EM_ANDAMENTO')).rejects.toThrow(
      'Não é possível mover para Em andamento porque as datas ainda indicam atraso.',
    );
  });

  it('accepts a successful deletion without trying to parse an empty body', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(null, { status: 204 })));

    await expect(deleteProject(2)).resolves.toBeUndefined();
  });
});
