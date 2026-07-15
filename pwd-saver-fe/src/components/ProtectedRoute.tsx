import type { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useSession } from '../features/auth/useSession';

export function ProtectedRoute({ children }: { children: ReactNode }) {
  const { status, expired } = useSession();
  const location = useLocation();

  if (status !== 'authenticated') {
    return (
      <Navigate
        to="/sign-in"
        replace
        state={{
          from: location,
          reason: expired ? 'session-expired' : 'sign-in-required',
        }}
      />
    );
  }

  return <>{children}</>;
}
