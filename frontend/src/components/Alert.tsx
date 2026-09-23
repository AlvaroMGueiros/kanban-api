interface AlertProps {
  message: string;
  tone: 'danger' | 'success';
  onClose: () => void;
}

export function Alert({ message, tone, onClose }: AlertProps) {
  return (
    <div className={`alert alert--${tone}`} role={tone === 'danger' ? 'alert' : 'status'}>
      <span>{message}</span>
      <button type="button" onClick={onClose} aria-label="Fechar mensagem">×</button>
    </div>
  );
}
