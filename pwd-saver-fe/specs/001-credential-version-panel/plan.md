# Implementation Plan: Credential Version History Panel

**Branch**: `001-credential-version-panel` | **Date**: 2026-07-31 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-credential-version-panel/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Add a closable, right-side version-history panel to the Credentials list
page. Selecting a per-credential "History" action fetches that credential's
versions from the existing backend endpoint
(`GET /api/v1/credentials/{credentialId}/versions`) via a new React Query
hook, and displays them most-recent-first with the current version
distinguished, each version's password masked with an independent
per-version reveal toggle. The panel stays hidden until a credential is
selected, has an explicit close control, and swaps content in place when a
different credential is selected while already open — all built with the
project's existing layering (`features/credentials/`, `components/`,
`pages/`) and no new dependencies.

## Technical Context

**Language/Version**: TypeScript 5.8 (strict mode), React 18.3

**Primary Dependencies**: Existing stack only — `@tanstack/react-query` for
fetching/caching version history, `src/api/httpClient.ts` for the HTTP
call. No new dependency is introduced (see Constitution Check, Principle
II).

**Storage**: N/A on the frontend — version data is read-only and owned by
`pwd-saver-be`/Postgres; the frontend holds it only in React Query's
in-memory cache, keyed per credential.

**Testing**: Vitest + React Testing Library (jsdom), co-located with the
units under test, per existing convention (`*.test.ts` beside its source
file).

**Target Platform**: Browser SPA (existing Vite build, served by nginx in
production); no new platform target.

**Project Type**: Web frontend — single existing project (`pwd-saver-fe`),
extending the current `src/features/credentials/`, `src/components/`, and
`src/pages/` structure. Not a new project or a frontend/backend split
(the backend already exists as the sibling `pwd-saver-be` repo).

**Performance Goals**: Version history for a typical credential (dozens of
versions or fewer) displays within 2 seconds under normal network
conditions (SC-004).

**Constraints**: No new runtime dependency (Constitution II); all HTTP
calls go through `httpClient` (Constitution I); response type is narrowed
to only what the UI needs, not blindly mirrored from the backend DTO
(Constitution V); no `any` without inline justification (Constitution III).

**Scale/Scope**: Single-user vault; a credential typically has a handful to
a few dozen versions. Exactly one version-history panel is shown at a time.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|---|---|---|
| I. Strict Layering | PASS | New data/business logic (`listCredentialVersions`, a `useCredentialVersions` hook) lives in `src/features/credentials/`, alongside the existing `api.ts`/`useCredentials.ts`. The panel itself (`CredentialVersionPanel`) is a presentational component in `src/components/`; it receives data/callbacks as props and contains no HTTP calls. `CredentialListPage` owns the "which credential is selected" state and wires the hook to the panel. All HTTP access goes through `httpClient`. |
| II. Simplicity & YAGNI | PASS | No new dependency. The panel is hand-rolled CSS (a fixed-position `<aside>` styled with a new CSS Module + existing `tokens.css`), following the same pattern as `Modal`/`ConfirmDialog`. Password masking/reveal reuses the same interaction pattern already implemented in `CredentialCard`. |
| III. Strict TypeScript | PASS | New `CredentialVersion` type and mapping function are fully typed; no `any` introduced. |
| IV. Test-First Co-location | PASS | New/changed logic ships with co-located tests: `useCredentialVersions.test.ts`, and mapping-function coverage added alongside existing `features/credentials` tests. |
| V. Backend Contract Discipline | PASS (documented) | The backend's `CredentialVersionResponse` (`versionId`, `username`, `password`, `url`, `description`, `createdAt`) is mapped to a frontend `CredentialVersion` type rather than consumed as-is. In this case every backend field happens to be needed by the UI (per FR-004/FR-006), so the mapped type's fields are a 1:1 subset — this is a coincidence of concrete UI need, not unexamined mirroring, and is called out explicitly here per the principle's requirement. No CSRF handling is added. |
| VI. Session Model, Not JWT | PASS | The versions request goes through `httpClient`, which already sends `credentials: 'include'`; no client-side token storage is introduced. |
| VII. Runtime, Not Build-Time, Configuration | N/A | No new configuration value is introduced by this feature. |
| VIII. Spec-Kit Governed Feature Work | PASS | This feature is being planned under `specs/001-credential-version-panel/` before implementation, per this workflow. |

No violations — Complexity Tracking table is not needed.

## Project Structure

### Documentation (this feature)

```text
specs/001-credential-version-panel/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
src/
├── features/
│   └── credentials/
│       ├── api.ts                      # MODIFIED: add CredentialVersion type + listCredentialVersions()
│       ├── api.test.ts                 # NEW: tests for the version-response mapping
│       ├── useCredentials.ts           # unchanged
│       ├── useCredentialVersions.ts    # NEW: React Query hook, keyed per credentialId
│       └── useCredentialVersions.test.ts  # NEW
├── components/
│   ├── CredentialVersionPanel.tsx         # NEW: presentational right-side panel
│   ├── CredentialVersionPanel.module.css  # NEW
│   ├── CredentialCard.tsx                 # MODIFIED: add a "History" action
│   └── CredentialCard.module.css          # MODIFIED: style for the new action
└── pages/
    ├── CredentialListPage.tsx             # MODIFIED: track selected credential id, render panel
    └── CredentialListPage.module.css      # MODIFIED: layout room for the right-side panel
```

**Structure Decision**: This feature extends the existing single-project
frontend layout — no new project, package, or top-level directory is
created. New business/data logic follows Principle I into the `credentials`
feature domain (versions are a sub-resource of a credential); the panel is
a new reusable presentational component; the page wires selection state
between the credential list and the panel, mirroring how it already wires
`editingCredential`/`deletingCredential` state to `CredentialForm`/
`ConfirmDialog`.

## Complexity Tracking

*No entries — Constitution Check reported no violations.*
