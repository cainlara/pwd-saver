# Contract: `CredentialVersionPanel` component (UI contract)

Internal presentational contract — not a network API — documented because
this project has no other interface surface for this feature (per plan
Phase 1 guidance: "UI contracts for applications").

## Props

```ts
interface CredentialVersionPanelProps {
  credential: CredentialEntry;                 // whose history is shown; panel title/context
  versions: CredentialVersion[] | undefined;    // undefined while loading
  isLoading: boolean;
  isError: boolean;
  error: unknown;                               // surfaced via ApiError message when present
  onClose: () => void;
}
```

- The panel is only ever mounted when a credential is selected
  (`selectedCredentialId !== null` in `CredentialListPage`); when no
  credential is selected, the panel is not rendered at all (satisfies
  FR-002 by construction rather than a hidden/`display: none` element).
- `versions` is expected pre-fetched by `useCredentialVersions` in the page
  and passed down — the panel itself does not call `httpClient` or React
  Query hooks directly, preserving Principle I (presentational components
  don't own data fetching).
- `onClose` clears `selectedCredentialId` in the parent; the panel has no
  internal knowledge of how selection is stored.

## Behavioral contract

- **Ordering & current marker**: renders `versions` most-recent-first with
  the first entry labeled "Current" (FR-004, FR-005). Sorting happens
  before this component receives the array (see `research.md`), so the
  panel itself only renders in the order given.
- **Per-version password masking**: internal `revealedVersionId: string |
  null` state; only one version's password can be revealed at a time is
  NOT required — each version has independent masked/revealed state
  (FR-006 scenario 4), implemented as a `Set<string>` of revealed
  `versionId`s (or equivalent per-item boolean), not a single shared flag.
- **Loading state**: while `isLoading`, renders a loading indicator in
  place of the list (FR-011).
- **Error state**: while `isError`, renders an error message derived from
  `error` (matching the `ApiError`-message pattern already used on
  `CredentialListPage`) in place of the list, without unmounting the panel
  chrome (title/close control) (FR-010).
- **Close control**: always rendered regardless of loading/error/success
  state, so the panel can be dismissed from any state (FR-007, SC-003).
- **Layout**: fixed-position, right-docked; does not alter the document
  flow of the credential grid behind it (Edge Cases: rest of page stays
  interactive).
