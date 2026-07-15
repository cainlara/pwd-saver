# Running Pwd Saver Frontend in Docker

This packages the frontend as a static build served by nginx, in a
container that can later be added as a service to a docker-compose file
alongside the backend and database (not included here — see
`specs/004-docker-containerize-fe/spec.md`).

## Build

From the repository root:

```sh
docker build -t pwd-saver-fe .
```

If `npm ci` fails with an npm registry authentication error, your
`package-lock.json` resolves packages through a private npm registry (this
is normal in some corporate environments). Pass your local npm credentials
in as a build secret — they are used only during the build and are never
stored in the image:

```sh
docker build --secret id=npmrc,src="$HOME/.npmrc" -t pwd-saver-fe .
```

## Run standalone

The container requires `VITE_API_BASE_URL` — the absolute URL of the
`pwd-saver-be` instance it should call. This is read at **container
startup**, not baked into the image, so the same built image can be
pointed at different backends without rebuilding:

```sh
docker run --rm -p 8080:8080 -e VITE_API_BASE_URL=http://localhost:9090 pwd-saver-fe
```

Then open `http://localhost:8080`.

If `VITE_API_BASE_URL` is missing or isn't a valid absolute `http(s)://`
URL, the container prints a clear error and exits immediately rather than
serving a broken app.

## Reconfiguring the backend URL

Because the URL is supplied at startup, pointing the same image at a
different backend (e.g. local vs. a future compose network) is just a
matter of restarting the container with a different value — no rebuild:

```sh
docker run --rm -p 8080:8080 -e VITE_API_BASE_URL=http://backend:9090 pwd-saver-fe
```

## docker-compose usage

The full stack (db + backend + this frontend) is orchestrated by the
`docker-compose.yml` at the repository root (`../../docker-compose.yml`
relative to this file), where this service is defined as:

```yaml
services:
  frontend:
    build: ./pwd-saver-fe
    depends_on:
      backend:
        condition: service_healthy
    environment:
      # Must be reachable from the user's browser, not the docker network —
      # this value ends up in the browser bundle's runtime config.
      VITE_API_BASE_URL: http://localhost:8080
    ports:
      - "8081:8080"
```

The container also defines a `HEALTHCHECK` (a local HTTP request to `/`),
which the compose file's `depends_on: condition: service_healthy` relies
on. See
`specs/004-docker-containerize-fe/contracts/container-interface.md` for the
full interface contract (port, environment variables, health, failure
behavior).
