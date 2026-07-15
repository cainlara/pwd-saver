import { createContext, useContext } from 'react';

export type SessionStatus = 'anonymous' | 'authenticated';

export interface SessionUser {
  userId: string;
  usernameOrEmail: string;
}

interface ClearSessionOptions {
  /** Set when the session ended because the backend rejected a request with 401, not a user-initiated sign-out. */
  expired?: boolean;
}

export interface SessionValue {
  status: SessionStatus;
  user: SessionUser | null;
  expired: boolean;
  setSession: (user: SessionUser) => void;
  clearSession: (options?: ClearSessionOptions) => void;
}

export const SessionContext = createContext<SessionValue | undefined>(
  undefined,
);

export function useSession(): SessionValue {
  const context = useContext(SessionContext);
  if (!context) {
    throw new Error('useSession must be used within a SessionProvider');
  }
  return context;
}
