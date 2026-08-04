# Quickstart: Validating the Credential Version History Panel

## Prerequisites

- `pwd-saver-be` running and reachable (native `mvn spring-boot:run` or via
  the root `docker compose up -d db backend`), with `VITE_API_BASE_URL`
  pointing at it.
- A signed-in test user with at least:
  - One credential that has been edited two or more times (to produce
    multiple versions).
  - One credential that has never been edited since creation (single
    version).

## Setup

```bash
npm install
npm run dev
```

Open the printed dev server URL, sign in, and navigate to the Credentials
list page.

## Validation scenarios

Each scenario below maps to an acceptance scenario in `spec.md`.

1. **Open history for a multi-version credential** (US1, spec scenarios
   1–2)
   - Select the "History" action on the credential with multiple edits.
   - Expect: a panel appears on the right side of the screen; the most
     recent version is labeled "Current"; versions are listed
     most-recent-first with visible save dates.

2. **Open history for a never-edited credential** (US1, scenario 3)
   - Select "History" on the single-version credential.
   - Expect: the panel shows exactly one entry, marked "Current".

3. **Reveal a historical password independently** (US1, scenario 4)
   - With the panel open on a multi-version credential, activate the
     reveal control for one version's password.
   - Expect: only that version's password becomes plain text; other
     versions' passwords remain masked. Toggling it again re-masks it.

4. **Close the panel** (US2, scenarios 1–2)
   - With the panel open, activate its close control.
   - Expect: the panel disappears entirely and the credential grid returns
     to full width; no version content remains visible anywhere on the
     page.

5. **Switch credentials while open** (US3, scenario 1)
   - Open history for credential A, then — without closing — select
     "History" on credential B.
   - Expect: the panel's contents update to credential B's history; no
     stale data from credential A remains, and the panel does not need to
     be closed/reopened.

6. **Loading state** (Edge Cases)
   - Using browser devtools, throttle the network (e.g. "Slow 3G") and
     open history for a credential.
   - Expect: a loading indicator appears in the panel before the version
     list renders.

7. **Error state** (Edge Cases)
   - Stop the backend (or block the request via devtools) and open history
     for a credential.
   - Expect: the panel shows an error message in place of the list; the
     rest of the page (credential grid, add/edit/delete actions) remains
     usable.

8. **Deleted-while-open handling** (Edge Cases)
   - Open history for a credential, then delete that same credential
     (e.g. from a second browser tab signed in as the same user).
   - Expect: the panel either closes or shows a clear "no longer available"
     message rather than continuing to display stale data.

## Automated checks

```bash
npm run lint
npm run build
npm test
```

`npm test` should include the new co-located tests:
`src/features/credentials/useCredentialVersions.test.ts` and the version-
mapping tests added to `src/features/credentials/api.test.ts`.
