import { createContext } from "react";

export interface Balance {
  totalIncome: number;
  totalExpense: number;
  balance: number;
}

interface BalanceContextType {
  balance: Balance | null;
  refreshBalance: (days: number | null) => void;
}

export const BalanceContext = createContext<BalanceContextType | undefined>(
  undefined
);
