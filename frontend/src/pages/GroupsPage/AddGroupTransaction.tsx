import { useMemo, useState, type FormEvent } from "react";
import { Id, groupsApi } from "../../api/groupsApi";
import { useBalance } from "../../components/BalanceBar/useBalance";
import { useAuth } from "../../context/AuthContext";
import styles from "./Group.module.scss";

interface Member {
  id: Id;
  userId: Id;
  groupId: Id;
  userEmail: string;
}

interface Props {
  groupId: Id;
  members: Member[];
  onTransactionAdded: () => void;
}

const AddGroupTransaction = ({
  groupId,
  members,
  onTransactionAdded,
}: Props) => {
  const { user } = useAuth();
  const { refreshBalance } = useBalance();
  const [title, setTitle] = useState("");
  const [amount, setAmount] = useState("");
  const [type, setType] = useState<"EXPENSE" | "INCOME">("EXPENSE");
  const [selectedUserIds, setSelectedUserIds] = useState<Id[]>([]);
  const [hasCustomParticipants, setHasCustomParticipants] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const currentUserId = user?.id !== undefined ? String(user.id) : "";

  const memberIds = useMemo(() => members.map((member) => member.userId), [members]);
  const effectiveSelectedUserIds = useMemo(() => {
    if (!hasCustomParticipants) return memberIds;

    const existingMemberIds = new Set(memberIds.map(String));
    return selectedUserIds.filter((id) => existingMemberIds.has(String(id)));
  }, [hasCustomParticipants, memberIds, selectedUserIds]);

  const getErrorMessage = (error: unknown, fallback: string) => {
    if (error instanceof Error && error.message.trim()) {
      return error.message.replace(/^Wystąpił błąd:\s*/i, "");
    }

    return fallback;
  };

  const toggleUserSelection = (userId: Id) => {
    setHasCustomParticipants(true);
    setSelectedUserIds((current) =>
      (hasCustomParticipants ? current : memberIds).some(
        (id) => String(id) === String(userId)
      )
        ? (hasCustomParticipants ? current : memberIds).filter(
            (id) => String(id) !== String(userId)
          )
        : [...(hasCustomParticipants ? current : memberIds), userId]
    );
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const parsedAmount = Number(amount);
    const uniqueSelectedUserIds = Array.from(
      new Map(effectiveSelectedUserIds.map((id) => [String(id), id])).values()
    );

    if (!title.trim()) {
      setErrorMessage("Podaj tytuł transakcji.");
      return;
    }

    if (!Number.isFinite(parsedAmount) || parsedAmount <= 0) {
      setErrorMessage("Podaj kwotę większą od zera.");
      return;
    }

    if (uniqueSelectedUserIds.length < 2) {
      setErrorMessage("Wybierz co najmniej dwóch uczestników transakcji.");
      return;
    }

    if (!uniqueSelectedUserIds.some((id) => String(id) === currentUserId)) {
      setErrorMessage("Musisz być uczestnikiem transakcji grupowej.");
      return;
    }

    try {
      setErrorMessage("");
      await groupsApi.addGroupTransaction(
        groupId,
        parsedAmount,
        type,
        title.trim(),
        uniqueSelectedUserIds
      );
      setTitle("");
      setAmount("");
      setType("EXPENSE");
      setSelectedUserIds([]);
      setHasCustomParticipants(false);
      onTransactionAdded();
      refreshBalance(null);
    } catch (error: unknown) {
      console.error("Błąd dodawania transakcji grupowej:", error);
      setErrorMessage(
        getErrorMessage(error, "Nie udało się dodać transakcji grupowej.")
      );
    }
  };

  return (
    <form onSubmit={handleSubmit} className={styles.form}>
      <h3>Dodaj nowy {type === "EXPENSE" ? "wydatek" : "przychód"}</h3>
      <div className={styles.formsContainer}>
        <input
          type="text"
          placeholder="Tytuł"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          className={styles.input}
        />
        <input
          type="number"
          min="0.01"
          step="0.01"
          placeholder="Kwota"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          className={styles.input}
        />
        <select
          value={type}
          onChange={(e) => setType(e.target.value as "EXPENSE" | "INCOME")}
          className={styles.input}
        >
          <option value="EXPENSE">Wydatek</option>
          <option value="INCOME">Przychód</option>
        </select>
        <fieldset className={styles.participants}>
          <legend>Uczestnicy transakcji</legend>
          {members.map((member) => (
            <label key={member.id} className={styles.participantOption}>
              <input
                type="checkbox"
                checked={effectiveSelectedUserIds.some(
                  (id) => String(id) === String(member.userId)
                )}
                onChange={() => toggleUserSelection(member.userId)}
              />
              <span>{member.userEmail}</span>
            </label>
          ))}
        </fieldset>
        <button type="submit" className={styles.button}>
          Dodaj
        </button>
      </div>
      {errorMessage && <p className={styles.errorMessage}>{errorMessage}</p>}
    </form>
  );
};

export default AddGroupTransaction;
