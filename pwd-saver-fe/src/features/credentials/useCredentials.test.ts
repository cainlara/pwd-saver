import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createElement, type ReactNode } from 'react';
import { useCredentials } from './useCredentials';
import * as credentialsApi from './api';
import type { CredentialEntry } from './api';

vi.mock('./api');

function createWrapper() {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return function wrapper({ children }: { children: ReactNode }) {
    return createElement(QueryClientProvider, { client }, children);
  };
}

describe('useCredentials', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('starts in a loading state', () => {
    vi.mocked(credentialsApi.listCredentials).mockReturnValue(
      new Promise(() => {}),
    );

    const { result } = renderHook(() => useCredentials(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);
  });

  it('returns the credential list on success', async () => {
    const entries: CredentialEntry[] = [
      {
        id: '1',
        username: 'alice',
        password: 'secret',
        url: null,
        description: null,
        updatedAt: '2026-01-01T00:00:00Z',
      },
    ];
    vi.mocked(credentialsApi.listCredentials).mockResolvedValue(entries);

    const { result } = renderHook(() => useCredentials(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual(entries);
  });

  it('surfaces an error state when the request fails', async () => {
    vi.mocked(credentialsApi.listCredentials).mockRejectedValue(
      new Error('network error'),
    );

    const { result } = renderHook(() => useCredentials(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isError).toBe(true));
  });
});
