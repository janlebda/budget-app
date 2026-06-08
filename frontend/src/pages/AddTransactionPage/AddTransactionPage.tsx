// src/pages/AddTransactionPage/AddTransactionPage.tsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Transaction, transactionsApi } from '../../api/transactionsApi';
import ErrorMessage from '../../components/ErrorMessage/ErrorMessage';
import CustomDialog from '../../components/CustomDialog/CustomDialog';
import styles from './AddTransactionPage.module.scss';

const AddTransactionPage = () => {
  const navigate = useNavigate();
  const [amount, setAmount] = useState<number>(0);
  const [type, setType] = useState<'INCOME' | 'EXPENSE'>('EXPENSE');
  const [tags, setTags] = useState<string>('');
  const [notes, setNotes] = useState<string>('');
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [showDialog, setShowDialog] = useState<boolean>(false);
  const [dialogMessage, setDialogMessage] = useState<string>('');

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};

    if (amount <= 0) {
      newErrors.amount = 'Kwota musi być większa od zera.';
    }

    if (!['INCOME', 'EXPENSE'].includes(type)) {
      newErrors.type = 'Wybierz poprawny typ transakcji.';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // POPRAWKA: Zamiana React.FormEvent na React.SyntheticEvent rozwiązuje błąd S1874
  const handleSubmit = async (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!validate()) return;

    const newTransaction: Transaction = {
      amount,
      type,
      tags: tags.trim(),
      notes,
    };

    try {
      await transactionsApi.create(newTransaction);
      alert('Transakcja dodana pomyślnie!');
      navigate('/transactions');
    } catch (error: unknown) {
      setDialogMessage('Błąd dodawania transakcji.' + error);
      setShowDialog(true);
    }
  };

  const closeDialog = () => {
    setShowDialog(false);
  };

  return (
    <div className={styles.container}>
      <h2>Dodaj Transakcję</h2>
      <form onSubmit={handleSubmit} className={styles.form}>
        <div className={styles.formGroup}>
          <label htmlFor="amount">Kwota:</label>
          <input
            id="amount"
            type="number"
            value={amount}
            onChange={(e) => setAmount(Number.parseFloat(e.target.value))}
            required
            className={styles.input}
          />
          {errors.amount && <ErrorMessage message={errors.amount} />}
        </div>
        <div className={styles.formGroup}>
          <label htmlFor="type">Typ:</label>
          <select
            id="type"
            value={type}
            onChange={(e) => setType(e.target.value as 'INCOME' | 'EXPENSE')}
            className={styles.input}
          >
            <option value="INCOME">Przychód</option>
            <option value="EXPENSE">Wydatek</option>
          </select>
          {errors.type && <ErrorMessage message={errors.type} />}
        </div>
        <div className={styles.formGroup}>
          <label htmlFor="tags">Tag:</label>
          <input
            id="tags"
            type="text"
            value={tags}
            onChange={(e) => setTags(e.target.value)}
            placeholder="np. jedzenie"
            className={styles.input}
          />
        </div>
        <div className={styles.formGroup}>
          <label htmlFor="notes">Notatki:</label>
          <textarea
            id="notes"
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            placeholder="Dodatkowe informacje"
            className={styles.textarea}
          />
        </div>
        {errors.general && <ErrorMessage message={errors.general} />}
        <button type="submit" className={styles.button}>Dodaj</button>
      </form>

      {showDialog && (
        <CustomDialog
          title="Błąd"
          message={dialogMessage}
          onClose={closeDialog}
        />
      )}
    </div>
  );
};

export default AddTransactionPage;