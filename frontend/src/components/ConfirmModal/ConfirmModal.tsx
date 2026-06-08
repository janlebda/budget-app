import { type ReactNode, useEffect } from "react";
import styles from "./ConfirmModal.module.scss";

interface ConfirmModalProps {
  visible: boolean;
  title?: string;
  message: ReactNode;
  confirmLabel?: string;
  cancelLabel?: string;
  onConfirm: () => void;
  onCancel: () => void;
}

const ConfirmModal = ({
  visible,
  title = "Potwierdź akcję",
  message,
  confirmLabel = "Potwierdź",
  cancelLabel = "Anuluj",
  onConfirm,
  onCancel,
}: ConfirmModalProps) => {
  
  // Obsługa klawisza Escape w sposób dostępny dla czytników i klawiatury
  useEffect(() => {
    if (!visible) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        onCancel();
      }
    };

    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [visible, onCancel]);

  if (!visible) return null;

  return (
    // aria-hidden="true" sprawia, że tło jest ignorowane przez czytniki, 
    // rozwiązując błąd "Non-interactive elements should not be assigned... listeners"
    <div 
      className={styles.modalOverlay} 
      onClick={onCancel} 
      aria-hidden="true"
    >
      <dialog
        className={styles.modal}
        open
        aria-modal="true"
        aria-labelledby="confirm-modal-title"
        // Zatrzymujemy propagację, aby kliknięcie w modal nie zamykało go
        onClick={(e) => e.stopPropagation()}
      >
        <h3 id="confirm-modal-title">{title}</h3>
        <p>{message}</p>
        <div className={styles.actions}>
          <button type="button" className={styles.cancel} onClick={onCancel}>
            {cancelLabel}
          </button>
          <button type="button" className={styles.confirm} onClick={onConfirm}>
            {confirmLabel}
          </button>
        </div>
      </dialog>
    </div>
  );
};

export default ConfirmModal;