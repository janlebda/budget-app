import { GroupDebt } from "../../api/groupsApi";
import styles from "./Group.module.scss";

interface GroupDebtItemProps {
  debt: GroupDebt;
  currentUserId: string;
  isGroupOwner: boolean;
  onMarkAsPaid: (id: any) => void;
  onConfirmPayment: (id: any) => void;
  onDelete: (debt: GroupDebt) => void;
}

const GroupDebtItem = ({
  debt,
  currentUserId,
  isGroupOwner,
  onMarkAsPaid,
  onConfirmPayment,
  onDelete,
}: GroupDebtItemProps) => {
  
  const canManageDebt =
    isGroupOwner ||
    String(debt.debtor.id) === currentUserId ||
    String(debt.creditor.id) === currentUserId;

  const canMarkDebtAsPaid =
    String(debt.debtor.id) === currentUserId && !debt.paidByDebtor;

  const canConfirmDebtPayment =
    String(debt.creditor.id) === currentUserId &&
    debt.paidByDebtor &&
    !debt.confirmedByCreditor;

  const getDebtStatusLabel = () => {
    if (debt.confirmedByCreditor) return "Spłata potwierdzona";
    if (debt.paidByDebtor) return "Oczekuje na potwierdzenie";
    return "Nieopłacony";
  };

  const getDebtStatusStyle = () => {
    if (debt.confirmedByCreditor) return styles.statusPaid;
    if (debt.paidByDebtor) return styles.statusPending;
    return styles.statusOpen;
  };

  return (
    <li>
      <strong className={styles.debtorName}>{debt.debtor.email}</strong>{" "}
      jest winien{" "}
      <strong className={styles.creditorName}>{debt.creditor.email}</strong>{" "}
      {debt.amount.toFixed(2)} zł za <strong>{debt.title}</strong>
      <span className={`${styles.statusBadge} ${getDebtStatusStyle()}`}>
        {getDebtStatusLabel()}
      </span>
      {canMarkDebtAsPaid && (
        <button
          type="button"
          className={styles.button}
          onClick={() => onMarkAsPaid(debt.id)}
        >
          Oznacz jako opłacony
        </button>
      )}
      {canConfirmDebtPayment && (
        <button
          type="button"
          className={styles.button}
          onClick={() => onConfirmPayment(debt.id)}
        >
          Potwierdź spłatę
        </button>
      )}
      {canManageDebt && (
        <button
          type="button"
          className={styles.deleteButton}
          onClick={() => onDelete(debt)}
        >
          Usuń
        </button>
      )}
    </li>
  );
};

export default GroupDebtItem;