<!--
Sync Impact Report
- Version change: [TEMPLATE] → 1.0.0 (initial ratification)
- Modified principles: n/a (first fill of template placeholders)
- Added sections:
  - Core Principles I–VI (Pinned Java Toolchain; Contract-First API Design;
    Real-Dependency Testing; Observability by Default; Externalized,
    Secret-Free Configuration; Fixed Build Tooling & Static Analysis Gates)
  - Additional Constraints (auth model, credential secrecy semantics,
    cross-user isolation, containerized deployment)
  - Development Workflow (spec-kit process, mvn verify gate)
  - Governance (amendment procedure, versioning policy)
- Removed sections: none
- Templates requiring updates:
  - ✅ .specify/templates/plan-template.md — generic "Constitution Check" gate
    placeholder already defers to this file; no edit needed
  - ✅ .specify/templates/spec-template.md — no principle-specific references;
    no edit needed
  - ✅ .specify/templates/tasks-template.md — no principle-specific references;
    no edit needed
  - ✅ .claude/skills/speckit-constitution/SKILL.md — generic, no agent-specific
    references to fix
- Follow-up TODOs:
  - TODO(RATIFICATION_DATE): original adoption date is not recorded anywhere
    in repo history (constitution.md was untracked template content); confirm
    with project owner and replace this TODO once known.
-->

# pwdsaver Constitution

## Core Principles

### I. Pinned Java Toolchain
Java 25 MUST be pinned via a Maven toolchain declaration, not resolved from
whatever JDK happens to be ambient on a contributor's or CI machine's `PATH`.
The toolchain plugin enforces the version at build time, so `mvn` commands
fail fast rather than silently compiling against the wrong bytecode target.
Rationale: the project depends on Java 25-specific behavior; an ambient-JDK
mismatch is a class of build failure that must be caught at build time, not
discovered in a running container.

### II. Contract-First API Design
Every externally-facing endpoint MUST be defined in the OpenAPI contract
(`specs/001-password-manager/contracts/openapi.yaml`) before or alongside its
implementation. The contract is the single source of truth for
request/response shapes — including for the sibling frontend repository that
consumes this API — and MUST be updated in the same change as any endpoint
addition or modification. Rationale: the frontend and backend are separate
repositories with no shared type system; the OpenAPI contract is the only
artifact that keeps them from silently drifting apart.

### III. Real-Dependency Testing (NON-NEGOTIABLE)
Unit tests MUST cover business logic in isolation (no Spring context).
Integration tests MUST cover every endpoint and every external dependency
(PostgreSQL) against a real, Testcontainers-managed instance — protocol or
database mocks are never an acceptable substitute for integration coverage.
`mvn verify` (tests + checkstyle) is the required merge gate and MUST pass
before any change is merged. Rationale: mocked-dependency tests have
previously passed while the real integration (JDBC session store, Flyway
migrations, encryption round-trips) failed; only real dependencies catch
that class of bug.

### IV. Observability by Default
Application logs MUST be structured JSON emitted to stdout/stderr, and MUST
NOT contain secrets or PII — this explicitly includes decrypted vault
passwords, encryption keys, and session tokens/identifiers. The health
endpoint MUST distinguish liveness from readiness. Metrics MUST be exposed
via Micrometer in a Prometheus-scrapeable format. Rationale: this is a
credential-storage service; a logging mistake that leaks a decrypted
password is a security incident, not a bug — the constraint is treated as
non-negotiable rather than best-effort.

### V. Externalized, Secret-Free Configuration
All environment-specific configuration (database connection, CORS allowed
origins, session timeout, encryption key) MUST be supplied via environment
variables, never committed to source. `CREDENTIAL_ENCRYPTION_KEY` in
particular MUST be supplied externally, and the application MUST fail fast
at startup if it is missing or does not decode to exactly 32 bytes.
Rationale: a service that encrypts other people's passwords cannot ship
with a default or checked-in key, and a bad key is safer to fail loudly on
at boot than to silently accept and encrypt data unrecoverably.

### VI. Fixed Build Tooling & Static Analysis Gates
Maven is the only supported build tool for this project; Gradle or other
build systems MUST NOT be introduced. Checkstyle MUST run as a Maven phase
and MUST fail the build on violations. SpotBugs is intended to run the same
way as a build-breaking gate, but is currently disabled pending upstream
support for parsing Java 25 class files — this is a documented, temporary
exception recorded in `pom.xml`, and MUST NOT be treated as precedent for
disabling any other quality gate. Rationale: a single, enforced toolchain
keeps CI and local builds identical; the SpotBugs exception is scoped and
time-bound rather than an open door for skipping static analysis elsewhere.

## Additional Constraints

- **Auth model**: Authentication is session-based via Spring Session JDBC,
  not JWT. This is a deliberate architectural choice enabling any backend
  node to serve any session without sticky sessions, and MUST NOT be
  silently replaced by a token-based scheme without a constitution
  amendment.
- **Credential secrecy semantics**: Vault passwords are reversibly encrypted
  (AES-256-GCM) because the user must be able to retrieve them verbatim,
  which is a deliberately different guarantee from the one-way BCrypt
  hashing used for the user's own login password. Any change touching
  credential storage MUST preserve this distinction — vault passwords MUST
  remain recoverable; login passwords MUST remain one-way hashed.
- **Cross-user isolation**: Every credential read or write MUST be scoped by
  the authenticated caller's account id. No endpoint may return or mutate
  another account's data, regardless of how the request is shaped.
- **Containerized deployment**: The service MUST ship as a Docker image
  (`docker build -t pwdsaver:local .`) and be orchestrated via the
  root-level `docker-compose.yml` alongside PostgreSQL 16 and the frontend.
  Flyway migrations MUST run automatically on startup rather than being
  applied as a separate manual step.

## Development Workflow

- Feature work follows the spec-kit workflow under `specs/<NNN-name>/`
  (`spec.md`, `plan.md`, `data-model.md`, `contracts/`, `tasks.md`), governed
  by this constitution. Architectural changes MUST be preceded by reading the
  active feature's `plan.md`/`spec.md`.
- `mvn verify` MUST pass (tests + checkstyle) before merge; this is the
  concrete enforcement mechanism for Principle III and Principle VI above.

## Governance

This constitution supersedes ad-hoc convention for structural decisions —
toolchain choice, testing strategy, configuration handling, and build
tooling. All PRs and reviews MUST verify compliance with the principles
above; any deviation MUST be justified in the PR description or, if
recurring, folded back into this document via an amendment.

**Amendment procedure**: amendments MUST update this file directly, include
a Sync Impact Report as an HTML comment at the top of the file, and bump
`CONSTITUTION_VERSION` per semantic versioning:
- **MAJOR**: backward-incompatible governance or principle removals/redefinitions.
- **MINOR**: new principle or materially expanded guidance.
- **PATCH**: wording clarifications and non-semantic refinements.

`LAST_AMENDED_DATE` MUST be updated to the date of the change whenever this
file is modified.

**Version**: 1.0.0 | **Ratified**: TODO(RATIFICATION_DATE): original adoption date not found in repo history | **Last Amended**: 2026-08-04
