# Pwd Saver

A React single-page application for managing a personal vault of username/password credentials. Users can register, sign in, and view, add, update, and delete their stored credentials.

This is a pure frontend client — all data is owned and persisted by the [`pwd-saver-be`](../pwd-saver-be) Spring Boot service over its REST API. This repository contains no backend code.

## Prerequisites

- Node.js 20+
- A running instance of `pwd-saver-be` on `http://localhost:8080` (the dev server proxies `/api` requests to it)

## Getting Started

```bash
npm install
npm run dev
```

The app will be available at the URL printed by Vite (typically `http://localhost:5173`).

## Docker

The app can also be built and run as a Docker container — a multi-stage
build compiles the production bundle (`node:22-alpine`) and serves it with
`nginx:alpine`. No local Node.js/npm is required once the image is built.

### Build the image

From the repository root:

```bash
docker build -t pwd-saver-fe .
```


If `npm ci` fails with a registry authentication error (`E401`), your
`package-lock.json` resolves packages through a private npm registry. Pass your local npm
credentials in as a build secret — they're used only during the build and
are never stored in the resulting image:

```bash
docker build --secret id=npmrc,src="$HOME/.npmrc" -t pwd-saver-fe .
```

### Run the container

The container reads the backend URL at **startup**, not at build time, via
the `VITE_API_BASE_URL` environment variable — the same built image can be
pointed at different backends without rebuilding:

```bash
docker run --rm -p 8080:8080 -e VITE_API_BASE_URL=http://localhost:9090 pwd-saver-fe
```

Then open `http://localhost:8080`.

- `VITE_API_BASE_URL` is **required**: if it's missing or isn't a valid
  absolute `http://`/`https://` URL, the container prints a clear error and
  exits immediately instead of serving a broken app.
- To point the same image at a different backend, just restart the
  container with a different value — no rebuild needed:

  ```bash
  docker run --rm -p 8080:8080 -e VITE_API_BASE_URL=http://backend:9090 pwd-saver-fe
  ```

- The image also defines a `HEALTHCHECK` (an HTTP request to `/`), so it
  reports `healthy`/`unhealthy` via `docker ps` / `docker inspect` once
  running.

### Docker reference

| Aspect | Value |
| --- | --- |
| Exposed port | `8080` (container-internal; map to any host port) |
| Required env var | `VITE_API_BASE_URL` — absolute URL of the `pwd-saver-be` instance to call |
| Healthcheck | Local HTTP request to `/`, checked every 30s |

The full stack (db + backend + this frontend) is orchestrated by the
`docker-compose.yml` at the repository root, alongside `pwd-saver-fe` and
`pwd-saver-be` — see `../docker-compose.yml` and `../.env.example`. See
[`docker/README.md`](docker/README.md) for this image's standalone
container interface and
[`specs/004-docker-containerize-fe/`](specs/004-docker-containerize-fe/)
for the full feature spec, design decisions, and validation steps.

## Scripts

| Command           | Description                                  |
| ----------------- | --------------------------------------------- |
| `npm run dev`     | Start the Vite dev server                     |
| `npm run build`   | Type-check and build for production           |
| `npm run preview` | Preview the production build locally          |
| `npm run lint`    | Run ESLint                                    |
| `npm run format`  | Format the codebase with Prettier             |
| `npm test`        | Run the test suite once (Vitest)              |

Run a single test file:

```bash
npx vitest run src/features/auth/useAuth.test.ts
```

## Tech Stack

- React 18 + TypeScript (strict mode)
- Vite (build/dev server)
- react-router-dom (routing)
- TanStack React Query (server-state fetching and caching)
- Vitest + React Testing Library (testing)
- ESLint + Prettier (linting/formatting)

## Project Structure

```
src/
├── app/          # App shell: router, providers (query client, session context)
├── pages/        # Route-level screens
├── components/   # Reusable presentational components
├── features/     # Business/data-fetching logic per domain (auth, credentials)
├── api/          # Shared HTTP client wrapper
└── styles/       # Design tokens and global styles
```

See `specs/001-credential-manager-ui/` for the feature specification, implementation plan, and API contracts, and `CLAUDE.md` for more detailed architecture notes.

## Authentication

The app authenticates against the backend using session cookies (`credentials: 'include'` on every request). CSRF protection is disabled server-side, so no CSRF token handling is required.
