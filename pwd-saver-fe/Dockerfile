# syntax=docker/dockerfile:1

# --- Build stage -------------------------------------------------------
# Matches Vite 8's engine requirement (^20.19.0 || >=22.12.0) and the
# project's local dev Node version.
FROM node:22-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
# package-lock.json resolves entirely against the public npm registry, so
# no registry credentials are needed here.
RUN npm ci

COPY . .

# Placeholder only: satisfies vite.config.ts's existing build-time
# validateApiBaseUrl() fail-fast check (see specs/002-env-config/). The
# running container never uses this value — the real backend URL is
# supplied at container *startup* and injected via docker/entrypoint.sh
# (see specs/004-docker-containerize-fe/research.md, Decisions 3 and 5).
ARG VITE_API_BASE_URL=http://localhost:9090
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

RUN npm run build

# --- Runtime stage -------------------------------------------------------
FROM nginx:alpine AS runtime

COPY --from=build /app/dist /usr/share/nginx/html
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
COPY docker/env-config.template.js /usr/share/nginx/html/env-config.template.js
COPY docker/entrypoint.sh /entrypoint.sh

RUN chmod +x /entrypoint.sh

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -q -O /dev/null http://127.0.0.1:8080/ || exit 1

ENTRYPOINT ["/entrypoint.sh"]
