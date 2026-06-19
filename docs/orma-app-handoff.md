# ORMA App Handoff

## Current Status

- App project: `/Users/jibincherian/Desktop/ORMA/Orma`
- Backend module: `backend`
- Backend service: `https://orma-backend.onrender.com`
- Render service: `orma-backend`
- Render project: `orma-pilot`
- GitHub repo: `https://github.com/jibin892/orma`
- Main branch: `main`

The backend is a separate Ktor server module inside the same KMP project. It is not packaged into the Android, iOS, desktop, or web apps.

## Technologies

- Kotlin Multiplatform app
- Compose Multiplatform UI
- Firebase Auth for phone, Google, and email auth
- Ktor JVM backend
- Neon Postgres for pilot database
- Flyway for database migrations
- Cloudinary for business logo and product image storage
- Render Free web service for backend hosting
- GSTINCheck provider for GSTIN verification

## Backend APIs

Public:

```text
GET /
GET /health
```

Firebase protected:

```text
POST /auth/session
POST /onboarding/business
GET  /onboarding/team-invites/active
POST /onboarding/team-invites/lookup
POST /onboarding/team-invites/join
POST /onboarding/notifications
POST /media/business-logo
POST /media/product-images
GET  /gstin/{gstin}
```

Protected APIs require:

```text
Authorization: Bearer <Firebase ID token>
```

## Onboarding Flow

1. User signs in with phone OTP, Google, or email.
2. App calls `POST /auth/session`.
3. Backend verifies the Firebase ID token.
4. Backend checks whether the user already exists.
5. Backend returns the user route:
   - `new_account`: create business owner setup
   - `business_setup_required`: continue owner setup
   - `team_member_ready`: open team workspace
   - `complete`: open workspace dashboard
6. Business owner setup saves business data in Postgres.
7. Business owners fetch/create the active team invite with `GET /onboarding/team-invites/active`.
8. Team members join by invite code.
9. Logo/product images are uploaded to Cloudinary through backend APIs.

## GSTIN Lookup Flow

Endpoint:

```text
GET /gstin/{gstin}
```

Behavior:

1. Backend normalizes and validates the GSTIN.
2. Backend checks `gstin_lookups` in Postgres first.
3. If found, backend returns the cached result with `source: "cache"`.
4. If not found, backend calls GSTINCheck.
5. Backend saves the full provider response in Postgres.
6. Backend returns the saved response with `source: "provider"`.

Response shape:

```json
{
  "gstin": "32ELNPS1701J1Z1",
  "flag": true,
  "message": "GSTIN found.",
  "data": {},
  "source": "provider",
  "cached": false,
  "cachedAt": "2026-06-19T06:00:00Z"
}
```

Database table:

```text
gstin_lookups
```

Migration:

```text
backend/src/main/resources/db/migration/V4__gstin_lookup_cache.sql
```

## Database

Migrations:

- `V1__init.sql`: base user, booking, audit tables
- `V2__onboarding.sql`: onboarding users, workspaces, members, team invites
- `V3__media_assets.sql`: product image metadata
- `V4__gstin_lookup_cache.sql`: GSTIN lookup cache

Main tables:

- `app_users`
- `business_workspaces`
- `workspace_members`
- `team_invites`
- `product_images`
- `gstin_lookups`

## Required Render Env

Do not commit real secret values. Set these in Render environment variables:

```text
DATABASE_URL
DATABASE_USER
DATABASE_PASSWORD
RUN_MIGRATIONS=true
FIREBASE_PROJECT_ID
FIREBASE_CREDENTIALS_PATH
FIREBASE_STORAGE_BUCKET
MEDIA_STORAGE_PROVIDER=cloudinary
CLOUDINARY_CLOUD_NAME
CLOUDINARY_API_KEY
CLOUDINARY_API_SECRET
GSTINCHECK_API_KEY
GSTINCHECK_BASE_URL=https://sheet.gstincheck.co.in/check
ALLOWED_ORIGINS
```

After `GSTINCHECK_API_KEY` is added in Render and the service is redeployed, `/health` should include:

```json
{
  "gstinCheckConfigured": true
}
```

## Verification

Backend tests:

```bash
cd /Users/jibincherian/Desktop/ORMA/Orma
./gradlew :backend:test
```

Health check:

```text
https://orma-backend.onrender.com/health
```

Expected deployed health fields:

```json
{
  "status": "ok",
  "environment": "pilot",
  "databaseConfigured": true,
  "firebaseAuthConfigured": true,
  "mediaStorageProvider": "cloudinary",
  "mediaStorageConfigured": true,
  "cloudinaryConfigured": true,
  "gstinCheckConfigured": true
}
```

## Notes

- Render Free can cold start after inactivity.
- GSTIN lookup is intentionally backend-only so the provider API key is never exposed to the app.
- The GSTIN frontend call still needs to be wired wherever the tax/GSTIN field should validate in the app flow.
