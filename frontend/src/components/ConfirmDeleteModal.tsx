import { useEffect } from 'react';
interface ConfirmDeleteModalProps {
  subjectName: string;
  subjectLabel: string;
  deleting: boolean;
  onCancel: () => void;
  onConfirm: () => void;
}

export function ConfirmDeleteModal({ subjectName, subjectLabel, deleting, onCancel, onConfirm }: ConfirmDeleteModalProps) {
  useEffect(() => {
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape' && !deleting) {
        onCancel();
      }
    }
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [deleting, onCancel]);

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
          <h2 id="confirm-delete-title">Excluir {subjectLabel}?</h2>
          <p id="confirm-delete-description">
            <strong>“{subjectName}”</strong> será removido permanentemente. Esta ação não pode ser desfeita.
          </p>
        </div>
        <footer className="confirm-modal__actions">
          <button type="button" className="secondary-button" onClick={onCancel} disabled={deleting}>
            Cancelar
          </button>
          <button type="button" className="danger-button" onClick={onConfirm} disabled={deleting} autoFocus>
            {deleting ? 'Excluindo…' : `Excluir ${subjectLabel}`}
          </button>
        </footer>
      </section>
    </div>
  );
}
