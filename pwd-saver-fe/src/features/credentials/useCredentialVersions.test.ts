import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createElement, type ReactNode } from 'react';
import { useCredentialVersions } from './useCredentialVersions';
import * as credentialsApi from './api';
import type { CredentialVersion } from './api';

vi.mock('./api');

function createWrapper() {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return function wrapper({ children }: { children: ReactNode }) {
    return createElement(QueryClientProvider, { client }, children);
  };
}

describe('useCredentialVersions', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('does not fetch when no credential is selected', () => {
    const { result } = renderHook(() => useCredentialVersions(null), {
      wrapper: createWrapper(),
    });

    expect(credentialsApi.listCredentialVersions).not.toHaveBeenCalled();
    expect(result.current.isPending).toBe(true);
  });

  it('starts in a loading state once a credential is selected', () => {
    vi.mocked(credentialsApi.listCredentialVersions).mockReturnValue(
      new Promise(() => {}),
    );

    const { result } = renderHook(() => useCredentialVersions('cred-1'), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);
  });

  it('returns versions sorted most-recent-first', async () => {
    const versions: CredentialVersion[] = [
      {
        versionId: 'v1',
        username: 'alice',
        password: 'old-pass',
        url: null,
        description: null,
        createdAt: '2026-01-01T00:00:00Z',
      },
      {
        versionId: 'v2',
        username: 'alice',
        password: 'new-pass',
        url: null,
        description: null,
        createdAt: '2026-02-01T00:00:00Z',
      },
    ];
    vi.mocked(credentialsApi.listCredentialVersions).mockResolvedValue(
      versions,
    );

    const { result } = renderHook(() => useCredentialVersions('cred-1'), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data?.map((v) => v.versionId)).toEqual([
      'v2',
      'v1',
    ]);
  });

  it('surfaces an error state when the request fails', async () => {
    vi.mocked(credentialsApi.listCredentialVersions).mockRejectedValue(
      new Error('network error'),
    );

    const { result } = renderHook(() => useCredentialVersions('cred-1'), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isError).toBe(true));
  });
});
