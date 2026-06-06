import { useState, useCallback, useMemo, type ReactNode } from "react";
import graphqlClient from "../../api/graphClient";
import { BalanceContext, type Balance } from "./BalanceContext";

interface UserBalanceResponse {
  userBalance: Balance;
}

export const BalanceProvider = ({ children }: { children: ReactNode }) => {
  const [balance, setBalance] = useState<Balance | null>(null);

  const refreshBalance = useCallback(async (days: number | null) => {
    const query = `
      query($days: Float) {
        userBalance(days: $days) {
          totalIncome
          totalExpense
          balance
        }
      }
    `;
    try {
      const response = await graphqlClient<UserBalanceResponse>(query, { days });
      setBalance(response.data.userBalance);
    } catch (error) {
      console.error("Błąd pobierania bilansu:", error);
    }
  }, []);

  const value = useMemo(
    () => ({ balance, refreshBalance }),
    [balance, refreshBalance]
  );

  return (
    <BalanceContext.Provider value={value}>
      {children}
    </BalanceContext.Provider>
  );
};
