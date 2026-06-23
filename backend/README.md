# ORMA Backend

Kotlin/Ktor backend module for ORMA. This folder is inside the main KMP app project, but it builds as a separate JVM server module and is not packaged into the Android or iOS apps.

## Stack

- Kotlin JVM
- Ktor server
- PostgreSQL
- Flyway migrations
- Firebase Auth token verification
- Cloudinary for business and product images
- GSTINCheck provider with Postgres cache
- Meta/WhatsApp backend integration for per-workspace catalog and order messaging

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
- `GSTINCHECK_API_KEY`: GSTINCheck provider API key
- `GSTINCHECK_BASE_URL`: optional provider base URL, defaults to `https://sheet.gstincheck.co.in/check`
- `META_WEBHOOK_VERIFY_TOKEN`: webhook verification token configured in Meta App dashboard
- `META_APP_ID`: Meta app ID for OAuth / embedded signup
- `META_APP_SECRET`: Meta app secret for OAuth code exchange
- `META_REDIRECT_URI`: backend callback URL, for example `https://orma-backend.onrender.com/integrations/meta/connect/callback`
- `META_TOKEN_ENCRYPTION_SECRET`: backend-only secret used to encrypt per-workspace Meta access tokens
- `META_SYSTEM_USER_ACCESS_TOKEN`: optional pilot token stored only in Render env; use OAuth/embedded signup for per-business production
- `META_GRAPH_API_VERSION`: optional Graph API version, defaults to `v20.0`
- `META_DEFAULT_ORDER_TEMPLATE`: WhatsApp template used for order updates, defaults to `orma_order_update`
- `META_DEFAULT_LANGUAGE_CODE`: WhatsApp template language, defaults to `en_US`
- `META_OAUTH_SCOPES`: optional comma-separated scopes; defaults to business, catalog, and WhatsApp scopes
- `FIREBASE_STORAGE_BUCKET`: optional Firebase Storage bucket if `MEDIA_STORAGE_PROVIDER=firebase`
- `ALLOWED_ORIGINS`: comma-separated CORS origins

## First APIs

```text
GET  /
GET  /health
POST /auth/session
POST /onboarding/business
GET  /onboarding/team-invites/active
POST /onboarding/team-invites/lookup
POST /onboarding/team-invites/join
POST /onboarding/notifications
POST /media/business-logo
POST /media/product-images
GET  /gstin/{gstin}
GET  /integrations/meta/status
POST /integrations/meta/connection
POST /integrations/meta/connect/start
GET  /integrations/meta/connect/callback
POST /integrations/meta/connect/system-user
POST /integrations/meta/catalog/sync
POST /integrations/meta/whatsapp/send-order-update
GET  /webhooks/meta
POST /webhooks/meta
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

`GET /gstin/{gstin}` is protected by Firebase auth. It checks Postgres first and returns the cached lookup when available. If the GSTIN is not cached, it calls GSTINCheck, saves the provider response in `gstin_lookups`, then returns it. The response includes `source: "cache"` or `source: "provider"`.

Meta/WhatsApp integration is workspace-scoped. App clients save non-secret business identifiers through `POST /integrations/meta/connection`; access tokens are stored only in the backend through OAuth callback or the optional Render `META_SYSTEM_USER_ACCESS_TOKEN`. Catalog sync and WhatsApp order updates never expose Meta tokens to Android, iOS, desktop, or web clients.

See `docs/backend-onboarding-handoff.md` for the pilot onboarding contract.

## Tests

```bash
cd /Users/jibincherian/Desktop/ORMA/Orma
./gradlew :backend:test
```
