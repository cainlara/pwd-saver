# Contract: GET /api/v1/credentials/{credentialId}/versions

**Source of truth**: `pwd-saver-be`'s existing endpoint
(`CredentialController.versionHistory`, backed by
`CredentialService.getVersionHistory`); this feature is a pure consumer and
introduces no backend changes. Full backend contract lives in
`pwd-saver-be/specs/001-password-manager/contracts/openapi.yaml` — this
document only captures what the frontend depends on.

## Request

- **Method**: `GET`
- **Path**: `/api/v1/credentials/{credentialId}/versions`
- **Auth**: Session cookie (`credentials: 'include'`, sent automatically by
  `httpClient`); no request body.
- **Path parameter**: `credentialId` — UUID of the credential whose history
  is being requested; must belong to the signed-in user (enforced
  server-side; see FR-009).

## Response — 200 OK

`CredentialVersionResponse[]`, ordered oldest-first by `createdAt`:

```json
[
  {
    "versionId": "uuid",
    "username": "string",
    "password": "string",
    "url": "string | null",
    "description": "string | null",
    "createdAt": "2026-07-01T12:00:00Z"
  }
]
```

**Frontend handling**: mapped to `CredentialVersion[]` (see
`data-model.md`) via `toCredentialVersion`, then re-sorted most-recent-first
for display (FR-004) — the frontend must not assume the backend's ordering
is display-ready.

## Error responses

- **401 Unauthorized** — no/expired session. Handled by the app-wide
  `QueryCache`/`MutationCache` `onError` (`QueryProvider.tsx`), which clears
  the session and redirects to sign-in; the panel does not need bespoke
  401 handling.
- **404 Not Found** — `credentialId` doesn't exist or doesn't belong to the
  caller. Surfaced by `httpClient` as an `ApiError`; the panel displays
  this as its error state (FR-010, Edge Cases — deleted-while-open case).
- **Network failure** — `httpClient` throws `ApiError` with `status: 0` and
  a generic connectivity message; the panel displays this as its error
  state (FR-010).

## Frontend contract: `listCredentialVersions`

```ts
function listCredentialVersions(credentialId: string): Promise<CredentialVersion[]>
```

- Defined in `src/features/credentials/api.ts`, alongside
  `listCredentials`/`createCredential`/etc.
- Internally calls `httpClient.get<CredentialVersionResponse[]>(...)`,
  never a raw `fetch` (Constitution I).
- Returns versions mapped and left in the backend's original (oldest-first)
  order; sorting for display is the caller's (`useCredentialVersions`'s or
  the panel's) responsibility, kept as a documented, testable step rather
  than an implicit side effect of the mapping function.
