# Phase 1 Data Model: Credential Version History Panel

## CredentialVersion (new frontend type)

Represents one historical snapshot of a credential, as displayed in the
version-history panel. Sourced from the backend's
`GET /api/v1/credentials/{credentialId}/versions` response (see
`contracts/get-credential-versions.md`), mapped in
`src/features/credentials/api.ts`.

| Field | Type | Notes |
|---|---|---|
| `versionId` | `string` (UUID) | Unique identifier for this version. Used as the React list key and as the per-version reveal-toggle key (FR-006 scenario 4 requires independent masking state per version). |
| `username` | `string` | The credential's username as of this version. |
| `password` | `string` | The credential's password as of this version. Masked by default in the UI; never logged or persisted client-side beyond the query cache (FR-006). |
| `url` | `string \| null` | Optional site URL as of this version. |
| `description` | `string \| null` | Optional free-text note as of this version. |
| `createdAt` | `string` (ISO 8601) | When this version was saved. Used both for display (formatted per `research.md`) and for determining most-recent-first ordering and which entry is "current" (FR-004/FR-005). |

**Relationships**: Many `CredentialVersion` records belong to exactly one
`CredentialEntry` (existing type in `features/credentials/api.ts`), related
by the `credentialId` used in the fetch path — the version records
themselves do not carry a `credentialId` field since they are always
fetched and held scoped to one credential at a time (per spec Assumptions:
only one panel/credential's history is loaded at a time).

**Derived/UI state** (not part of the wire payload):
- *Ordering*: versions are sorted most-recent-first by `createdAt` before
  display (the backend returns them oldest-first).
- *Current version*: the entry with the greatest `createdAt` (i.e., first
  after sorting) is visually distinguished as "Current" (FR-005).
- *Per-version reveal state*: which version's password (if any) is
  currently shown in plain text — local UI state in
  `CredentialVersionPanel`, keyed by `versionId`, independent per version
  (FR-006 scenario 4). Not persisted; resets when the panel's underlying
  credential selection changes.

## Selection state (page-level, not a new domain entity)

`CredentialListPage` gains one new piece of state:

| Field | Type | Notes |
|---|---|---|
| `selectedCredentialId` | `string \| null` | `null` means the panel is hidden (FR-002). Setting it to a credential's `id` opens/updates the panel for that credential (FR-001, FR-008). Setting it back to `null` closes the panel (FR-007) and, per Assumptions, does not affect any stored data. |

No new persisted entity, migration, or backend change is introduced by
this feature — it is read-only against the existing version-history
endpoint (see spec Assumptions).
