# Pwd Saver

A password manager: register, sign in, and keep a private vault of
third-party credentials (username/password pairs with optional URL and
description), with full update history and soft delete.

The project is split into two sibling services, orchestrated together with
Docker Compose in this repository:

| Service | Directory | Stack |
| --- | --- | --- |
| Frontend | [`pwd-saver-fe/`](pwd-saver-fe/) | React SPA (Vite), served by nginx |
| Backend | [`pwd-saver-be/`](pwd-saver-be/) | Spring Boot (Java 25, Maven) REST API |
| Database | — | PostgreSQL 16 |

The frontend is a pure client of the backend's session-cookie-based REST
API — there is no client-side persistent storage; all credential data lives
in Postgres behind the backend. See each service's own `README.md`/
`CLAUDE.md` for architecture details specific to that codebase.

## Running with Docker

This is the fastest way to get all three pieces (db, backend, frontend)
running together.

### Prerequisites

- Docker and Docker Compose (`docker compose version`)

### Steps

1. Copy the example environment file and fill in a real encryption key:

   ```bash
   cp .env.example .env
   ```

   Generate a value for `CREDENTIAL_ENCRYPTION_KEY` (used by the backend to
   encrypt/decrypt vault passwords) and paste it into `.env`:

   ```bash
   openssl rand -base64 32
   ```

   The other values in `.env.example` have sane local defaults (db
   credentials, host ports); adjust them only if you need something
   different, e.g. because a port is already in use on your machine.

2. Build and start everything:

   ```bash
   docker compose up -d --build
   ```

   This starts, in dependency order, a `db` container (Postgres 16), a
   `backend` container (builds `pwd-saver-be`, runs Flyway migrations on
   startup, waits for `db` to be healthy first), and a `frontend` container
   (builds `pwd-saver-fe`, waits for `backend` to be healthy first).

3. Open the app:

   - Frontend: <http://localhost:8081>
   - Backend API: <http://localhost:8080> (health check at
     `/actuator/health`)

4. Check status / logs:

   ```bash
   docker compose ps
   docker compose logs -f backend   # or db / frontend
   ```

5. Stop everything:

   ```bash
   docker compose down        # stop and remove containers, keep the db volume
   docker compose down -v     # also delete the Postgres data volume
   ```

### Configuration reference

All variables are read from `.env` at the repository root (see
`.env.example` for the full list with descriptions):

| Variable | Purpose |
| --- | --- |
| `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` | Postgres credentials, shared with the backend's datasource config |
| `CREDENTIAL_ENCRYPTION_KEY` | Base64, 256-bit AES key the backend uses to encrypt/decrypt vault passwords — required, no default |
| `SESSION_TIMEOUT` | Backend session idle timeout (default `30m`) |
| `CORS_ALLOWED_ORIGINS` | Origin(s) allowed to make cross-origin requests to the backend — defaults to the frontend's own host URL |
| `VITE_API_BASE_URL` | Absolute backend URL the frontend calls from the *browser* — must be reachable from your machine, not just the Docker network |
| `DB_PORT` / `BACKEND_PORT` / `FRONTEND_PORT` | Host-side port mappings (defaults `5432` / `8080` / `8081`) |

### Rebuilding after code changes

```bash
docker compose up -d --build backend    # or frontend
```

## Running without Docker

Each service can also be run natively for local development — see
[`pwd-saver-be/README.md`](pwd-saver-be/README.md) and
[`pwd-saver-fe/README.md`](pwd-saver-fe/README.md) for the non-Docker
quickstart (Maven/`mvn spring-boot:run` for the backend, `npm run dev` for
the frontend).
