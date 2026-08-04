<!--
Sync Impact Report
- Version change: (template, unratified) → 1.0.0
- Modified principles: none (initial ratification — no prior filled version existed)
- Added sections:
  - Core Principles I–VIII (Strict Layering; Simplicity & YAGNI; Strict TypeScript;
    Test-First Co-location; Backend Contract Discipline; Session Model, Not JWT;
    Runtime, Not Build-Time, Configuration; Spec-Kit Governed Feature Work)
  - Technology Stack & Cross-Service Contracts
  - Development Workflow & Quality Gates
  - Governance
- Removed sections: none (template placeholders replaced, not removed)
- Templates requiring updates:
  - ✅ .specify/templates/plan-template.md — Constitution Check gate is generated
    dynamically from this file's principles; no static edit needed.
  - ✅ .specify/templates/spec-template.md — no principle-specific mandatory
    sections introduced beyond template defaults; no edit needed.
  - ✅ .specify/templates/tasks-template.md — no new principle-driven task
    categories (e.g. observability) introduced; no edit needed.
  - ⚠ pending — pwd-saver-fe/CLAUDE.md already documents most of these rules in
    prose (layering, Simplicity & YAGNI, httpClient-only rule, contract
    narrowing); recommend a follow-up pass to cross-link CLAUDE.md to this file
    so the two don't drift, per the Governance section below.
- Follow-up TODOs: none blocking; see Governance for the CLAUDE.md sync
  expectation going forward.
-->

# Pwd Saver Frontend Constitution

## Core Principles

### I. Strict Layering
Presentation MUST stay separated from business/data logic. `src/pages/`
(route-level screens) and `src/components/` (reusable presentational pieces)
MUST NOT contain HTTP calls, session logic, or server-state management
directly. All business/data logic lives in `src/features/<domain>/`,
organized by domain (e.g. `auth/`, `credentials/`) — not by route. Each
domain owns its own `api.ts` for HTTP calls and its own hooks for
consumption by pages/components. Every backend HTTP call MUST go through the
single wrapper `src/api/httpClient.ts`; raw `fetch` calls elsewhere are
prohibited.

**Rationale**: This is the seam that keeps the SPA testable and lets the
backend's session/CORS/error-handling behavior be governed in one place
instead of being re-implemented ad hoc per screen.

### II. Simplicity & YAGNI
No UI/design-system library and no form library MAY be introduced by
default. Styling is hand-written CSS Modules plus `src/styles/tokens.css`
(design tokens) and `src/styles/global.css`; forms are built directly with
controlled inputs. Any new dependency MUST be justified explicitly in the
PR/plan: it must replace meaningfully more hand-written code than it adds,
not merely offer convenience.

**Rationale**: This is a small, single-purpose SPA; dependency weight and
upgrade churn cost more here than the code they would save.

### III. Strict TypeScript
`strict`, `noUnusedLocals`, `noUnusedParameters`, `verbatimModuleSyntax`, and
`noFallthroughCasesInSwitch` MUST remain enabled in `tsconfig.app.json`.
`any` MUST NOT be used unless accompanied by an inline comment justifying
why a precise type is not possible.

**Rationale**: The app has no runtime type-checking layer of its own (it
trusts the backend contract at compile time), so the type system is the
primary defense against shape mismatches reaching production.

### IV. Test-First Co-location
Tests MUST live next to the unit under test (e.g. `useAuth.test.ts` beside
`useAuth.ts`), using Vitest + React Testing Library in a jsdom environment,
sharing the setup file `src/test/setup.ts`. New business logic in
`src/features/` or `src/config/` MUST ship with a co-located test file in
the same change.

**Rationale**: Co-location keeps coverage visible at a glance and prevents
test suites from drifting into a separate, easily-neglected directory tree.

### V. Backend Contract Discipline
The backend's OpenAPI contract
(`pwd-saver-be/specs/001-password-manager/contracts/openapi.yaml`) is the
source of truth for request/response shapes, but the frontend MUST
intentionally narrow response DTOs to only the fields the UI needs (e.g.
`toCredentialEntry` in `features/credentials/api.ts`). Widening a narrowed
type is only allowed when a concrete UI need exists — parity with the
backend DTO alone is not sufficient justification. CSRF is disabled
server-side by design; no CSRF token handling MUST be (re)introduced on the
frontend.

**Rationale**: Mirroring the backend DTO 1:1 would leak backend-internal
fields (status, versioning, warnings, timestamps) into UI code paths that
never use them, and re-adding CSRF handling would fight a deliberate,
already-accepted server-side design choice.

### VI. Session Model, Not JWT
Authentication MUST remain server-side session-cookie based
(`credentials: 'include'` on every request, enforced centrally in
`httpClient`). No client-side token storage or persistence (localStorage,
sessionStorage, cookies set by the frontend, etc.) MAY be added.
`SessionContext` MUST remain in-memory only; it is not persisted or
rehydrated from the backend on page load by design — a hard refresh
returning to `anonymous` until the next authenticated call is expected
behavior, not a defect to be fixed incidentally as part of unrelated work.

**Rationale**: The whole security model rests on the backend owning session
state; introducing any client-side persistence of auth state would create a
second, unsynchronized source of truth.

### VII. Runtime, Not Build-Time, Configuration
`VITE_API_BASE_URL`, and any future configuration value added to
`src/config/env.ts`, MUST remain a runtime-read value so the same built
artifact/Docker image can be pointed at a different backend without
rebuilding. Fail-fast validation (`src/config/validateApiBaseUrl.ts`) MUST
reject a missing or invalid value immediately at config-load time (dev,
build, and test) rather than allowing the app to start in a broken or
silently-misconfigured state.

**Rationale**: The deployment model relies on one image being retargetable
across environments purely via environment variables at container start.

### VIII. Spec-Kit Governed Feature Work
Feature work MUST be planned under `specs/<NNN-name>/` (spec.md, plan.md,
data-model.md, contracts/, tasks.md) before implementation begins.
Architectural changes MUST be checked against the active feature's
`plan.md`/`spec.md` first; if no active spec covers the change, one MUST be
created via the spec-kit workflow before proceeding.

**Rationale**: This keeps route/component structure and backend API
assumptions documented and reviewable ahead of code, rather than discovered
after the fact from the diff.

## Technology Stack & Cross-Service Contracts

**Fixed stack**: React 18 + TypeScript (strict mode), Vite, react-router-dom,
TanStack React Query, Vitest + React Testing Library, ESLint + Prettier.
Swapping any of these MUST go through the same dependency-justification bar
as Principle II, since replacing a foundational piece of the stack is a
larger cost than adding one incremental dependency.

**Cross-service contract seams** — changes here MUST be checked against
both `pwd-saver-fe` and the sibling `pwd-saver-be` repo, since a change on
one side can silently break the other:
- **Session cookies, not JWT** (Principle VI) — the backend authenticates
  via Spring Session JDBC; CSRF is disabled server-side by design.
- **CORS** — the backend only allows cross-origin credentialed requests
  from origins listed in `CORS_ALLOWED_ORIGINS` (exact match, no
  wildcards). A new frontend host/port requires a corresponding backend
  config change, or requests are silently rejected by the browser.
- **API base URL** is a runtime value (Principle VII), not a build-time
  one.
- **API shape** — the backend's OpenAPI contract at
  `pwd-saver-be/specs/001-password-manager/contracts/openapi.yaml` is the
  source of truth; see Principle V for how the frontend intentionally
  diverges from it (narrowing, not mirroring).

## Development Workflow & Quality Gates

Every PR/change MUST pass `npm run build` (typecheck + build), `npm run
lint`, and `npm test` before merge. Reviews MUST explicitly verify
compliance with Principles I–III and V–VI, since these are the ones most
likely to be violated by an otherwise well-intentioned "convenience" change
(e.g. a direct `fetch` call, a quick `any` cast, a widened response type, or
client-side auth persistence added to fix a refresh-state annoyance).
Feature-level architectural review happens against the active feature's
`plan.md`/`spec.md` per Principle VIII before implementation, not after.

## Governance

This constitution supersedes any ad hoc conventions currently documented
only in prose in `CLAUDE.md`. Where the two overlap, this file is
authoritative; `CLAUDE.md` MUST be kept in sync with it — amendments to a
principle here MUST be accompanied by a corresponding update to `CLAUDE.md`
in the same change so the two do not drift apart.

**Amendment procedure**: propose the change, update this file, update
`CLAUDE.md` and any affected `.specify/templates/*.md` in the same change,
and record the update in a new Sync Impact Report at the top of this file.

**Versioning policy** (semantic versioning applied to this document):
- **MAJOR** — backward-incompatible principle removal or redefinition.
- **MINOR** — a new principle or materially expanded guidance is added.
- **PATCH** — clarifications, wording, or non-semantic refinements.

**Compliance review**: every PR/review MUST verify compliance with the
Development Workflow & Quality Gates section above. Any deviation from a
Core Principle MUST be called out explicitly in the PR description with its
justification (mirroring the Complexity Tracking gate in
`plan-template.md`) rather than merged silently.

**Version**: 1.0.0 | **Ratified**: 2026-07-31 | **Last Amended**: 2026-07-31
