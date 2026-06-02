export type TransactionType = "INCOME" | "EXPENSE";

export interface Transaction {
  id: number;
  amount: number;
  type: TransactionType;
  tags: string;
  notes: string;
  timestamp: string;
}
