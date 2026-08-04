# pwdsaver — Password Manager

A backend microservice for registering, logging in, and managing a private vault of
third-party credentials (username/password pairs with optional URL/description), with
full update history and soft delete. Built with Spring Boot (Java 25, Maven) and
PostgreSQL.

## Quick start

```bash
docker compose -f ../docker-compose.yml up -d db   # starts local PostgreSQL (compose lives at repo root, alongside pwd-saver-fe)
export CREDENTIAL_ENCRYPTION_KEY=<base64-encoded 256-bit key>
mvn spring-boot:run
```

See [quickstart.md](specs/001-password-manager/quickstart.md) for the full set of
environment variables and a curl-based walkthrough of every feature.

## API Documentation

Once the app is running, open `http://localhost:9090/scalar` (adjust the port to
`SERVER_PORT` if you've overridden it) for an interactive, self-hosted API reference
covering every endpoint. It's read-only documentation — it renders the OpenAPI
document generated live from the running app (`/v3/api-docs`), so it always reflects
the current API with no separate document to keep in sync, and it's reachable without
logging in.

## Running the tests

```bash
mvn test
```

Integration tests use Testcontainers to run against a real PostgreSQL instance
(Docker required). If your local Docker setup isn't Docker Desktop's default, see the
"Running the test suite locally" section of
[quickstart.md](specs/001-password-manager/quickstart.md) for the extra flags/env vars
some setups need.

## Building a container image

```bash
docker build -t pwdsaver:local .
```

## Tech stack

Java 25 · Spring Boot (Web, Security, Data JPA, Validation, Actuator) · Spring Session
JDBC · PostgreSQL · Flyway · Lombok · Maven.
