// src/api/authApi.ts
import { AxiosResponse } from "axios";
import axiosClient from './axiosClient';

interface RegisterData {
  username: string;
  email: string;
  password: string;
}

interface LoginData {
  email: string;
  password: string;
}

interface AuthResponse {
  token: string;
  // Dodaj inne pola odpowiedzi, jeśli są
}

export const authApi = {
  register: (data: RegisterData): Promise<AxiosResponse<AuthResponse>> => {
    return axiosClient.post('auth/register', data);
  },
  login: (data: LoginData): Promise<AxiosResponse<AuthResponse>> => {
    return axiosClient.post('auth/login', data);
  },
};
