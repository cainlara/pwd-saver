# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

`pwdsaver` is a Spring Boot backend microservice for registering/logging in and
managing a private vault of third-party credentials (username/password pairs with
optional URL/description), with full update history and soft delete. Java 25, Maven,
PostgreSQL. Full feature spec, plan, data model, and API contract live under
`specs/001-password-manager/` — read those before making behavioral changes; they are
the source of truth for requirements (referenced below as FR-xxx).

## Commands

```bash
docker compose -f ../docker-compose.yml up -d db   # local PostgreSQL (compose lives at repo root)
cp .env.example .env                          # fill in real values, especially CREDENTIAL_ENCRYPTION_KEY
set -a && source .env && set +a               # load .env into the shell (never committed)
mvn spring-boot:run                           # run the app (Flyway migrations run automatically)

mvn test                                      # unit + integration tests (Testcontainers, Docker required)
mvn -Dtest=LoginAttemptServiceTest test        # single unit test class
mvn -Dtest=AuthFlowIT test                     # single integration test class (failsafe)
mvn verify                                     # full build gate: tests + checkstyle (must pass to merge)

docker build -t pwdsaver:local .
```

Integration tests run against a real, Testcontainers-managed PostgreSQL instance (no
mocked DB — see Constitution Principle III below). On non-Docker-Desktop setups
(Rancher Desktop, etc.) you may need:

```bash
export JAVA_HOME=<path to a JDK 25 install>     # Maven's own JVM, not just the toolchain
export DOCKER_HOST=unix:///path/to/docker.sock
export TESTCONTAINERS_RYUK_DISABLED=true        # only if Ryuk can't mount your docker.sock
mvn -DargLine="-Dapi.version=1.41" test         # only if Docker Engine rejects the default client API version
```

See `specs/001-password-manager/quickstart.md` for the full curl-based walkthrough of
every endpoint and `specs/001-password-manager/research.md` ("Java 25 Toolchain")
for why each of the above env vars/flags is needed.

## Constitution (`.specify/memory/constitution.md`)

Non-negotiable project rules that override ad-hoc convention — check this file before
making structural decisions:

- Java 25 is pinned via a Maven toolchain declaration (not the ambient JDK).
- Every externally-facing endpoint must be defined by the OpenAPI contract
  (`specs/001-password-manager/contracts/openapi.yaml`) before/alongside implementation.
- Unit tests cover business logic; integration tests cover every endpoint and every
  external dependency using real/containerized instances, never protocol mocks.
- Structured JSON logs to stdout/stderr, no secrets/PII in logs; health endpoint with
  liveness/readiness distinction; metrics scrapeable via Micrometer/Prometheus.
- All env-specific config via env vars; no secrets committed to source.
- Build tool is fixed to Maven — no Gradle. Checkstyle/SpotBugs run as Maven phases and
  must fail the build on violations (SpotBugs execution is currently disabled — see the
  comment in `pom.xml` — because it can't parse Java 25 class files yet; re-enable once
  upstream supports it).

## Architecture

Package-by-feature under `io.github.cainlara.pwdsaver`:

- `account/` — registration, login/logout, lockout tracking (`AuthController`,
  `LoginAttemptService`, `UserAccount`, `UserAccountRepository`).
- `credential/` — the credential vault (`CredentialController`, `CredentialService`,
  `CredentialEncryptionService`, `CredentialEntry`, `CredentialVersion` + repositories).
- `security/` — Spring Security wiring (`SecurityConfig`), `AccountUserDetailsService`,
  `JsonAuthenticationEntryPoint` for uniform JSON 401s.
- `common/` — shared exceptions (`NotFoundException`, `ConflictException`,
  `ValidationException`) and `ApiExceptionHandler` (`@RestControllerAdvice`) that maps
  them to `ErrorResponse` JSON.

### Auth model

Session-based, not JWT: login authenticates via Spring Security's
`AuthenticationManager`/`DaoAuthenticationProvider`, then the controller manually
builds a `SecurityContext` and persists it via `SecurityContextRepository`
(`HttpSessionSecurityContextRepository`) — this is done in `AuthController` itself
(not the default form-login filter) so `LoginAttemptService` lockout bookkeeping can
be interleaved with authentication. Sessions are persisted server-side via Spring
Session JDBC (`spring.session.store-type: jdbc`), so any node can serve any session.
Controllers resolve the caller's account by re-looking-up `Authentication.getName()`
against `UserAccountRepository` (see `currentUserAccountId` in
`CredentialController`) rather than trusting a cached ID — there is no custom
`UserDetails` subclass carrying the account UUID.

Login lockout: 3 consecutive failures locks the account (`UserAccount.locked`);
unknown identifier, disabled, and locked accounts all return the *same* 401 response
(no information leak about which case applies) — see `data-model.md`'s validation
rules for `UserAccount`.

### CORS

Cross-origin browser access is configured in `SecurityConfig` alongside session
wiring: a `CorsConfigurationSource` bean reads `cors.allowed-origins`
(`CORS_ALLOWED_ORIGINS` env var, comma-separated, exact-match origins only, no
wildcards) and is wired into the `SecurityFilterChain` via `http.cors(...)`. An
empty/unset value denies all cross-origin access by default; the allow-list
applies uniformly to every endpoint, including `/actuator/health`. See
`specs/003-cors-support/` for the full spec, decisions, and validation guide.

### Credential versioning model

`CredentialEntry` is an immutable-identity/ownership row; all mutable data
(username, encrypted password, url, description) lives in append-only
`CredentialVersion` rows. There is no "update" of a version — "update" always means
"insert a new `CredentialVersion` row"; the current version is whichever row has the
greatest `created_at` for that entry (see `findFirstByCredentialEntryIdOrderByCreatedAtDesc`
in `CredentialService`). Deleting a `CredentialEntry` only flips its `status` to
`deleted` (soft delete) — it never removes rows, and once deleted it can no longer be
updated (`update()`/`delete()` check ownership + status via
`findByIdAndUserAccountId`, and every credential lookup is scoped by
`userAccountId` to enforce cross-user isolation, FR-009/FR-009a).

Passwords in the vault are AES-256-GCM encrypted (`CredentialEncryptionService`), not
hashed, because they must be recoverable verbatim for the user — unlike the login
password (`UserAccount.password_hash`), which is one-way BCrypt. The encryption key
comes from `CREDENTIAL_ENCRYPTION_KEY` (base64, must decode to exactly 32 bytes); the
service fails fast at startup if it's missing or the wrong length.

Duplicate detection (`CredentialService.create`) is a soft warning, not a rejection:
creating a credential whose username+password matches an existing *active* entry's
*current* version still returns `201`, with a `warnings` array in the response
(FR-004a) rather than a `409`.

### Test structure

- `src/test/java/.../account`, `.../credential` — plain unit tests (e.g.
  `LoginAttemptServiceTest`, `CredentialVersionSelectionTest`), no Spring context.
- `src/test/java/.../integration/*IT.java` — `@SpringBootTest` + real HTTP calls via
  `TestRestTemplate`, one class per acceptance scenario, run by the failsafe plugin
  (`mvn test`/`mvn verify`, not plain unit test runners).
- `IntegrationTestBase` starts a single `PostgreSQLContainer` in a static initializer
  and never stops it for the life of the test JVM — this is deliberate: a per-class
  `@Testcontainers` container gets restarted per class, but Spring's `ApplicationContext`
  cache still reuses one cached context across "identical" test classes, which would
  otherwise hold a stale JDBC URL pointing at an already-stopped container. All
  integration tests extend this base and get `baseUrl(path)` + an autowired
  `TestRestTemplate` for free.
