# Phase 0 Research: Credential Version History Panel

No `NEEDS CLARIFICATION` markers remain in the Technical Context (the
codebase's existing conventions and the constitution already answer the
standard questions). The decisions below cover the implementation choices
that had more than one reasonable option.

## Data fetching & caching strategy

- **Decision**: Add a `useCredentialVersions(credentialId: string | null)`
  hook in `src/features/credentials/useCredentialVersions.ts`, built on
  `@tanstack/react-query`'s `useQuery`, `enabled: credentialId !== null`,
  keyed as `['credentials', credentialId, 'versions']`.
- **Rationale**: Mirrors the existing `useCredentials` pattern exactly
  (same file, same domain, same library). Keying by `credentialId` gives
  each credential its own cache entry, so switching the selected credential
  (US3) is just changing the query key rather than manual cache
  bookkeeping, and React Query naturally handles the loading/error states
  needed for FR-010/FR-011.
- **Alternatives considered**:
  - Manual `useState` + `useEffect` fetch in the page component — rejected;
    duplicates what React Query already solves and diverges from
    Constitution Principle I (`features/<domain>` owns data-fetching hooks,
    not pages).
  - A single hook that fetches all credentials' versions eagerly — rejected
    as unnecessary network/memory cost; the panel only ever shows one
    credential's history at a time (per spec Assumptions).

## Response shape mapping

- **Decision**: Introduce a `CredentialVersion` frontend type and a
  `toCredentialVersion` mapping function in `features/credentials/api.ts`,
  next to the existing `CredentialEntry`/`toCredentialEntry`, mapping the
  backend's `CredentialVersionResponse` (`versionId`, `username`,
  `password`, `url`, `description`, `createdAt`) field-for-field.
- **Rationale**: Consistent with the existing narrowing pattern (Principle
  V) even though, in this case, the UI needs every field the backend
  returns (FR-004/FR-006 require username, url, description, password, and
  timestamp for every version). Keeping an explicit mapping function (
  rather than typing the fetch call directly as `CredentialVersionResponse`)
  still decouples the frontend type from the backend DTO shape, so a future
  backend-only field addition doesn't silently leak into the frontend type.
- **Alternatives considered**: Reusing `CredentialEntry` for versions —
  rejected; a version has no `id`/status concept, and reusing an entry type
  for a different resource would blur the two concepts in calling code.

## Panel UI implementation

- **Decision**: Build `CredentialVersionPanel` as a plain presentational
  component rendered directly in `CredentialListPage` (not through
  `Modal`'s `createPortal`+backdrop pattern), styled as a fixed-position
  `<aside>` docked to the right edge of the viewport via a new CSS Module,
  using `src/styles/tokens.css` for color/spacing values.
- **Rationale**: `Modal` is built for centered, backdrop-blocking dialogs
  (used for add/edit forms); this panel is explicitly a non-blocking,
  right-docked element that coexists with the rest of the page (FR-010
  requires the rest of the page to stay interactive on error, which rules
  out a blocking backdrop). No new dependency is needed — CSS `position:
  fixed`/`transform` is sufficient (Constitution II).
- **Alternatives considered**: Reusing `Modal` with a right-aligned style
  override — rejected; `Modal`'s backdrop click-to-close and centered
  layout assumptions don't fit a persistent, non-blocking side panel, and
  forcing it to fit would add more special-casing than writing a small
  dedicated component.

## Selection & trigger for opening the panel

- **Decision**: Add a new "History" action button to `CredentialCard`
  (alongside the existing "Edit"/"Delete" actions), calling an
  `onViewHistory?: (credential: CredentialEntry) => void` prop.
  `CredentialListPage` holds `selectedCredentialId: string | null` state,
  passed down to `CredentialCard` instances and to the panel.
- **Rationale**: `CredentialCard` already exposes optional per-action
  callbacks (`onEdit`, `onDelete`) rather than a whole-card click handler,
  and the card's password reveal/copy buttons already occupy click targets
  within the card — adding a third explicit action button is consistent
  with the existing interaction model and avoids ambiguity between
  "clicking the card" and "clicking reveal/copy/edit/delete".
- **Alternatives considered**: Making the whole card clickable to open
  history — rejected; conflicts with existing in-card interactive elements
  and isn't how any other selection/action in this component works today.

## Date/time formatting

- **Decision**: Format each version's `createdAt` using the browser's
  built-in `Intl.DateTimeFormat`, matching how other timestamps are
  presented today (no dedicated date library exists in the project).
- **Rationale**: Consistent with Constitution Principle II — no new
  dependency is justified for formatting a single timestamp field.
- **Alternatives considered**: Adding a date-formatting library (e.g.
  `date-fns`) — rejected; far more capability than one field needs.
