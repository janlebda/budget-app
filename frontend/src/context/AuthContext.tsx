import React, {
  createContext,
  useContext,
  useState,
  useCallback,
  useMemo,
  useEffect,
} from "react";

export interface User {
  id: number;
  email: string;
}

export interface AuthContextType {
  isAuthenticated: boolean;
  login: (token: string) => void;
  logout: () => void;
  user: User | null;
}

// eslint-disable-next-line react-refresh/only-export-components
export const AuthContext = createContext<AuthContextType | undefined>(undefined);

const isTokenExpired = (token: string): boolean => {
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload.exp * 1000 < Date.now();
  } catch {
    return true;
  }
};

function parseJwt(token: string): User | null {
  try {
    const base64Url = token.split(".")[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      window
        .atob(base64)
        .split("")
        .map((c) => `%${("00" + c.charCodeAt(0).toString(16)).slice(-2)}`)
        .join("")
    );
    return JSON.parse(jsonPayload);
  } catch (e) {
    console.error("Nie udało się sparsować tokena", e);
    return null;
  }
}

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(() => {
    const token = localStorage.getItem("accessToken");
    if (!token || isTokenExpired(token)) {
      localStorage.removeItem("accessToken");
      return false;
    }
    return true;
  });

  const [user, setUser] = useState<User | null>(() => {
    const token = localStorage.getItem("accessToken");
    if (token && !isTokenExpired(token)) {
      return parseJwt(token);
    }
    return null;
  });

  const logout = useCallback(() => {
    localStorage.removeItem("accessToken");
    setIsAuthenticated(false);
    setUser(null);
  }, []);

  const login = useCallback((token: string) => {
    const parsedUser = parseJwt(token);
    if (!parsedUser) {
      console.error("Nie udało się sparsować użytkownika z tokena");
      return;
    }
    localStorage.setItem("accessToken", token);
    setIsAuthenticated(true);
    setUser(parsedUser);
  }, []);

  useEffect(() => {
    const interval = setInterval(() => {
      const token = localStorage.getItem("accessToken");
      if (token && isTokenExpired(token)) {
        logout();
      }
    }, 60000);

    return () => clearInterval(interval);
  }, [logout]);

  const value = useMemo(
    () => ({ isAuthenticated, login, logout, user }),
    [isAuthenticated, login, logout, user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within an AuthProvider");
  return context;
};
