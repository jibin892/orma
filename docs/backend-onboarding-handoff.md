# ORMA Backend Onboarding Handoff

## Current Setup

- Backend module: `backend`
- Runtime: Kotlin JVM + Ktor
- Database: Neon Postgres Free, project `orma-pilot`
- Local server: `http://localhost:8090`
- Android emulator backend URL: `http://10.0.2.2:8090`
- iOS simulator/Desktop/Web backend URL: `http://localhost:8090`
- Local secret env file: `backend/.env` is gitignored and permissioned to owner-only.

## Database

Flyway migrations applied to Neon:

- `V1__init.sql`: base users/bookings/audit tables
- `V2__onboarding.sql`: app user profile fields, business workspaces, workspace members, team invites

The onboarding tables are:

- `app_users`
- `business_workspaces`
- `workspace_members`
- `team_invites`

## API Contract

Public:

```text
GET /health
POST /auth/session
```

Protected with `Authorization: Bearer <Firebase ID token>`:

```text
POST /onboarding/business
GET  /onboarding/team-invites/active
POST /onboarding/team-invites/lookup
POST /onboarding/team-invites/join
POST /onboarding/notifications
```

### `POST /auth/session`

Called immediately after Firebase phone OTP, email/password, or Google sign-in. It verifies the Firebase token, upserts `app_users`, then returns where the app should route:

- `new_account` / `owner`: new user should create owner profile and business
- `business_setup_required` / `business_setup`: owner has a workspace but setup is incomplete
- `team_member_ready` / `team`: team member is linked to a workspace
- `complete` / `complete`: onboarding is complete

### `POST /onboarding/business`

Saves the full `BusinessSetupDraft`, creates or updates the owner workspace, creates owner membership, and generates a pilot team invite code.

### `GET /onboarding/team-invites/active`

Owner-only endpoint that returns the active invite code for the signed-in owner's workspace, creating one if the workspace has no active invite.

### `POST /onboarding/team-invites/join`

Links the signed-in user to a workspace through an active invite code.

### `POST /onboarding/notifications`

Stores notification preference on `app_users`.

## Frontend Integration

Shared KMP files added:

- `shared/src/commonMain/kotlin/org/orma/project_90/backend/OrmaBackendClient.kt`
- `shared/src/commonMain/kotlin/org/orma/project_90/backend/OrmaBackendConfig.kt`
- platform `OrmaBackendConfig.*.kt` actuals

Flow connected in:

- `shared/src/commonMain/kotlin/org/orma/project_90/onboarding/feature/OrmaOnboardingFlow.kt`

The app now:

1. Authenticates with Firebase.
2. Calls ORMA backend `/auth/session`.
3. Routes existing owner/team/new users based on backend response.
4. Saves business setup to Neon.
5. Saves team invite joins to Neon.
6. Saves notification preference to Neon.

## Remaining Blocker

Firebase Admin verification is not configured in `backend/.env` yet:

```text
FIREBASE_PROJECT_ID=
FIREBASE_CREDENTIALS_PATH=
```

Until the Firebase service account JSON is added, protected API calls return `firebase_auth_not_configured`. The database is ready; the next backend setup step is Firebase Admin credentials.
