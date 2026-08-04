# Feature Specification: Credential Version History Panel

**Feature Branch**: `001-credential-version-panel`

**Created**: 2026-07-31

**Status**: Draft

**Input**: User description: "create a closable panel that lists previous versions of a selected credential from the Credentials list page. There is an end point available to retrieve previous versions `/api/v1/credentials/{credentialId}/versions` (from ../pwd-saver-be repository). This panel should be hidden at all times until the user select a Credential from the list. Then the panel will be shown in the rigth side of the screen, listing previous versions of the selected Credential."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View a credential's version history (Priority: P1)

A signed-in user is looking at their list of stored credentials and wants to
see how a particular credential has changed over time. They select the
credential's history action, and a panel opens on the right side of the
screen listing every recorded version of that credential, most recent
first, each showing when it was saved.

**Why this priority**: This is the entire value of the feature — without
it, there is no way to review a credential's change history at all.

**Independent Test**: Can be fully tested by selecting a credential that has
more than one saved version and confirming the panel opens on the right
side of the screen listing each version with its save date, in
most-recent-first order.

**Acceptance Scenarios**:

1. **Given** the Credentials list page with the version panel hidden, **When** the user selects a credential's history action, **Then** the panel appears on the right side of the screen listing that credential's versions.
2. **Given** a credential that has been edited multiple times, **When** its version history is displayed, **Then** the versions appear most-recent-first and the most recent one is clearly marked as the current version.
3. **Given** a credential that has never been edited (only created), **When** its version history is displayed, **Then** the panel shows a single entry representing the current version.
4. **Given** the version panel is open, **When** the user activates the reveal action for a specific version's password, **Then** that version's password is shown in plain text until hidden again, without affecting the masked state of any other version's password.

---

### User Story 2 - Close the version history panel (Priority: P1)

Having reviewed a credential's history, the user wants to dismiss the panel
and return to browsing their credential list without it staying open.

**Why this priority**: The feature was explicitly requested as a
"closable" panel that must stay hidden except when actively in use;
without a reliable way to close it, it would permanently occupy screen
space and violate that requirement.

**Independent Test**: Can be fully tested by opening the panel for any
credential and then using the panel's close control, confirming the panel
disappears and the credential list returns to its normal full-width
layout.

**Acceptance Scenarios**:

1. **Given** the version panel is open, **When** the user activates the panel's close control, **Then** the panel disappears and no credential's history remains visible.
2. **Given** the version panel is closed, **When** the page is viewed, **Then** no version history content is visible anywhere on the page.

---

### User Story 3 - Switch between credentials while the panel is open (Priority: P2)

While already viewing one credential's history, the user selects a
different credential from the list to compare its history instead of
closing the panel first.

**Why this priority**: This is a natural follow-on interaction once the
panel exists, but the feature still delivers its core value (US1 + US2)
without it — a user could simply close and reopen the panel instead.

**Independent Test**: Can be fully tested by opening the panel for one
credential, then selecting a different credential from the list, and
confirming the panel updates to show the newly selected credential's
history instead of the original one.

**Acceptance Scenarios**:

1. **Given** the version panel is open showing credential A's history, **When** the user selects credential B's history action, **Then** the panel updates to show credential B's history without requiring the panel to be closed first.

---

### Edge Cases

- What happens when the selected credential's version history fails to load (e.g. network/server error)? The panel MUST show an error message in place of the list rather than leaving a blank or endlessly loading panel.
- What happens while version history is being retrieved? The panel MUST show a loading indication until the data arrives.
- What happens if the selected credential is deleted (by the same user, e.g. in another tab) while its panel is open? The panel MUST close or show a clear "no longer available" message rather than displaying stale data.
- What happens when a credential has a very long version history? The panel's list MUST remain scrollable within the panel rather than growing to push page content around.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The Credentials list page MUST provide an explicit action, per listed credential, for the user to view that credential's version history.
- **FR-002**: The version history panel MUST be hidden at all times until the user selects a credential's history action; it MUST NOT be visible on initial page load or at any other time by default.
- **FR-003**: When opened, the version history panel MUST be displayed on the right side of the screen.
- **FR-004**: The panel MUST list every recorded version of the selected credential, ordered most-recent-first, showing at minimum the username, URL, description, and the date/time each version was saved.
- **FR-005**: The most recent version in the list MUST be visibly distinguished as the current version.
- **FR-006**: Each version in the panel MUST display its password masked by default, with an explicit per-version reveal action the user can activate to view it in plain text (consistent with how the current credential's password is already handled on the credential card).
- **FR-007**: The panel MUST provide an explicit close control that hides the panel and clears the displayed history.
- **FR-008**: Selecting a different credential's history action while the panel is already open MUST replace the panel's contents with the newly selected credential's history, without requiring the user to close the panel first.
- **FR-009**: The panel MUST only ever display version history for credentials owned by the signed-in user.
- **FR-010**: If version history fails to load, the panel MUST display an error message and MUST NOT block interaction with the rest of the Credentials list page.
- **FR-011**: While version history is loading, the panel MUST display a loading indication before the list of versions appears.

### Key Entities

- **Credential Version**: A historical snapshot of a stored credential captured at the time it was created or last saved. Represents one point in a credential's change history; carries the username, URL, description, and password values as they were at that point in time, plus the timestamp it was saved. Multiple versions belong to exactly one credential.
- **Credential**: An existing entry in the user's vault (username/password pair with optional URL and description). Has a one-to-many relationship with its versions; the most recent version reflects the credential's current, active values.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can go from browsing their credential list to viewing a specific credential's full version history in a single action.
- **SC-002**: The version history panel remains completely hidden until a user explicitly requests it — 100% of page loads and unrelated interactions show no trace of version history content.
- **SC-003**: A user can dismiss the version history panel and return to normal browsing in a single action, from any state the panel is in (loaded, loading, or showing an error).
- **SC-004**: For a credential with a typical amount of history (dozens of versions or fewer), the panel displays the full list within 2 seconds under normal network conditions.

## Assumptions

- The panel is scoped to the Credentials list page only; no other page needs to display version history.
- Only one version-history panel is shown at a time; opening history for a new credential replaces the currently displayed history rather than stacking multiple panels.
- The version list intentionally includes the current (latest) version alongside prior ones, since the underlying history does not separate "current" from "past" as distinct concepts — the panel distinguishes the current one visually instead.
- This feature is read-only/informational: no restore, rollback, or edit action is introduced from within the panel.
- Closing the panel only changes UI visibility; it does not delete, modify, or otherwise affect any stored credential or version data.
- Retrieval of version history relies on the existing, already-available backend capability for fetching a credential's versions; no new backend capability is assumed.
