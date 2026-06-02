import { Link } from "react-router-dom";
import styles from "./Navbar.module.scss";
import { useAuth } from "../../context/AuthContext";

const Navbar = () => {
  const { isAuthenticated, logout } = useAuth();

  return (
    <nav className={styles.navbar}>
      <h1 className={styles.logo}>
        <Link to="/">💰 Budget App</Link>
      </h1>
      <ul className={styles["nav-list"]}>
        <div className={styles["nav-container"]}>
          {isAuthenticated && (
            <>
              <li>
                <Link to="/add-transaction">Dodaj Transakcję</Link>
              </li>
              <li>
                <Link to="/transactions">Lista Transakcji</Link>
              </li>
              <li>
                <Link to="/groups">Grupy</Link>
              </li>
            </>
          )}
        </div>
        <div className={styles["nav-container"]}>
          {!isAuthenticated ? (
            <>
              <li>
                <Link to="/login">Logowanie</Link>
              </li>
              <li>
                <Link to="/register">Rejestracja</Link>
              </li>
            </>
          ) : (
            <li>
              <button onClick={logout} className={styles.logout}>
                Wyloguj
              </button>
            </li>
          )}
        </div>
      </ul>
    </nav>
  );
};

export default Navbar;
