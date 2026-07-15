import { useMemo, useState, type ReactNode } from 'react';
import { SessionContext, type SessionUser, type SessionValue } from './useSession';

export function SessionProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<SessionUser | null>(null);
  const [expired, setExpired] = useState(false);

  const value = useMemo<SessionValue>(
    () => ({
      status: user ? 'authenticated' : 'anonymous',
      user,
      expired,
      setSession: (nextUser: SessionUser) => {
        setUser(nextUser);
        setExpired(false);
      },
      clearSession: (options) => {
        setUser(null);
        setExpired(Boolean(options?.expired));
      },
    }),
    [user, expired],
  );

  return (
    <SessionContext.Provider value={value}>{children}</SessionContext.Provider>
  );
}
