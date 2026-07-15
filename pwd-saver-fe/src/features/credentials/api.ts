import { httpClient } from '../../api/httpClient';

interface CredentialResponse {
  id: string;
  status: string;
  username: string;
  password: string;
  url: string | null;
  description: string | null;
  currentVersionId: string;
  createdAt: string;
  updatedAt: string;
  warnings: string[];
}

export interface CredentialEntry {
  id: string;
  username: string;
  password: string;
  url: string | null;
  description: string | null;
  updatedAt: string;
}

export interface CredentialInput {
  username: string;
  password: string;
  url?: string;
  description?: string;
}

function toCredentialEntry(response: CredentialResponse): CredentialEntry {
  return {
    id: response.id,
    username: response.username,
    password: response.password,
    url: response.url,
    description: response.description,
    updatedAt: response.updatedAt,
  };
}

export async function listCredentials(): Promise<CredentialEntry[]> {
  const data = await httpClient.get<CredentialResponse[]>('/credentials');
  return data.map(toCredentialEntry);
}

export async function createCredential(
  input: CredentialInput,
): Promise<CredentialEntry> {
  const data = await httpClient.post<CredentialResponse>('/credentials', input);
  return toCredentialEntry(data);
}

export async function updateCredential(
  id: string,
  input: CredentialInput,
): Promise<CredentialEntry> {
  const data = await httpClient.put<CredentialResponse>(
    `/credentials/${id}`,
    input,
  );
  return toCredentialEntry(data);
}

export async function deleteCredential(id: string): Promise<void> {
  await httpClient.delete<void>(`/credentials/${id}`);
}
