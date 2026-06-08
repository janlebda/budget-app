import { useCallback, useEffect, useState } from "react";
import { GroupDebt, Id, groupsApi } from "../../api/groupsApi";
import { useParams } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import styles from "./Group.module.scss";
import ConfirmModal from "../../components/ConfirmModal/ConfirmModal";
import GroupDebtItem from "./GroupDebtItem"; // NOWY IMPORT

const GroupDebtsPage = () => {
  const { groupId } = useParams();
  const { user } = useAuth();
  const [debts, setDebts] = useState<GroupDebt[]>([]);
  const [ownerId, setOwnerId] = useState<Id | null>(null);
  const [errorMessage, setErrorMessage] = useState("");
  const [debtToDelete, setDebtToDelete] = useState<GroupDebt | null>(null);
  const currentUserId = user?.id === undefined ? "" : String(user.id);

  const getErrorMessage = (error: unknown, fallback: string) => {
    if (error instanceof Error && error.message.trim()) {
      return error.message.replace(/^Wystąpił błąd:\s*/i, "");
    }
    return fallback;
  };

  const fetchDebtsData = useCallback(async () => {
    if (!groupId) return null;
    const [debtsData, groupsData] = await Promise.all([
      groupsApi.getDebts(groupId),
      groupsApi.getGroups(),
    ]);
    return {
      debtsData,
      ownerId: groupsData.find((group) => String(group.id) === String(groupId))?.ownerId ?? null,
    };
  }, [groupId]);

  const refreshDebts = useCallback(async () => {
    try {
      const data = await fetchDebtsData();
      if (!data) return;
      setErrorMessage("");
      setDebts(data.debtsData);
      setOwnerId(data.ownerId);
    } catch (error: unknown) {
      console.error("Błąd pobierania długów:", error);
      setDebts([]);
      setErrorMessage(getErrorMessage(error, "Nie udało się pobrać długów grupy."));
    }
  }, [fetchDebtsData]);

  useEffect(() => {
    let ignore = false;
    fetchDebtsData()
      .then((data) => {
        if (ignore || !data) return;
        setErrorMessage("");
        setDebts(data.debtsData);
        setOwnerId(data.ownerId);
      })
      .catch((error: unknown) => {
        if (ignore) return;
        console.error("Błąd pobierania długów:", error);
        setDebts([]);
        setErrorMessage(getErrorMessage(error, "Nie udało się pobrać długów grupy."));
      });
    return () => { ignore = true; };
  }, [fetchDebtsData]);

  const handleDeleteDebt = async () => {
    if (!debtToDelete) return;
    try {
      setErrorMessage("");
      await groupsApi.deleteDebt(debtToDelete.id);
      setDebtToDelete(null);
      refreshDebts();
    } catch (error: unknown) {
      console.error("Błąd usuwania długu:", error);
      setErrorMessage(getErrorMessage(error, "Nie udało się usunąć długu."));
    }
  };

  const handleMarkDebtAsPaid = async (debtId: Id) => {
    try {
      setErrorMessage("");
      await groupsApi.markDebtAsPaid(debtId);
      refreshDebts();
    } catch (error: unknown) {
      console.error("Błąd oznaczania długu jako opłaconego:", error);
      setErrorMessage(getErrorMessage(error, "Nie udało się oznaczyć długu jako opłaconego."));
    }
  };

  const handleConfirmDebtPayment = async (debtId: Id) => {
    try {
      setErrorMessage("");
      await groupsApi.confirmDebtPayment(debtId);
      refreshDebts();
    } catch (error: unknown) {
      console.error("Błąd potwierdzania spłaty długu:", error);
      setErrorMessage(getErrorMessage(error, "Nie udało się potwierdzić spłaty długu."));
    }
  };

  return (
    <div className={styles.container}>
      <h2>Długi w grupie</h2>
      {errorMessage && <p className={styles.errorMessage}>{errorMessage}</p>}

      <ul className={styles.debtsList}>
        {debts.map((debt) => (
          // Użycie wyabstrahowanego komponentu usuwa duplikację kodu HTML i funkcji pomocniczych
          <GroupDebtItem
            key={debt.id}
            debt={debt}
            currentUserId={currentUserId}
            isGroupOwner={String(ownerId) === currentUserId}
            onMarkAsPaid={handleMarkDebtAsPaid}
            onConfirmPayment={handleConfirmDebtPayment}
            onDelete={setDebtToDelete}
          />
        ))}
      </ul>

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

export default GroupDebtsPage;