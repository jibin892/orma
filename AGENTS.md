# ORMA Agent Guide

This file is the working guide for agents modifying the ORMA project. Keep changes aligned with the product direction, existing Kotlin Multiplatform architecture, and the DarDoc-derived ORMA design system.

## Project Root

- Main repo: `/Users/jibincherian/Desktop/ORMA/Orma`
- DarDoc reference app: `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp`
- Do not edit the DarDoc app unless the user explicitly asks. Use it only as a UI/UX reference.

## Product Direction

ORMA is a business operations app for small and local businesses such as retail shops, restaurants, bakeries, salons, service providers, and inventory-driven businesses.

The app should help owners and staff:

- sign in securely,
- complete workspace/business setup,
- manage orders or bookings,
- manage customers,
- manage products, stock, and suppliers,
- invite team members,
- view real business activity and totals,
- avoid fake/sample business data.

## Architecture

ORMA is a Kotlin Multiplatform / Compose Multiplatform project.

Modules:

- `shared`: shared business logic, models, auth/onboarding flow, Compose UI, design system, API client.
- `androidApp`: Android entry point.
- `webApp`: Kotlin/JS web entry point and distribution.
- `desktopApp`: Compose Desktop entry point.
- `backend`: Ktor JVM backend.
- `iosApp`: Xcode/iOS host project may exist outside Gradle includes; shared iOS targets are compiled from `shared`.

Important shared areas:

- `shared/src/commonMain/kotlin/org/orma/project_90/designsystem`
- `shared/src/commonMain/kotlin/org/orma/project_90/onboarding`
- `shared/src/commonMain/kotlin/org/orma/project_90/auth`
- `shared/src/commonMain/kotlin/org/orma/project_90/backend`

## UI/UX Rules

ORMA must follow the supplied DarDoc design system visually while using ORMA naming and business copy.

Core tokens:

- screen background: `#FCFDFE`
- primary/accent: `#4F46E5`
- field/cell background: `#F4F7FB`
- font: Google Sans only
- hierarchy comes from size, spacing, and opacity more than heavy weights
- section headers are uppercase, compact, low-opacity indigo
- CTAs use rounded 14dp radius; capsules use full pill radius
- cards/larger panels use 16dp, 20dp, or 24dp radius only when framing is necessary

Design behavior:

- No fake rows, fake totals, or sample business data.
- Empty states must be useful and action-oriented.
- Avoid clutter and technical messages.
- Do not show raw backend/auth wording to users.
- Prefer skeleton or pulse loading states over spinners.
- Use icons only where they clarify navigation or action.
- Bottom navigation is mobile-only.
- Web/desktop must not stretch mobile screens into wide layouts.

Mobile:

- Native app feeling: one-column flows, bottom sheets, clean lists, sticky bottom actions, safe-area padding.
- Use DarDoc-style capsule bottom bar for Dashboard, Orders, Customers, Products, Account.
- Logout/session actions live in Account, not the primary dashboard header.
- Refresh should feel native, such as pull/swipe refresh, not permanent top buttons.

Web/Desktop:

- Use operational layouts: persistent left navigation, page headers, tables/lists, detail panels, and focused forms.
- Auth and OTP should use centered editorial forms, not phone frames.
- Onboarding should use wider layouts with progress/context separated from the active form.
- Dashboard should act as a command center with real KPIs and real queues only.

## Backend/API Rules

Backend base URLs:

- local: `http://localhost:8090`
- dev: `https://orma-backend.onrender.com`

Protected backend APIs require:

```text
Authorization: Bearer <Firebase ID token>
```

Backend responsibilities:

- resolve workspace from the signed-in Firebase user,
- enforce active workspace membership,
- calculate order totals server-side,
- update stock only through backend-controlled flows,
- write stock movement history for adjustments,
- keep provider/API secrets backend-only.

Do not commit secrets, API keys, Vercel tokens, Firebase service credentials, or provider credentials.

## Implementation Rules

- Search with `rg` or `rg --files` first.
- Prefer existing ORMA patterns and components before adding new abstractions.
- Keep shared business logic in `shared` when possible.
- Use platform-specific UI branches only when mobile and web/desktop presentation must differ.
- Use `apply_patch` for manual edits.
- Do not revert user changes or unrelated dirty worktree changes.
- Keep code comments brief and only where they clarify non-obvious logic.
- Use ASCII unless the file already uses non-ASCII for a clear reason.

## Verification Commands

Run the narrowest useful checks for the change. Common commands:

```bash
./gradlew :shared:compileKotlinJvm
./gradlew :androidApp:compileDebugKotlin
./gradlew :shared:compileKotlinIosSimulatorArm64
./gradlew :webApp:jsBrowserDistribution
./gradlew :desktopApp:compileKotlin
./gradlew :backend:test
git diff --check
```

For desktop runtime checks:

```bash
./gradlew :desktopApp:run
```

For backend local checks:

```bash
./gradlew :backend:run
```

## Deployment Notes

- Backend deploys from GitHub `main` to Render service `orma-backend`.
- Web deploy target used by the project: `https://orma-web-dist-dev-api.vercel.app/`.
- Deploy only when the user asks.
- After backend deploy, verify `/health` before assuming app APIs are ready.

## Current Product Priorities

1. Auth, OTP, Google sign-in, session restore, and onboarding routing must remain stable across Android, iOS, Web, and Desktop.
2. Completed auth plus completed onboarding must open Dashboard after refresh/reopen.
3. Dashboard mobile experience is first-class and should follow DarDoc mobile patterns.
4. Web/desktop must use their own operational layouts while staying on the same token system.
5. Team invite should identify invited users during signup and collect the team profile before opening the workspace.
6. Business setup should support industry selection, country/state pickers, currency defaults, logo preview, notification permission, and GST/VAT lookup UI.
