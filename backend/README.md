# ORMA Backend

Kotlin/Ktor backend module for ORMA. This folder is inside the main KMP app project, but it builds as a separate JVM server module and is not packaged into the Android or iOS apps.

## Stack

- Kotlin JVM
- Ktor server
- PostgreSQL
- Flyway migrations
- Firebase Auth token verification
- Cloudinary for business and product images

## Run Locally

From the main app project root:

```bash
cd /Users/jibincherian/Desktop/ORMA/Orma
[ -f backend/.env ] || cp backend/.env.example backend/.env
backend/run-local.sh
```

The local server starts on `http://localhost:8090` by default.

## Docker

```bash
cd /Users/jibincherian/Desktop/ORMA/Orma
docker build -f backend/Dockerfile -t orma-backend .
```

## Environment

Copy `.env.example` into your deployment provider or shell environment. For local development, `backend/.env` is ignored by git and loaded by `backend/run-local.sh`.

Important variables:

- `PORT`: server port
- `DATABASE_URL`: PostgreSQL JDBC URL
- `DATABASE_USER`: PostgreSQL username
- `DATABASE_PASSWORD`: PostgreSQL password
- `RUN_MIGRATIONS`: set to `true` to run Flyway migrations on startup
- `FIREBASE_PROJECT_ID`: Firebase project ID
- `FIREBASE_CREDENTIALS_PATH`: service account JSON path for local/server auth verification
- `MEDIA_STORAGE_PROVIDER`: `cloudinary` for pilot uploads, or `firebase` if Firebase Storage is enabled
- `CLOUDINARY_CLOUD_NAME`: Cloudinary cloud name
- `CLOUDINARY_API_KEY`: Cloudinary API key
- `CLOUDINARY_API_SECRET`: Cloudinary API secret
- `FIREBASE_STORAGE_BUCKET`: optional Firebase Storage bucket if `MEDIA_STORAGE_PROVIDER=firebase`
- `ALLOWED_ORIGINS`: comma-separated CORS origins

## First APIs

```text
GET  /
GET  /health
POST /auth/session
POST /onboarding/business
POST /onboarding/team-invites/lookup
POST /onboarding/team-invites/join
POST /onboarding/notifications
POST /media/business-logo
POST /media/product-images
```

`POST /auth/session` expects:

```json
{
  "idToken": "firebase-id-token"
}
```

Protected onboarding APIs expect:

```text
Authorization: Bearer <firebase-id-token>
Content-Type: application/json
```

Protected media APIs expect:

```text
Authorization: Bearer <firebase-id-token>
Content-Type: multipart/form-data
```

`POST /media/business-logo` accepts one JPEG, PNG, or WebP file up to 5 MB. If business setup is not complete yet, it stores the file under a temporary user path and returns a `storagePath` that can be sent later as `logoFileName`.

`POST /media/product-images` accepts one image file plus a `productId` form field. Product image metadata is stored in Postgres and the file is stored in the configured media provider.

See `docs/backend-onboarding-handoff.md` for the pilot onboarding contract.

## Tests

```bash
cd /Users/jibincherian/Desktop/ORMA/Orma
./gradlew :backend:test
```
