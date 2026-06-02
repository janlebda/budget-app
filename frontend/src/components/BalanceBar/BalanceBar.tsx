import { useEffect, useState } from "react";
import styles from "./BalanceBar.module.scss";
import { useAuth } from "../../context/AuthContext";
import { useBalance } from "./useBalance";

const RANGE_TO_DAYS: Record<string, number | null> = {
  minute: 1 / 1440,
  "1": 1,
  "7": 7,
  "30": 30,
  "365": 365,
  all: null,
};

const BalanceBar = () => {
  const [range, setRange] = useState("all");
  const { isAuthenticated } = useAuth();
  const { balance, refreshBalance } = useBalance();

  useEffect(() => {
    if (!isAuthenticated) return;
    refreshBalance(RANGE_TO_DAYS[range]);
  }, [isAuthenticated, range, refreshBalance]);

  if (!isAuthenticated || !balance) return null;

  return (
    <div className={styles.balanceBar}>
      <div className={styles.inlineWrap}>
        <div className={styles.summary}>
          <span className={styles.label}>Twój bilans:</span>
          <span className={styles.value}>{balance.balance.toFixed(2)} zł</span>
          <span className={styles.label}> | Wydatki:</span>
          <span className={styles.expense}>
            {balance.totalExpense.toFixed(2)} zł
          </span>
          <span className={styles.label}> | Przychody:</span>
          <span className={styles.income}>
            {balance.totalIncome.toFixed(2)} zł
          </span>
        </div>

        <div className={styles.rangeSelector}>
          <label htmlFor="range">Zakres: </label>
          <select id="range" value={range} onChange={(e) => setRange(e.target.value)}>
            <option value="minute">Ostatnia minuta</option>
            <option value="1">24h</option>
            <option value="7">7 dni</option>
            <option value="30">30 dni</option>
            <option value="365">365 dni</option>
            <option value="all">Wszystko</option>
          </select>
        </div>
      </div>
    </div>
  );
};

export default BalanceBar;
