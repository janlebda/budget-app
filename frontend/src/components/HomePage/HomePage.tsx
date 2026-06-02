import styles from "./HomePage.module.scss";
import { Link } from "react-router-dom";

const HomePage = () => {
  return (
    <div className={styles.home}>
      <h1>Witaj w Budget App!</h1>
      <p>Zarządzaj swoimi finansami łatwo i szybko. Twórz, edytuj i analizuj swoje transakcje.</p>
      <div className={styles.buttons}>
        <Link to="/add-transaction" className={styles.btn}>Dodaj Transakcję</Link>
        <Link to="/transactions" className={styles.btnSecondary}>Zobacz Listę</Link>
      </div>
    </div>
  );
};

export default HomePage;
