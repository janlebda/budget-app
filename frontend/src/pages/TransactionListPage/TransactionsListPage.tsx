// src/pages/TransactionsListPage/TransactionsListPage.tsx
import { useEffect, useState } from 'react';
import { Transaction, transactionsApi } from '../../api/transactionsApi';
import styles from './TransactioListPage.module.scss';

const TransactionsListPage = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    const fetchTransactions = async () => {
      try {
        const data = await transactionsApi.getAll();
        setTransactions(data);
      } catch (err: unknown) {
        setError('Nie udało się pobrać transakcji.' + err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchTransactions();
  }, []);

  // POPRAWKA S3358: Wyciągamy zagnieżdżony ternary operator do czystej struktury if/else
  let content;

  if (isLoading) {
    content = <p>Ładowanie...</p>;
  } else if (error) {
    content = <p className={styles.error}>{error}</p>;
  } else {
    content = (
      <table className={styles.table}>
        <thead>
          <tr>
            <th>Typ</th>
            <th>Kwota</th>
            <th>Tagi</th>
            <th>Notatki</th>
          </tr>
        </thead>
        <tbody>
          {transactions.map(tx => (
            <tr key={tx.id}>
              <td className={tx.type === 'INCOME' ? styles.income : styles.expense}>
                {tx.type}
              </td>
              <td>{tx.amount} zł</td>
              <td>{tx.tags || '-'}</td>
              <td>{tx.notes || '-'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    );
  }

  return (
    <div className={styles.container}>
      <h2>Lista Transakcji</h2>
      {/* W JSX wstrzykujemy już gotowy, czysty komponent */}
      {content}
    </div>
  );
};

export default TransactionsListPage;