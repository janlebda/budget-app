import { useContext } from "react";
import { BalanceContext } from "./BalanceContext";

export const useBalance = () => {
  const context = useContext(BalanceContext);
  if (!context) {
    throw new Error("useBalance must be used within a BalanceProvider");
  }
  return context;
};
