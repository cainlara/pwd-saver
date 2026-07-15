import {
  MutationCache,
  QueryCache,
  QueryClient,
  QueryClientProvider,
} from '@tanstack/react-query';
import { useState, type ReactNode } from 'react';
import { ApiError } from '../api/httpClient';
import { useSession } from '../features/auth/useSession';

export function QueryProvider({ children }: { children: ReactNode }) {
  const { clearSession } = useSession();

  const [client] = useState(() => {
    function handlePossibleSessionExpiry(error: unknown) {
      if (error instanceof ApiError && error.status === 401) {
        clearSession({ expired: true });
      }
    }

    return new QueryClient({
      defaultOptions: {
        queries: { retry: 1, refetchOnWindowFocus: false },
      },
      queryCache: new QueryCache({ onError: handlePossibleSessionExpiry }),
      mutationCache: new MutationCache({ onError: handlePossibleSessionExpiry }),
    });
  });

  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}
