# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Pwd Saver Frontend — a React SPA that is a pure client of the external `pwd-saver-be` Spring Boot service (a sibling repo, not in this codebase). It lets a user register, sign in, and manage a vault of username/password credentials over the backend's session-cookie-based REST API. There is no client-side persistent storage; all credential data lives on the backend.

Feature work is driven by the `spec-kit` workflow under `specs/001-credential-manager-ui/` and `specs/002-env-config/` (spec.md, plan.md, data-model.md, contracts/, tasks.md) and governed by `.specify/memory/constitution.md`. Read `plan.md` and `contracts/*.md` in the active feature folder before making architectural changes — they document the intended route/component structure and the exact backend API shapes.

**Known local-dev risk**: the app now calls `pwd-saver-be` directly at an absolute `VITE_API_BASE_URL` in every mode, including `npm run dev` (no same-origin dev-server proxy). `pwd-saver-be`'s `SecurityConfig` has no CORS configuration today, so this cross-origin, credentialed request may be rejected by the browser until that sibling repo adds matching CORS support (`Access-Control-Allow-Credentials` + an allowed origin, and a compatible cookie `SameSite` setting). See `specs/002-env-config/research.md` for the accepted-risk rationale.

## Commands

- `npm run dev` — start Vite dev server; the app calls the backend directly at `VITE_API_BASE_URL` (default `http://localhost:9090`, see `.env`/`.env.example`) — there is no dev-server proxy
- `npm run build` — typecheck (`tsc -b`) then production build
- `npm run lint` — ESLint over the whole project
- `npm run format` — Prettier write
- `npm test` — run all Vitest tests once
- `npx vitest run src/features/auth/useAuth.test.ts` — run a single test file
- `npx vitest run -t "test name"` — run tests matching a name pattern
- `npx vitest` — watch mode

## Architecture

**Layering**: presentation is strictly separated from data/session logic, enforced by the project constitution (`.specify/memory/constitution.md`):
- `src/pages/` — route-level screens (`LandingPage`, `SignInPage`, `SignUpPage`, `CredentialListPage`)
- `src/components/` — reusable presentational pieces (`CredentialForm`, `CredentialCard`, `Modal`, `ConfirmDialog`, `AuthLayout`, `ProtectedRoute`), each with a co-located `.module.css`
- `src/features/<domain>/` — business/data logic per domain, not per route: `auth/` (api.ts, SessionContext.tsx, useAuth.ts) and `credentials/` (api.ts, useCredentials.ts). Each domain owns its own `api.ts` for HTTP calls and its own hooks for consumption by pages/components.
- `src/api/httpClient.ts` — the single fetch wrapper all domain `api.ts` modules go through. It hardcodes `credentials: 'include'` (session cookie), builds its base URL from `src/config/env.ts` (`${env.apiBaseUrl}/api/v1`), JSON body/response handling, and throws `ApiError` (with `.status`) on non-2xx or network failure. Any new backend call should go through this, not raw `fetch`.
- `src/config/env.ts` — single read-site for app configuration (currently just `apiBaseUrl`, from `VITE_API_BASE_URL`); add future configuration values here, following the same `.env`/`.env.example` convention, without touching `vite.config.ts`. `src/config/validateApiBaseUrl.ts` is the fail-fast validation used by `vite.config.ts` at config-load time (dev/build/test all fail immediately on a missing/invalid value) — see `specs/002-env-config/` for the full design.
- `src/app/` — app shell: `App.tsx` wires `SessionProvider` → `QueryProvider` → `BrowserRouter` → `AppRoutes`; `routes.tsx` defines all routes; `QueryProvider.tsx` builds the React Query client and centrally handles session expiry (a global `onError` on both `QueryCache` and `MutationCache` calls `clearSession({ expired: true })` whenever an `ApiError` with status 401 surfaces from any query/mutation).

**Session/auth model**: `SessionContext` holds only `{ status, user, expired }` in React state — it is not persisted or rehydrated from the backend on load, so a hard refresh returns to `anonymous` until the user hits an endpoint that reconfirms the session. `useAuth` wraps `features/auth/api.ts` calls and pushes results into `SessionContext`. `ProtectedRoute` redirects unauthenticated access to `/sign-in`, passing `state.reason` (`'session-expired'` vs `'sign-in-required'`) so the sign-in page can show the right message.

**Server state**: all credential list/create/update/delete goes through React Query hooks in `features/credentials/useCredentials.ts`, keyed by the shared `credentialsQueryKey`; mutations invalidate that key on success rather than manually patching cache.

**Backend contract quirks to preserve**: the backend's credential response includes extra fields (`status`, `currentVersionId`, `warnings`, timestamps) that the frontend intentionally narrows via `toCredentialEntry` in `features/credentials/api.ts` — don't widen `CredentialEntry` unless a new field is actually needed by the UI. CSRF is disabled server-side, so no CSRF token handling exists or is needed.

## Conventions

- Strict TypeScript (`strict`, `noUnusedLocals`, `noUnusedParameters`, `verbatimModuleSyntax` all on) — avoid `any`; if unavoidable, justify with an inline comment (constitution requirement).
- No UI/design-system dependency and no form library by design (constitution: Simplicity & YAGNI) — styling is hand-written CSS Modules plus `src/styles/tokens.css` (design tokens) and `src/styles/global.css`; forms are built directly with controlled inputs.
- Tests are co-located with the unit under test (e.g. `useAuth.test.ts` next to `useAuth.ts`), using Vitest + React Testing Library, jsdom environment, setup file at `src/test/setup.ts`.
- No dependency should be added unless it replaces meaningfully more hand-written code than it adds — call this out explicitly if proposing a new dependency.
