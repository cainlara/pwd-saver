---

description: "Task list for Credential Version History Panel"
---

# Tasks: Credential Version History Panel

**Input**: Design documents from `/specs/001-credential-version-panel/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/, quickstart.md

**Tests**: Constitution Principle IV mandates co-located tests for new business/data logic in `src/features/`; test tasks for that layer are included below and are NOT optional. No test files exist today for `src/components/`/`src/pages/` in this project, so presentational-component tasks are validated via the `quickstart.md` manual scenarios instead, consistent with current project convention.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

Single existing frontend project — all paths are relative to the
`pwd-saver-fe` repository root, under `src/`.

---

## Phase 1: Setup

**Purpose**: Establish a clean baseline before making changes

- [X] T001 Run `npm run build`, `npm run lint`, and `npm test` on the current branch to confirm a clean baseline before starting implementation (no code changes in this task)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared data-fetching layer and selection trigger that every user story depends on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T002 Add `CredentialVersionResponse` (wire shape) and `CredentialVersion` (frontend type) plus `toCredentialVersion` mapping and `listCredentialVersions(credentialId: string)` to `src/features/credentials/api.ts`, per `data-model.md` and `contracts/get-credential-versions.md` (call through `httpClient.get`, never raw `fetch`)
- [X] T003 Add tests for `toCredentialVersion`/`listCredentialVersions` in `src/features/credentials/api.test.ts` (new file; depends on T002)
- [X] T004 Add `useCredentialVersions(credentialId: string | null)` hook in `src/features/credentials/useCredentialVersions.ts` using `@tanstack/react-query`, query key `['credentials', credentialId, 'versions']`, `enabled: credentialId !== null`, returning versions sorted most-recent-first (depends on T002)
- [X] T005 [P] Add tests for `useCredentialVersions` in `src/features/credentials/useCredentialVersions.test.ts` (loading state, success with sorted data, error state) (depends on T004)
- [X] T006 [P] Add a "History" action button to `CredentialCard` in `src/components/CredentialCard.tsx` (new optional `onViewHistory?: (credential: CredentialEntry) => void` prop, rendered alongside the existing Edit/Delete actions, reusing the existing `.actionButton` CSS class — no new CSS needed)
- [X] T007 Add `selectedCredentialId: string | null` state to `CredentialListPage` in `src/pages/CredentialListPage.tsx` and pass `onViewHistory={setSelectedCredentialId}`-style wiring into each `CredentialCard` (depends on T006)

**Checkpoint**: Selecting a credential's "History" action now updates page state and the data-fetching hook is available — user story implementation can begin.

---

## Phase 3: User Story 1 - View a credential's version history (Priority: P1) 🎯 MVP

**Goal**: Selecting a credential's history action opens a right-side panel listing all of that credential's versions, most-recent-first, with the current version marked and each version's password independently maskable/revealable.

**Independent Test**: Select the history action on a credential with multiple saved versions; confirm the panel opens on the right side of the screen, lists versions most-recent-first with save dates, marks the newest as current, and reveals/hides each version's password independently of the others.

### Implementation for User Story 1

- [X] T008 [US1] Create `CredentialVersionPanel` component in `src/components/CredentialVersionPanel.tsx` per `contracts/credential-version-panel-ui.md`: accepts `credential`, `versions`, `isLoading`, `isError`, `error`, `onClose` props; renders a loading indicator while `isLoading`, an error message while `isError`, and otherwise the version list with the first (most recent) entry labeled "Current" and each version showing username, url, description, and formatted `createdAt` (via `Intl.DateTimeFormat`) (depends on T002, T004)
- [X] T009 [US1] Add independent per-version password masking/reveal in `CredentialVersionPanel.tsx`: local state tracking which `versionId`s are currently revealed (e.g. a `Set<string>`), each version rendering its own mask/reveal toggle that only affects its own entry (depends on T008)
- [X] T010 [P] [US1] Create `src/components/CredentialVersionPanel.module.css` styling the panel as a fixed-position, right-docked element using `src/styles/tokens.css` values, with its version list independently scrollable (does not grow the page)
- [X] T011 [US1] Render `CredentialVersionPanel` in `CredentialListPage.tsx` whenever `selectedCredentialId` is non-null, passing the matching credential and the `useCredentialVersions` result into it (depends on T007, T008)
- [X] T012 [P] [US1] Add layout styles in `src/pages/CredentialListPage.module.css` reserving right-side space when the panel is open so it does not overlap the credential grid
- [ ] T013 [US1] Run `quickstart.md` scenarios 1–3 (multi-version credential, single-version credential, independent password reveal) and fix any discrepancies

**Checkpoint**: User Story 1 is fully functional and independently testable/demoable.

---

## Phase 4: User Story 2 - Close the version history panel (Priority: P1)

**Goal**: The panel has an explicit close control that hides it entirely and clears its content, from any state (loaded, loading, or error).

**Independent Test**: Open the panel for any credential, then activate its close control; confirm the panel disappears completely and the credential list returns to its normal full-width layout.

### Implementation for User Story 2

- [X] T014 [US2] Add a close control to `CredentialVersionPanel.tsx` that always renders regardless of loading/error/success state and invokes the `onClose` prop (depends on T008, T009)
- [X] T015 [US2] Wire `onClose` in `CredentialListPage.tsx` to reset `selectedCredentialId` to `null`, unmounting `CredentialVersionPanel` entirely rather than hiding it via CSS (depends on T011)
- [ ] T016 [US2] Run `quickstart.md` scenario 4 (close the panel) and confirm no version content remains visible anywhere on the page afterward

**Checkpoint**: User Stories 1 and 2 both work independently — panel opens with correct content and closes cleanly from any state.

---

## Phase 5: User Story 3 - Switch between credentials while the panel is open (Priority: P2)

**Goal**: Selecting a different credential's history action while the panel is already open replaces its contents with the newly selected credential's history, without requiring the panel to be closed first.

**Independent Test**: Open history for credential A, then select credential B's history action without closing the panel; confirm the panel updates to show credential B's history only.

### Implementation for User Story 3

- [X] T017 [US3] Verify/adjust `useCredentialVersions` in `src/features/credentials/useCredentialVersions.ts` so that changing `credentialId` shows a loading state for the new credential rather than briefly displaying the previous credential's stale version list (i.e. do not opt into `placeholderData`/keep-previous-data semantics) (depends on T004)
- [X] T018 [US3] Reset per-version password reveal state in `CredentialVersionPanel.tsx` whenever the selected credential's id changes (e.g. key the reveal state off `credential.id`, or remount via a `key` prop from the parent) (depends on T009)
- [ ] T019 [US3] Run `quickstart.md` scenario 5 (switch credentials while open) and confirm no stale data or leftover reveal state from the previous credential

**Checkpoint**: All three user stories are independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final verification across all stories

- [X] T020 [P] Run `npm run lint` and resolve any issues across all changed/added files
- [X] T021 [P] Run `npm run build` (typecheck + build) and resolve any type errors
- [X] T022 Run `npm test` and confirm the full suite passes, including the new `api.test.ts`/`useCredentialVersions.test.ts` coverage from T003/T005
- [ ] T023 Run the remaining `quickstart.md` edge-case scenarios end-to-end (6: loading state under throttled network, 7: backend/network error state, 8: credential deleted while its panel is open)
- [X] T024 Review the change against the Constitution Check table in `plan.md` (Principles I–VIII) before requesting review, per the constitution's Development Workflow & Quality Gates section

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion — BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on Foundational completion; builds on the `CredentialVersionPanel` component created in Phase 3 (T008/T009), so in practice starts after Phase 3
- **User Story 3 (Phase 5)**: Depends on Foundational completion; builds on the same component and hook, so in practice starts after Phase 3
- **Polish (Phase 6)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: No dependency on other stories — delivers the MVP (view history)
- **User Story 2 (P1)**: Adds the close control to the component US1 creates; not independently buildable before US1's component exists, but independently *testable* once both are in place (closing doesn't require US1's list-rendering details to be correct)
- **User Story 3 (P2)**: Adds swap/reset behavior on top of the component and hook from US1; independently testable once US1 (and ideally US2) are in place

### Within Each User Story

- Component creation before behavior additions (mask/reveal before close before switch-reset)
- Styling tasks marked [P] can run alongside their story's component-logic tasks
- Each story's manual quickstart validation task runs last, after that story's implementation tasks

### Parallel Opportunities

- T005 and T006 (Phase 2) touch different files and can run in parallel
- T010 and T012 (Phase 3, CSS files) can run in parallel with each other and alongside T008/T009 once the component's prop shape is settled
- T020 and T021 (Phase 6) can run in parallel

---

## Parallel Example: Phase 2 (Foundational)

```bash
# After T002/T004 land, these can run together:
Task: "Add tests for useCredentialVersions in src/features/credentials/useCredentialVersions.test.ts"
Task: "Add a History action button to CredentialCard in src/components/CredentialCard.tsx"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (blocks everything else)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: run `quickstart.md` scenarios 1–3 independently
5. Demo if ready — a user can already view version history, even without a close control

### Incremental Delivery

1. Setup + Foundational → data layer and selection trigger ready
2. Add User Story 1 → validate → the panel opens and lists history correctly (MVP)
3. Add User Story 2 → validate → the panel can be closed cleanly from any state
4. Add User Story 3 → validate → switching credentials while open behaves correctly
5. Polish → lint/build/test/full quickstart pass, constitution compliance reviewed

---

## Notes

- No new dependency is introduced anywhere in this task list, per Constitution Principle II and `research.md`.
- All `httpClient` usage stays inside `src/features/credentials/api.ts` (T002) — no other file should call `fetch` directly, per Constitution Principle I.
- `[P]` tasks touch different files with no completed-task dependency between them.
- Commit after each task or logical group.
- Stop at any checkpoint to validate a story independently before moving on.
