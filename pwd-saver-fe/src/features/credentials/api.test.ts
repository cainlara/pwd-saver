import { beforeEach, describe, expect, it, vi } from 'vitest';
import { httpClient } from '../../api/httpClient';
import { listCredentialVersions } from './api';

vi.mock('../../api/httpClient');

describe('listCredentialVersions', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('requests the versions endpoint for the given credential', async () => {
    vi.mocked(httpClient.get).mockResolvedValue([]);

    await listCredentialVersions('credential-1');

    expect(httpClient.get).toHaveBeenCalledWith(
      '/credentials/credential-1/versions',
    );
  });

  it('maps the response into CredentialVersion entries', async () => {
    vi.mocked(httpClient.get).mockResolvedValue([
      {
        versionId: 'v1',
        username: 'alice',
        password: 'hunter2',
        url: 'example.com',
        description: 'work account',
        createdAt: '2026-01-01T00:00:00Z',
      },
    ]);

    const result = await listCredentialVersions('credential-1');

    expect(result).toEqual([
      {
        versionId: 'v1',
        username: 'alice',
        password: 'hunter2',
        url: 'example.com',
        description: 'work account',
        createdAt: '2026-01-01T00:00:00Z',
      },
    ]);
  });

  it('preserves null url/description fields', async () => {
    vi.mocked(httpClient.get).mockResolvedValue([
      {
        versionId: 'v1',
        username: 'alice',
        password: 'hunter2',
        url: null,
        description: null,
        createdAt: '2026-01-01T00:00:00Z',
      },
    ]);

    const result = await listCredentialVersions('credential-1');

    expect(result[0].url).toBeNull();
    expect(result[0].description).toBeNull();
  });
});
