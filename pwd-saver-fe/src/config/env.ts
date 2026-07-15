// Single read-site for app configuration. To add a future configuration
// value: name it `VITE_<NAME>`, document it in `.env.example`, and add a
// field here — no changes to vite.config.ts or its validation are needed
// unless the new value must also fail fast when missing/invalid.
//
// `window.__ENV__` is populated at container startup (see docker/entrypoint.sh)
// so a single built image can be pointed at different backends without a
// rebuild; it takes precedence over the build-time `import.meta.env` value
// used by `npm run dev`/`npm run build` outside a container.
export const env = {
  apiBaseUrl: window.__ENV__?.VITE_API_BASE_URL ?? import.meta.env.VITE_API_BASE_URL,
} as const;
