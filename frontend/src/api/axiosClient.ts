// src/api/axiosClient.ts
import axios from "axios";

const axiosClient = axios.create({
  baseURL: "http://localhost:8080/api", 
  // Ustaw tu bazowy adres backendu (np. /api) jeśli Spring wystawia taką ścieżkę
});

// Opcjonalnie dodaj interceptor do automatycznego dołączania tokena JWT
axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default axiosClient;
