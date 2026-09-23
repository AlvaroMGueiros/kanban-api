import type { Project } from '../types/project';

interface ConfirmDeleteModalProps {
  project: Project;
  deleting: boolean;
  onCancel: () => void;
  onConfirm: () => void;
}

export function ConfirmDeleteModal({ project, deleting, onCancel, onConfirm }: ConfirmDeleteModalProps) {
  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={deleting ? undefined : onCancel}>
      <section
        className="confirm-modal"
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="confirm-delete-title"
        aria-describedby="confirm-delete-description"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <div className="confirm-modal__icon" aria-hidden="true">!</div>
        <div className="confirm-modal__content">
          <span className="eyebrow">CONFIRMAR EXCLUSÃO</span>
          <h2 id="confirm-delete-title">Excluir projeto?</h2>
          <p id="confirm-delete-description">
            O projeto <strong>“{project.name}”</strong> será removido permanentemente. Esta ação não pode ser desfeita.
          </p>
        </div>
        <footer className="confirm-modal__actions">
          <button type="button" className="secondary-button" onClick={onCancel} disabled={deleting}>
            Cancelar
          </button>
          <button type="button" className="danger-button" onClick={onConfirm} disabled={deleting} autoFocus>
            {deleting ? 'Excluindo…' : 'Excluir projeto'}
          </button>
        </footer>
      </section>
    </div>
  );
}
