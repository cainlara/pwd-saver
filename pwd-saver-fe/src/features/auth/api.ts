import { httpClient } from '../../api/httpClient';

export interface AuthCredentials {
  usernameOrEmail: string;
  password: string;
}

export interface AuthResponse {
  id: string;
  usernameOrEmail: string;
}

export function register(credentials: AuthCredentials): Promise<AuthResponse> {
  return httpClient.post<AuthResponse>('/auth/register', credentials);
}

export function login(credentials: AuthCredentials): Promise<AuthResponse> {
  return httpClient.post<AuthResponse>('/auth/login', credentials);
}

export function logout(): Promise<void> {
  return httpClient.post<void>('/auth/logout');
}
