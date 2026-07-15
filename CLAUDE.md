# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository structure

This top-level directory is **not itself a git repository** — it's a plain
folder containing two independent git repos as siblings, orchestrated
together only via the `docker-compose.yml`/`.env` at this level:

| Service | Directory | Stack | Own CLAUDE.md |
| --- | --- | --- | --- |
| Frontend | `pwd-saver-fe/` | React 18 + TypeScript SPA (Vite), served by nginx in prod | [`pwd-saver-fe/CLAUDE.md`](pwd-saver-fe/CLAUDE.md) |
| Backend | `pwd-saver-be/` | Spring Boot (Java 25, Maven) REST API | [`pwd-saver-be/CLAUDE.md`](pwd-saver-be/CLAUDE.md) |
| Database | — | PostgreSQL 16 (via docker-compose only) | — |

**Always read the relevant sub-repo's `CLAUDE.md` before making changes
there** — this file only covers things at the whole-stack/orchestration
level; each service's own CLAUDE.md is the source of truth for its
architecture, commands, and conventions. When a task touches only one
service, `cd` into it and follow its CLAUDE.md; treat the two service
directories as separate projects for git purposes (separate `git status`,
commits, etc. — there is no top-level git history tying them together).

The frontend is a pure client of the backend's session-cookie-based REST
API — there is no client-side persistent storage; all credential data lives
in Postgres behind the backend.

## Running the full stack (Docker Compose)

```bash
cp .env.example .env                 # then fill in CREDENTIAL_ENCRYPTION_KEY: openssl rand -base64 32
docker compose up -d --build         # db (Postgres 16) -> backend (Flyway migrations run on startup) -> frontend, in dependency order
docker compose ps
docker compose logs -f backend       # or db / frontend
docker compose up -d --build backend # rebuild a single service after code changes
docker compose down                  # stop + remove containers, keep db volume
docker compose down -v               # also delete the Postgres data volume
```

- Frontend: <http://localhost:8081> · Backend: <http://localhost:8080> (health: `/actuator/health`)
- All compose config comes from `.env` at this root (see `.env.example` for every variable). Key ones:
  - `CREDENTIAL_ENCRYPTION_KEY` — base64 256-bit AES key, **required, no default**, used by the backend to encrypt/decrypt vault passwords
  - `VITE_API_BASE_URL` — absolute backend URL the *browser* calls — must be reachable from the host machine, not just the Docker network (it gets baked into the frontend container's runtime `env-config.js`, not the build)
  - `CORS_ALLOWED_ORIGINS` — origin(s) allowed to call the backend cross-origin; defaults to the frontend's own host URL
  - `DB_PORT` / `BACKEND_PORT` / `FRONTEND_PORT` — host-side port mappings

For native (non-Docker) development of a single service, see that service's
own README/CLAUDE.md — e.g. `mvn spring-boot:run` for the backend or
`npm run dev` for the frontend; in that mode you still need `docker compose
up -d db` for Postgres.

## Cross-service contract points

These are the seams where a change in one service can silently break the
other — check both sides when touching them:

- **Session cookies, not JWT.** The backend authenticates via a server-side
  session (Spring Session JDBC) and the frontend calls every endpoint with
  `credentials: 'include'`. CSRF is disabled server-side by design, so there
  is no CSRF token handling on either side.
- **CORS.** The backend only allows cross-origin credentialed requests from
  origins listed in `CORS_ALLOWED_ORIGINS` (exact match, no wildcards). If
  the frontend is served from a new host/port, that origin must be added
  here or auth requests will be silently rejected by the browser.
- **API base URL is a runtime value for the frontend**, not a build-time
  one — the same frontend image can point at different backends by
  restarting the container with a different `VITE_API_BASE_URL`; don't
  assume rebuilding the frontend is needed to repoint it.
- **API shape**: the backend's OpenAPI contract
  (`pwd-saver-be/specs/001-password-manager/contracts/openapi.yaml`) is the
  source of truth for request/response shapes; the frontend intentionally
  narrows some response fields (see its CLAUDE.md, "Backend contract
  quirks to preserve") rather than mirroring the backend DTO 1:1.

## Spec-kit workflow

Both sub-repos use the [spec-kit](https://github.com/github/spec-kit)
workflow: feature work is planned under each repo's own `specs/<NNN-name>/`
(spec.md, plan.md, data-model.md, contracts/, tasks.md) and governed by that
repo's `.specify/memory/constitution.md`. The `speckit-*` skills (specify,
plan, tasks, implement, analyze, clarify, checklist, constitution, converge,
taskstoissues) are available per-repo for driving that workflow — read the
active feature's `plan.md`/`spec.md` before making architectural changes in
either service.
