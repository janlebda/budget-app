// src/components/CustomDialog/CustomDialog.tsx
import styles from './CustomDialog.module.scss';

interface CustomDialogProps {
  title: string;
  message: string;
  onClose: () => void;
}

const CustomDialog = ({ title, message, onClose }: CustomDialogProps) => {
  return (
    <div className={styles.overlay}>
      <div className={styles.dialog}>
        <h3>{title}</h3>
        <p>{message}</p>
        <button onClick={onClose}>Zamknij</button>
      </div>
    </div>
  );
};

export default CustomDialog;
