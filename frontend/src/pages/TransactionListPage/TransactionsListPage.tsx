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
      } catch (error: unknown) {
        setError('Nie udało się pobrać transakcji.' + error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchTransactions();
  }, []);

  return (
    <div className={styles.container}>
      <h2>Lista Transakcji</h2>
      {isLoading ? (
        <p>Ładowanie...</p>
      ) : error ? (
        <p className={styles.error}>{error}</p>
      ) : (
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
      )}
    </div>
  );
};

export default TransactionsListPage;
