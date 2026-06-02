// src/components/ErrorMessage/ErrorMessage.tsx
import styles from './ErrorMessage.module.scss';

interface ErrorMessageProps {
  message: string;
}

const ErrorMessage = ({ message }: ErrorMessageProps) => {
  return <div className={styles.error}>{message}</div>;
};

export default ErrorMessage;
