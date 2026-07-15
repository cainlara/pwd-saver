import { describe, expect, it } from 'vitest';
import { validateApiBaseUrl } from './validateApiBaseUrl';

describe('validateApiBaseUrl', () => {
  it('throws when the value is undefined', () => {
    expect(() => validateApiBaseUrl(undefined)).toThrow(/Missing required VITE_API_BASE_URL/);
  });

  it('throws when the value is an empty string', () => {
    expect(() => validateApiBaseUrl('')).toThrow(/Missing required VITE_API_BASE_URL/);
  });

  it('throws when the value is not a valid absolute URL', () => {
    expect(() => validateApiBaseUrl('not-a-url')).toThrow(/Invalid VITE_API_BASE_URL/);
  });

  it('returns the value unchanged when it is a valid absolute URL', () => {
    expect(validateApiBaseUrl('http://localhost:9090')).toBe('http://localhost:9090');
  });
});
