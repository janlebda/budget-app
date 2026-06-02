import { useCallback, useEffect, useRef, useState } from "react";
import { GroupDebt, groupsApi } from "../../api/groupsApi";
import { useAuth } from "../../context/AuthContext";
import styles from "./Group.module.scss";
import AddGroupTransaction from "./AddGroupTransaction";
import ConfirmModal from "../../components/ConfirmModal/ConfirmModal";

interface Group {
  id: number | string;
  name: string;
  ownerId: number | string;
}

interface Member {
  id: number | string;
  userId: number | string;
  groupId: number | string;
  userEmail: string;
}

interface Props {
  group: Group;
  onBack: () => void;
}

const GroupMembersPage = ({ group, onBack }: Props) => {
  const { user } = useAuth();
  const [members, setMembers] = useState<Member[]>([]);
  const [newMemberEmail, setNewMemberEmail] = useState("");
  const [debts, setDebts] = useState<GroupDebt[]>([]);
  const [errorMessage, setErrorMessage] = useState("");
  const [addMemberError, setAddMemberError] = useState("");
  const [debtTitle, setDebtTitle] = useState("");
  const [debtAmount, setDebtAmount] = useState("");
  const [debtorId, setDebtorId] = useState("");
  const [creditorId, setCreditorId] = useState("");
  const [debtFormError, setDebtFormError] = useState("");
  const [debtToDelete, setDebtToDelete] = useState<GroupDebt | null>(null);
  const requestIdRef = useRef(0);

  const isGroupOwner =
    user?.id !== undefined && String(user.id) === String(group.ownerId);
  const currentUserId = user?.id !== undefined ? String(user.id) : "";

  const getErrorMessage = (error: unknown, fallback: string) => {
    if (error instanceof Error && error.message.trim()) {
      return error.message.replace(/^Wystąpił błąd:\s*/i, "");
    }

    return fallback;
  };

  const loadMembersData = useCallback(async () => {
    return Promise.allSettled([
      groupsApi.getGroupMembers(group.id),
      groupsApi.getDebts(group.id),
    ]);
  }, [group.id]);

  const applyMembersResults = useCallback(
    ([membersResult, debtsResult]: Awaited<ReturnType<typeof loadMembersData>>) => {
      setErrorMessage("");

      if (membersResult.status === "fulfilled") {
        const membersData = membersResult.value;
        setMembers(membersData);

        if (membersData.length === 0) {
          setDebtorId("");
          setCreditorId("");
        } else {
          setDebtorId((current) =>
            membersData.some((member) => String(member.userId) === current)
              ? current
              : String(membersData[0].userId)
          );
          setCreditorId((current) =>
            membersData.some((member) => String(member.userId) === current)
              ? current
              : String(membersData[0].userId)
          );
        }
      } else {
        console.error("Błąd pobierania członków grupy:", membersResult.reason);
        setMembers([]);
        setErrorMessage((current) =>
          current || "Nie udało się pobrać członków grupy."
        );
      }

      if (debtsResult.status === "fulfilled") {
        setDebts(debtsResult.value);
      } else {
        console.error("Błąd pobierania długów grupy:", debtsResult.reason);
        setDebts([]);
        setErrorMessage((current) =>
          current || "Nie udało się pobrać długów grupy."
        );
      }
    },
    []
  );

  const refreshMembers = useCallback(async () => {
    const requestId = ++requestIdRef.current;
    const results = await loadMembersData();

    if (requestId !== requestIdRef.current) return;
    applyMembersResults(results);
  }, [applyMembersResults, loadMembersData]);

  useEffect(() => {
    const requestId = ++requestIdRef.current;
    let ignore = false;

    loadMembersData().then((results) => {
      if (ignore || requestId !== requestIdRef.current) return;
      applyMembersResults(results);
    });

    return () => {
      ignore = true;
    };
  }, [applyMembersResults, loadMembersData]);

  const handleAddMember = async () => {
    if (!isGroupOwner) {
      setAddMemberError("Tylko właściciel grupy może dodawać członków.");
      return;
    }

    const email = newMemberEmail.trim();
    if (!email) {
      setAddMemberError("Podaj email użytkownika.");
      return;
    }

    try {
      setAddMemberError("");
      await groupsApi.addMember(group.id, email);
      setNewMemberEmail("");
      refreshMembers();
    } catch (error: unknown) {
      console.error("Błąd dodawania członka:", error);
      setAddMemberError(getErrorMessage(error, "Nie udało się dodać członka."));
    }
  };

  const handleRemove = async (id: number | string) => {
    if (!isGroupOwner) {
      setErrorMessage("Tylko właściciel grupy może usuwać członków.");
      return;
    }

    try {
      setErrorMessage("");
      await groupsApi.removeMember(id);
      refreshMembers();
    } catch (error: unknown) {
      console.error("Błąd usuwania członka:", error);
      setErrorMessage(
        getErrorMessage(error, "Nie udało się usunąć członka grupy.")
      );
    }
  };

  const handleCreateDebt = async () => {
    const title = debtTitle.trim();
    const amount = Number(debtAmount);

    if (!title) {
      setDebtFormError("Podaj tytuł długu.");
      return;
    }

    if (!Number.isFinite(amount) || amount <= 0) {
      setDebtFormError("Podaj kwotę większą od zera.");
      return;
    }

    if (!debtorId || !creditorId) {
      setDebtFormError("Wybierz dłużnika i wierzyciela.");
      return;
    }

    if (debtorId === creditorId) {
      setDebtFormError("Dłużnik i wierzyciel muszą być różnymi osobami.");
      return;
    }

    if (!isGroupOwner && debtorId !== currentUserId && creditorId !== currentUserId) {
      setDebtFormError("Możesz dodać tylko dług, którego jesteś uczestnikiem.");
      return;
    }

    try {
      setDebtFormError("");
      await groupsApi.createDebt(group.id, debtorId, creditorId, amount, title);
      setDebtTitle("");
      setDebtAmount("");
      refreshMembers();
    } catch (error: unknown) {
      console.error("Błąd dodawania długu:", error);
      setDebtFormError(getErrorMessage(error, "Nie udało się dodać długu."));
    }
  };

  const handleDeleteDebt = async () => {
    if (!debtToDelete) return;

    try {
      setErrorMessage("");
      await groupsApi.deleteDebt(debtToDelete.id);
      setDebtToDelete(null);
      refreshMembers();
    } catch (error: unknown) {
      console.error("Błąd usuwania długu:", error);
      setErrorMessage(getErrorMessage(error, "Nie udało się usunąć długu."));
    }
  };

  const canManageDebt = (debt: GroupDebt) =>
    isGroupOwner ||
    String(debt.debtor.id) === currentUserId ||
    String(debt.creditor.id) === currentUserId;

  const canMarkDebtAsPaid = (debt: GroupDebt) =>
    String(debt.debtor.id) === currentUserId && !debt.paidByDebtor;

  const canConfirmDebtPayment = (debt: GroupDebt) =>
    String(debt.creditor.id) === currentUserId &&
    debt.paidByDebtor &&
    !debt.confirmedByCreditor;

  const getDebtStatusLabel = (debt: GroupDebt) => {
    if (debt.confirmedByCreditor) return "Spłata potwierdzona";
    if (debt.paidByDebtor) return "Oczekuje na potwierdzenie";
    return "Nieopłacony";
  };

  const handleMarkDebtAsPaid = async (debtId: number | string) => {
    try {
      setErrorMessage("");
      await groupsApi.markDebtAsPaid(debtId);
      refreshMembers();
    } catch (error: unknown) {
      console.error("Błąd oznaczania długu jako opłaconego:", error);
      setErrorMessage(
        getErrorMessage(error, "Nie udało się oznaczyć długu jako opłaconego.")
      );
    }
  };

  const handleConfirmDebtPayment = async (debtId: number | string) => {
    try {
      setErrorMessage("");
      await groupsApi.confirmDebtPayment(debtId);
      refreshMembers();
    } catch (error: unknown) {
      console.error("Błąd potwierdzania spłaty długu:", error);
      setErrorMessage(
        getErrorMessage(error, "Nie udało się potwierdzić spłaty długu.")
      );
    }
  };

  return (
    <div className={styles.container}>
      <button onClick={onBack} className={styles.backButton}>
        Wróć do grup
      </button>
      <h2>Członkowie grupy: {group.name}</h2>

      {errorMessage && <p className={styles.errorMessage}>{errorMessage}</p>}
      {!isGroupOwner && (
        <p className={styles.infoMessage}>
          Tylko właściciel grupy może dodawać i usuwać członków.
        </p>
      )}

      {isGroupOwner && (
        <>
          <div className={styles.form}>
            <input
              type="text"
              placeholder="Email użytkownika"
              value={newMemberEmail}
              onChange={(e) => setNewMemberEmail(e.target.value)}
            />
            <button onClick={handleAddMember}>Dodaj członka</button>
          </div>
          {addMemberError && (
            <p className={styles.errorMessage}>{addMemberError}</p>
          )}
        </>
      )}

      <AddGroupTransaction
        groupId={group.id}
        members={members}
        onTransactionAdded={refreshMembers}
      />

      {members.length > 1 && (
        <div className={styles.debtForm}>
          <h3>Dodaj ręczny dług</h3>
          <div className={styles.formsContainer}>
            <input
              type="text"
              placeholder="Tytuł"
              value={debtTitle}
              onChange={(e) => setDebtTitle(e.target.value)}
              className={styles.input}
            />
            <input
              type="number"
              min="0.01"
              step="0.01"
              placeholder="Kwota"
              value={debtAmount}
              onChange={(e) => setDebtAmount(e.target.value)}
              className={styles.input}
            />
            <select
              value={debtorId}
              onChange={(e) => setDebtorId(e.target.value)}
              className={styles.input}
            >
              <option value="">Dłużnik</option>
              {members.map((member) => (
                <option key={member.id} value={member.userId}>
                  {member.userEmail}
                </option>
              ))}
            </select>
            <select
              value={creditorId}
              onChange={(e) => setCreditorId(e.target.value)}
              className={styles.input}
            >
              <option value="">Wierzyciel</option>
              {members.map((member) => (
                <option key={member.id} value={member.userId}>
                  {member.userEmail}
                </option>
              ))}
            </select>
            <button type="button" className={styles.button} onClick={handleCreateDebt}>
              Dodaj dług
            </button>
          </div>
          {debtFormError && (
            <p className={styles.errorMessage}>{debtFormError}</p>
          )}
        </div>
      )}

      <ul className={styles.memberList}>
        {members.map((member) => (
          <li key={member.id}>
            {member.userEmail}
            {String(member.userId) === String(group.ownerId) && (
              <span className={styles.adminLabel}>(admin)</span>
            )}
            {isGroupOwner && String(member.userId) !== String(group.ownerId) && (
              <button
                className={styles.deleteButton}
                onClick={() => handleRemove(member.id)}
              >
                Usuń
              </button>
            )}
          </li>
        ))}
      </ul>

      {debts.length > 0 && (
        <div className={styles.debtsSection}>
          <h3>Długi w grupie:</h3>
          <ul className={styles.debtsList}>
            {debts.map((debt) => (
              <li key={debt.id}>
                <strong className={styles.debtorName}>
                  {debt.debtor.email}
                </strong>{" "}
                jest winien{" "}
                <strong className={styles.creditorName}>
                  {debt.creditor.email}
                </strong>{" "}
                {debt.amount.toFixed(2)} zł za <strong>{debt.title}</strong>
                <span
                  className={`${styles.statusBadge} ${
                    debt.confirmedByCreditor
                      ? styles.statusPaid
                      : debt.paidByDebtor
                        ? styles.statusPending
                        : styles.statusOpen
                  }`}
                >
                  {getDebtStatusLabel(debt)}
                </span>
                {canMarkDebtAsPaid(debt) && (
                  <button
                    type="button"
                    className={styles.button}
                    onClick={() => handleMarkDebtAsPaid(debt.id)}
                  >
                    Oznacz jako opłacony
                  </button>
                )}
                {canConfirmDebtPayment(debt) && (
                  <button
                    type="button"
                    className={styles.button}
                    onClick={() => handleConfirmDebtPayment(debt.id)}
                  >
                    Potwierdź spłatę
                  </button>
                )}
                {canManageDebt(debt) && (
                  <button
                    type="button"
                    className={styles.deleteButton}
                    onClick={() => setDebtToDelete(debt)}
                  >
                    Usuń
                  </button>
                )}
              </li>
            ))}
          </ul>
        </div>
      )}

      <ConfirmModal
        visible={Boolean(debtToDelete)}
        title="Usuń dług"
        message="Czy na pewno chcesz usunąć ten dług?"
        confirmLabel="Usuń"
        onConfirm={handleDeleteDebt}
        onCancel={() => setDebtToDelete(null)}
      />
    </div>
  );
};

export default GroupMembersPage;
