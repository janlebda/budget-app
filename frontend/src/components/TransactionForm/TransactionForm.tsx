import { useForm } from "react-hook-form";
import { TransactionType } from "../../types/transaction";
import styles from "./TransactionForm.module.scss";
import { toast } from "react-toastify";
import graphqlClient from "../../api/graphClient";
import { useBalance } from "../BalanceBar/useBalance";

interface FormData {
  amount: number;
  type: TransactionType;
  tags: string;
  notes?: string;
}

const TransactionForm = () => {
  const { refreshBalance } = useBalance();
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormData>();

  const onSubmit = async (data: FormData) => {
    const mutation = `
      mutation AddTransaction($transactionDTO: TransactionInput!) {
        addTransaction(transactionDTO: $transactionDTO) {
          id
        }
      }
    `;

    try {
      await graphqlClient(mutation, { transactionDTO: data });
      refreshBalance(null);
      reset();
      toast.success("✅ Transakcja dodana!");
    } catch (error) {
      toast.error((error as Error).message);
    }
  };

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      className={styles["transaction-form"]}
    >
      <h2>Dodaj Transakcję</h2>

      <div className={styles.formGroup}>
        <label htmlFor="transaction-amount">Kwota:</label>
        <input
          id="transaction-amount"
          type="number"
          step="0.01"
          placeholder="Kwota"
          {...register("amount", {
            required: "Kwota jest wymagana",
            valueAsNumber: true,
            min: {
              value: 0.01,
              message: "Kwota musi być większa od zera",
            },
          })}
          className={styles.input}
        />
        {errors.amount && (
          <p className={styles.error}>{errors.amount.message}</p>
        )}
      </div>

      <div className={styles.formGroup}>
        <label htmlFor="transaction-type">Typ:</label>
        <select
          id="transaction-type"
          {...register("type", {
            required: "Typ transakcji jest wymagany",
            validate: (val) =>
              val === "INCOME" ||
              val === "EXPENSE" ||
              "Nieprawidłowy typ transakcji",
          })}
          className={styles.input}
        >
          <option value="">-- wybierz --</option>
          <option value="INCOME">Przychód</option>
          <option value="EXPENSE">Wydatek</option>
        </select>
        {errors.type && <p className={styles.error}>{errors.type.message}</p>}
      </div>

      <div className={styles.formGroup}>
        <label htmlFor="transaction-tags">Tagi:</label>
        <input
          id="transaction-tags"
          type="text"
          {...register("tags", {
            required: "Tagi są wymagane",
            minLength: {
              value: 2,
              message: "Tag musi mieć co najmniej 2 znaki",
            },
            maxLength: {
              value: 70,
              message: "Tagi muszą mieć najwyżej 70 znaków",
            },
          })}
          placeholder="np. jedzenie, transport"
          className={styles.input}
        />
        {errors.tags && <p className={styles.error}>{errors.tags.message}</p>}
      </div>

      <div className={styles.formGroup}>
        <label htmlFor="transaction-notes">Notatki:</label>
        <textarea
          id="transaction-notes"
          {...register("notes", {
            maxLength: {
              value: 200,
              message: "Notatka może mieć maksymalnie 200 znaków",
            },
          })}
          placeholder="Dodatkowe informacje (opcjonalnie)"
          className={styles.textarea}
        />
        {errors.notes && <p className={styles.error}>{errors.notes.message}</p>}
      </div>

      <button type="submit" className={styles.button}>
        Dodaj
      </button>
    </form>
  );
};

export default TransactionForm;
