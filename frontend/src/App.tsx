// src/App.tsx
import { AuthProvider } from "./context/AuthContext";
import { BalanceProvider } from "./components/BalanceBar/BalanceProvider";
import AppRouter from "./routes/AppRouter";

function App() {
  return (
    <AuthProvider>
      <BalanceProvider>
        <AppRouter />
      </BalanceProvider>
    </AuthProvider>
  );
}

export default App;
