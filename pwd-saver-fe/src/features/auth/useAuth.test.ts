import { beforeEach, describe, expect, it, vi } from 'vitest';
import { act, renderHook } from '@testing-library/react';
import { createElement, type ReactNode } from 'react';
import { SessionProvider } from './SessionContext';
import { useAuth } from './useAuth';
import * as authApi from './api';

vi.mock('./api');

function wrapper({ children }: { children: ReactNode }) {
  return createElement(SessionProvider, null, children);
}

describe('useAuth', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('starts anonymous', () => {
    const { result } = renderHook(() => useAuth(), { wrapper });
    expect(result.current.status).toBe('anonymous');
    expect(result.current.user).toBeNull();
  });

  it('transitions to authenticated after a successful login', async () => {
    vi.mocked(authApi.login).mockResolvedValue({
      id: 'user-1',
      usernameOrEmail: 'alice@example.com',
    });

    const { result } = renderHook(() => useAuth(), { wrapper });

    await act(async () => {
      await result.current.login('alice@example.com', 'secret');
    });

    expect(result.current.status).toBe('authenticated');
    expect(result.current.user).toEqual({
      userId: 'user-1',
      usernameOrEmail: 'alice@example.com',
    });
  });

  it('propagates a login failure without changing session state', async () => {
    vi.mocked(authApi.login).mockRejectedValue(
      new Error('Invalid username or password'),
    );

    const { result } = renderHook(() => useAuth(), { wrapper });

    await act(async () => {
      await expect(
        result.current.login('alice@example.com', 'wrong'),
      ).rejects.toThrow('Invalid username or password');
    });

    expect(result.current.status).toBe('anonymous');
  });

  it('transitions back to anonymous after logout', async () => {
    vi.mocked(authApi.login).mockResolvedValue({
      id: 'user-1',
      usernameOrEmail: 'alice@example.com',
    });
    vi.mocked(authApi.logout).mockResolvedValue(undefined);

    const { result } = renderHook(() => useAuth(), { wrapper });

    await act(async () => {
      await result.current.login('alice@example.com', 'secret');
    });
    await act(async () => {
      await result.current.logout();
    });

    expect(result.current.status).toBe('anonymous');
    expect(result.current.user).toBeNull();
  });

  it('clears the session even if the logout request fails', async () => {
    vi.mocked(authApi.login).mockResolvedValue({
      id: 'user-1',
      usernameOrEmail: 'alice@example.com',
    });
    vi.mocked(authApi.logout).mockRejectedValue(new Error('network error'));

    const { result } = renderHook(() => useAuth(), { wrapper });

    await act(async () => {
      await result.current.login('alice@example.com', 'secret');
    });

    await act(async () => {
      await expect(result.current.logout()).rejects.toThrow('network error');
    });

    expect(result.current.status).toBe('anonymous');
  });
});
