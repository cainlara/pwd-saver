import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createCredential,
  deleteCredential,
  listCredentials,
  updateCredential,
  type CredentialEntry,
  type CredentialInput,
} from './api';

export const credentialsQueryKey = ['credentials'] as const;

export function useCredentials() {
  return useQuery<CredentialEntry[]>({
    queryKey: credentialsQueryKey,
    queryFn: listCredentials,
  });
}

export function useCreateCredential() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: CredentialInput) => createCredential(input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: credentialsQueryKey });
    },
  });
}

export function useUpdateCredential() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, input }: { id: string; input: CredentialInput }) =>
      updateCredential(id, input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: credentialsQueryKey });
    },
  });
}

export function useDeleteCredential() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteCredential(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: credentialsQueryKey });
    },
  });
}
