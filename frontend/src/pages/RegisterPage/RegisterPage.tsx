// src/pages/RegisterPage/RegisterPage.tsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AxiosError } from 'axios';
import { authApi } from '../../api/authApi';
import ErrorMessage from '../../components/ErrorMessage/ErrorMessage';
import styles from './RegisterPage.module.scss';

const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const [username, setUsername] = useState<string>('');
  const [email, setEmail] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [confirmPassword, setConfirmPassword] = useState<string>('');
  const [errors, setErrors] = useState<{ [key: string]: string }>({});

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};

    if (!username.trim()) {
      newErrors.username = 'username jest wymagany.';
    }

    if (!email.trim()) {
      newErrors.email = 'Email jest wymagany.';
    } else if (!/\S+@\S+\.\S+/.test(email)) {
      newErrors.email = 'Nieprawidłowy format email.';
    }

    if (!password) {
      newErrors.password = 'Hasło jest wymagane.';
    } else if (password.length < 6) {
      newErrors.password = 'Hasło musi mieć co najmniej 6 znaków.';
    }

    if (!confirmPassword) {
      newErrors.confirmPassword = 'Powtórzenie hasła jest wymagane.';
    } else if (password !== confirmPassword) {
      newErrors.confirmPassword = 'Hasła muszą być identyczne.';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({}); // Reset błędów

    if (!validate()) return;

    try {
      await authApi.register({ username, email, password });
      alert('Rejestracja udana. Możesz się teraz zalogować.');
      navigate('/login');
    } catch (error) {
      if (error instanceof AxiosError) {
        if (error.response?.data) {
          const errorData = error.response.data;
          setErrors(errorData.errors || { general: errorData.message || 'Błąd rejestracji. Spróbuj ponownie.' });
        } else {
          setErrors({ general: 'Błąd rejestracji. Spróbuj ponownie.' });
        }
      } else {
        setErrors({ general: 'Wystąpił nieznany błąd podczas rejestracji.' });
      }
    }
  };

  return (
    <div className={styles.container}>
      <h2>Rejestracja</h2>
      <form onSubmit={handleRegister} className={styles.form}>
        <div className={styles.formGroup}>
          <label>Nazwa użytkownika:</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            className={styles.input}
          />
          {errors.username && <ErrorMessage message={errors.username} />}
        </div>
        <div className={styles.formGroup}>
          <label>Email:</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            className={styles.input}
          />
          {errors.email && <ErrorMessage message={errors.email} />}
        </div>
        <div className={styles.formGroup}>
          <label>Hasło:</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            className={styles.input}
          />
          {errors.password && <ErrorMessage message={errors.password} />}
        </div>
        <div className={styles.formGroup}>
          <label>Powtórz hasło:</label>
          <input
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
            className={styles.input}
          />
          {errors.confirmPassword && <ErrorMessage message={errors.confirmPassword} />}
        </div>
        {errors.general && <ErrorMessage message={errors.general} />}
        <button type="submit" className={styles.button}>Zarejestruj</button>
      </form>
    </div>
  );
};

export default RegisterPage;
