#!/bin/sh
set -eu

if [ -z "${VITE_API_BASE_URL:-}" ]; then
  echo "ERROR: Missing required VITE_API_BASE_URL environment variable. Set it in 'docker run -e VITE_API_BASE_URL=...' or the compose service's environment." >&2
  exit 1
fi

case "$VITE_API_BASE_URL" in
  http://*/*|http://*|https://*/*|https://*) ;;
  *)
    echo "ERROR: Invalid VITE_API_BASE_URL \"$VITE_API_BASE_URL\": must be an absolute URL (e.g. http://backend:9090)." >&2
    exit 1
    ;;
esac

envsubst '${VITE_API_BASE_URL}' \
  < /usr/share/nginx/html/env-config.template.js \
  > /usr/share/nginx/html/env-config.js

exec nginx -g 'daemon off;'
