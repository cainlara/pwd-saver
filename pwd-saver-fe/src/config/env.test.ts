import { describe, expect, it } from 'vitest';
import { env } from './env';

describe('env', () => {
  it('exposes apiBaseUrl read from import.meta.env.VITE_API_BASE_URL', () => {
    expect(env.apiBaseUrl).toBe(import.meta.env.VITE_API_BASE_URL);
    expect(typeof env.apiBaseUrl).toBe('string');
    expect(env.apiBaseUrl.length).toBeGreaterThan(0);
  });
});
