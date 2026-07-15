import { useCallback } from 'react';
import {
  login as loginRequest,
  logout as logoutRequest,
  register as registerRequest,
} from './api';
import {
  useSession,
  type SessionStatus,
  type SessionUser,
} from './useSession';

interface UseAuthResult {
  status: SessionStatus;
  user: SessionUser | null;
  login: (usernameOrEmail: string, password: string) => Promise<void>;
  register: (usernameOrEmail: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

export function useAuth(): UseAuthResult {
  const { status, user, setSession, clearSession } = useSession();

  const login = useCallback(
    async (usernameOrEmail: string, password: string) => {
      const response = await loginRequest({ usernameOrEmail, password });
      setSession({
        userId: response.id,
        usernameOrEmail: response.usernameOrEmail,
      });
    },
    [setSession],
  );

  const register = useCallback(
    async (usernameOrEmail: string, password: string) => {
      await registerRequest({ usernameOrEmail, password });
    },
    [],
  );

  const logout = useCallback(async () => {
    try {
      await logoutRequest();
    } finally {
      clearSession();
    }
  }, [clearSession]);

  return { status, user, login, register, logout };
}
