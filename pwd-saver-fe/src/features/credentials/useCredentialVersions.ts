import { useQuery } from '@tanstack/react-query';
import { listCredentialVersions, type CredentialVersion } from './api';

function sortMostRecentFirst(
  versions: CredentialVersion[],
): CredentialVersion[] {
  return [...versions].sort(
    (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
  );
}

export function useCredentialVersions(credentialId: string | null) {
  return useQuery<CredentialVersion[]>({
    queryKey: ['credentials', credentialId, 'versions'],
    queryFn: () => listCredentialVersions(credentialId as string),
    enabled: credentialId !== null,
    select: sortMostRecentFirst,
  });
}
