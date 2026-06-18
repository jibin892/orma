# DarDoc Android UI/UX Reference for ORMA

This document records how the DarDoc Android app builds its UI/UX so ORMA can recreate the same quality and structure with ORMA naming.

Scope:
- DarDoc source was inspected read-only from `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp`.
- Do not edit, move, or refactor the DarDoc Android project.
- ORMA should remove the current basic onboarding/auth UI and rebuild from shared primitives that mirror this system.

## Source Files Reviewed

Primary design-system files:
- `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp/docs/DESIGN_SYSTEM.md`
- `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp/core/designsystem/src/main/java/com/dardoc/ui/theme/Color.kt`
- `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp/core/designsystem/src/main/java/com/dardoc/ui/theme/Type.kt`
- `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp/core/designsystem/src/main/java/com/dardoc/ui/theme/Tokens.kt`
- `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp/core/designsystem/src/main/java/com/dardoc/ui/theme/Theme.kt`
- `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp/core/designsystem/src/main/java/com/dardoc/ui/theme/Status.kt`
- `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp/core/designsystem/src/main/java/com/dardoc/ui/theme/DarDocHaptics.kt`

Shared UI and navigation files:
- `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp/core/designsystem/src/main/java/com/dardoc/ui/common/DarDocAnimatedBottomSheet.kt`
- `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp/core/designsystem/src/main/java/com/dardoc/ui/common/DarDocAlertDialog.kt`
- `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp/core/ui/src/main/java/com/dardoc/ui/common/DarDocCommon.kt`
- `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp/core/ui/src/main/java/com/dardoc/ui/common/DarDocRootPinnedTopBar.kt`
- `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp/core/ui/src/main/java/com/dardoc/ui/common/GalaxyBottomBar.kt`
- `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp/core/ui/src/main/java/com/dardoc/ui/common/DarDocCheckoutSelection.kt`
- `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp/core/navigation/src/main/java/com/dardoc/ui/common/DarDocNavigation.kt`
- `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp/core/navigation/src/main/java/com/dardoc/ui/common/DarDocNavigationTransition.kt`

Real screen/form examples:
- `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp/feature/auth/src/main/java/com/dardoc/ui/auth/DarDocAuthFlow.kt`
- `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp/core/customer/src/main/java/com/dardoc/ui/common/CustomerCreationForms.kt`
- `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp/core/payment/src/main/java/com/dardoc/ui/common/DarDocPaymentProviderSheet.kt`
- `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp/core/payment/src/main/java/com/dardoc/ui/common/DarDocPromoCodeSheet.kt`
- `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp/core/ui/src/main/java/com/dardoc/ui/home/PillButton.kt`
- `/Users/jibincherian/Desktop/DARDOC APP/AndroidApp/core/ui/src/main/java/com/dardoc/ui/home/DarDocFrostedCard.kt`

## Product Feel

DarDoc is not a generic Material app. It feels editorial, warm, quiet, premium, and text-forward.

Core visual language:
- Warm cream screen background.
- Dark teal for identity, primary text, and primary actions.
- Light tan filled cells instead of outlined fields.
- Google Sans typography.
- Large readable titles and quiet secondary text.
- Minimal chrome, sparse borders, low elevation.
- Skeleton-first loading states.
- Smooth screen transitions and custom sheet motion.

ORMA should keep this feeling but rename the primitives to ORMA, for example `OrmaTheme`, `OrmaColors`, `OrmaSpacing`, `OrmaShapes`, and `OrmaMotion`.

## Architecture Pattern

DarDoc separates the UI system into three layers:

1. Theme and tokens in `core/designsystem`
   - Colors, typography, spacing, radii, shapes, motion, status tones, haptics.

2. Shared UI primitives in `core/ui/src/main/java/com/dardoc/ui/common` and `core/designsystem/src/main/java/com/dardoc/ui/common`
   - Buttons, skeletons, bottom sheets, dialogs, top bars, bottom navigation, list rows, empty states.

3. Feature screens
   - Screens compose the shared primitives and rarely create new visual rules.

ORMA should follow the same structure:

```text
shared/designsystem
  OrmaColors
  OrmaTypography
  OrmaSpacing
  OrmaRadii
  OrmaShapes
  OrmaMotion
  OrmaStatus
  OrmaTheme

shared/ui
  OrmaBackButton
  OrmaPrimaryButton
  OrmaTextField
  OrmaFormCard
  OrmaAnimatedBottomSheet
  OrmaDialog
  OrmaPinnedTopBar
  OrmaBottomBar
  OrmaSkeletonBlock
  OrmaEmptyState

feature/onboarding
  Authentication
  OTP
  Owner onboarding
  Team login
  Business setup
```

## Color System

Canonical DarDoc tokens:

| Purpose | Token | Value |
| --- | --- | --- |
| Screen background | `ScreenBackground` | `#FEF9EF` |
| Accent / primary text | `Accent` | `#173B3D` |
| Cell background | `CellBackground` | `#F7EEE0` |
| Card background | `CardBackground` | `#FFFFFF` |
| Divider | `Accent` 6 percent alpha | `#173B3D` at 0.06 |
| Hairline | `Accent` 12 percent alpha | `#173B3D` at 0.12 |

Text hierarchy:

| Purpose | Color rule |
| --- | --- |
| Primary text | Accent |
| Secondary text | Accent at 40 percent alpha |
| Tertiary labels | Accent at 30 percent alpha |
| Disabled / hint | Accent at 20 percent alpha |
| Faint metadata | Accent at about 15 percent alpha |

Rules:
- Do not introduce random gray colors.
- Do not hardcode brand colors inside feature screens.
- Use token opacity for hierarchy.
- White surfaces are reserved for modals, frosted cards, and special premium cards. The app should not become white/clinical.

## Typography

DarDoc uses Google Sans from design-system font resources:
- `google_sans_regular.ttf`
- `google_sans_medium.ttf`
- `google_sans_semibold.ttf`
- `google_sans_bold.ttf`

Default weight is normal. Size, spacing, opacity, and position create hierarchy. Heavy font weights are rare.

Type scale:

| Style | Size / line height | Usage |
| --- | --- | --- |
| `displayLarge` | 44sp / 48sp | Editorial hero statement |
| `displayMedium` | 34sp / 38sp | Screen titles and major hero text |
| `headlineMedium` | 28sp / 32sp | Section titles |
| `titleLarge` | 26sp / 30sp | Large card title |
| `titleMedium` | 22sp / 26sp | Card and sheet title |
| `titleSmall` | 17sp / 22sp | Row primary label |
| `bodyLarge` | 15sp / 22sp | Body copy |
| `bodyMedium` | 14sp / 20sp | Secondary body |
| `labelLarge` | 15sp / 18sp medium | Buttons |
| `labelMedium` | 13sp / 16sp | Metadata and inline labels |
| `labelSmall` | 11sp / 14sp | Uppercase section labels |

Auth-specific DarDoc styles:
- Title: 34sp / 38sp, normal.
- Subtitle: 15sp / 22sp, normal.
- Field label: 13sp / 16sp, medium.
- Field text: 16sp / 20sp, normal.
- CTA label: 17sp / 22sp, semibold.
- Dialog phone/title: 22sp / 26sp, normal.
- Dialog body: 14sp / 20sp, normal.

ORMA rule:
- Keep Google Sans and the same type scale.
- Do not use negative letter spacing.
- Do not scale text by viewport width.
- Cap mobile font scale similarly to DarDoc where possible. DarDoc caps Compose font scale to 1.15 in `DarDocTheme`.

## Spacing and Layout

DarDoc screen rhythm:
- Full-screen root background is warm cream.
- Main mobile screen horizontal padding is usually 20dp or 24dp.
- Auth screens use 24dp horizontal padding.
- Form sheets use 20dp horizontal padding.
- Section gaps use 24dp, 28dp, 32dp, and 40dp.
- Bottom actions use `navigationBarsPadding()` and usually 16dp bottom padding.
- Screens are edge-to-edge and manually respect status/navigation bars.

Spacing token map:

| Token | Value |
| --- | --- |
| Micro | 4dp |
| Tight | 8dp |
| Small | 10dp |
| Compact | 12dp |
| Medium | 14dp |
| List | 16dp |
| Content | 20dp |
| Hero | 24dp |
| Section | 28dp |
| SectionLarge | 32dp |
| ScreenBottom | 36dp |
| HeroBottom | 40dp |
| Primary CTA horizontal | 28dp |
| Primary CTA vertical | 13dp |
| Light CTA horizontal | 20dp |
| Light CTA vertical | 10dp |

Layout rules:
- Prefer a clean `Column`, `LazyColumn`, or full-width surface layout.
- Avoid nested decorative cards.
- Do not use card-heavy marketing layouts for app workflows.
- Align title, subtitle, forms, and CTA to one clear left edge on mobile.
- Keep bottom CTA fixed or visually anchored when it is the main next step.

## Radii, Surfaces, and Shadows

Radii:

| Radius | Usage |
| --- | --- |
| 6dp | Skeleton blocks |
| 8dp | Media corners |
| 14dp | Action buttons and compact fields |
| 16dp | Small cards, auth text fields, OTP cells |
| 18dp | Form fields and form CTAs |
| 20dp | Standard cells and suggestion lists |
| 24dp | Premium cards, resolved address cards, dialogs |
| 28dp | Large dialogs and bottom sheet top corners |
| 30-32dp | Some feature bottom sheets |
| 999dp | Pills, badges, chips, circular/capsule controls |

Surface behavior:
- Most DarDoc `Surface` components use `tonalElevation = 0.dp` and `shadowElevation = 0.dp`.
- Visual separation comes from background color, whitespace, borders, dividers, and typography.
- Filled tan cells are preferred over outlined white fields.
- Thin dividers are usually Accent at 6-8 percent alpha.
- Dividers are often inset from the leading icon/text, for example 16dp, 20dp, 62dp, or 68dp depending on row content.

Shadow usage:
- Shadows are used sparingly.
- Primary auth CTA has explicit shadow when enabled: 12dp elevation with accent alpha colors.
- Auth dialog card has explicit 18dp black shadow with low alpha.
- Connectivity banner and Galaxy bottom bar use explicit shadows.
- Ordinary cards, forms, lists, and sheets generally have zero shadow.

ORMA rule:
- Do not use default Material raised cards for core flows.
- Use explicit shadows only for important floating UI: CTA, dialog, connectivity/banner, or bottom nav.

## Text Fields and Forms

DarDoc does not rely on default Material `TextField` or `OutlinedTextField` for its core visual style. It uses `BasicTextField` and draws the field itself.

Standard field pattern:
- Label outside the input, above the field.
- Label style: 13sp label medium, tertiary/secondary text.
- Field body: filled tan `CellBackground`.
- Field shape: 16dp to 18dp rounded rectangle.
- Field height: 56dp for auth.
- Horizontal padding: 16dp to 20dp.
- Placeholder is manually drawn behind the `BasicTextField`.
- Placeholder color: secondary or tertiary accent alpha.
- Cursor color: Accent.
- Input text: Accent, 16sp / 20sp for auth, 15sp / 22sp for form rows.
- No underline, no outline, no default Material decoration.

Auth field:
- `BasicTextField`
- height 56dp
- radius 16dp
- background `CellBackground`
- horizontal padding 20dp
- placeholder in `decorationBox`

Customer form field:
- `Surface` radius 18dp
- background `CellBackground`
- inner padding 16dp horizontal and vertical
- optional leading icon at 18dp
- optional trailing text
- label above with 8dp gap

Address detail row field:
- Used inside one grouped card.
- Label on the left, value/input on the right.
- Row padding 16dp horizontal and vertical.
- Input text aligns end.
- Dividers inset 16dp between rows.
- Good pattern for business setup settings such as currency, tax, invoice prefix, VAT/GST number, and address lines.

Dropdown field:
- The closed dropdown is a filled tan row with 18dp radius.
- Expanded options use 22dp radius and animated expand/fade.
- Selected row uses Accent at 7 percent alpha.
- Selected icon is 18dp check.

ORMA rule:
- Build `OrmaTextField`, `OrmaDropdownField`, `OrmaFormCard`, and `OrmaSettingsRowTextField` before rebuilding screens.
- Do not use default `OutlinedTextField` in the onboarding/auth/business setup milestone.

## Buttons and Actions

Primary capsule CTA:
- Shape: capsule.
- Background: Accent.
- Foreground: ScreenBackground or white.
- Text: 15sp label large, medium.
- Padding: 28dp horizontal, 13dp vertical.

Full-width auth/checkout CTA:
- Height: 56dp for auth.
- Shape: 16dp for auth, 14-18dp in sheets/forms.
- Enabled color: Accent.
- Disabled color: Accent at 28-35 percent alpha.
- Text: 16-17sp, medium/semibold depending on screen.
- Bottom placement uses `navigationBarsPadding()` plus 16dp bottom padding.
- Loading can be shown with skeleton shimmer instead of a spinner where appropriate.

Secondary action:
- Filled `CellBackground`.
- Shape: 14dp.
- Height around 52dp.
- Text uses Accent at around 70 percent alpha.

Back/close buttons:
- Default size: 44dp.
- Shape: circle.
- Icon size: 22dp, sometimes 18dp for close in sheet headers.
- Container: CellBackground or translucent white.
- Border: 0.5dp Accent at 8-12 percent alpha.
- Elevation: zero.

Pills/chips:
- Shape: 999dp.
- Selected: Accent background, ScreenBackground text/icon.
- Unselected: Accent at 8 percent alpha, Accent text/icon.
- Compact chip padding can be 10dp horizontal and 7dp vertical.
- Segmented property picker uses 12dp outer padding and 8dp gap.

## Navigation

DarDoc uses Navigation Compose but wraps transitions:
- Forward navigation slides left and fades in.
- Back navigation slides right and fades in.
- Replace transitions fade only.
- Main navigation duration: slide 300ms, fade-in 220ms, fade-out 120ms.
- Step navigation duration: slide 280ms, fade-in 180ms, fade-out 90ms.
- `navigateDarDocSingleTop` prevents duplicate current-route navigation.

Top bars:
- Root pinned top bar supports collapse based on scroll.
- Root list top padding: 112dp.
- Pinned screen content top padding: 88dp.
- Root expanded title: 34sp / 38sp.
- Root collapsed title: 23sp / 27sp.
- Pinned detail title fades in after scroll fraction starts.
- Top bars use `statusBarsPadding()` and `ScreenBackground`.
- Surfaces are transparent or cream, with zero shadow.

Bottom navigation:
- DarDoc has a custom `GalaxyBottomBar`, not Material `NavigationBar`.
- Outer padding: 18dp horizontal, 10dp vertical.
- Shape: 34dp rounded container.
- Container: dark glass color `0xE61B2A3A`.
- Border: white 14 percent alpha.
- Shadow: 18dp black at 18 percent alpha.
- Selected indicator animates offset over 360ms.
- Item shape: 26dp rounded.
- Icon selected scale: 1.06.
- Icon size: 22dp.
- Label: 12sp / 14sp Google Sans.

ORMA rule:
- Mobile should feel like a real mobile app, not a responsive web page.
- Web and desktop can use a wider layout, but mobile auth/onboarding should use mobile-native top bars, bottom CTAs, sheets, and field sizing.

## Bottom Sheets

DarDoc uses a custom animated bottom sheet, not the stock Material sheet.

Base behavior:
- `DarDocAnimatedBottomSheet` owns scrim, height, offset, and dismiss flow.
- Sheet starts hidden below the screen and springs to zero offset.
- Sheet height defaults to 90 percent of window height.
- Hidden bottom padding defaults to 32dp.
- Scrim alpha defaults to 20 percent black.
- Scrim fade: 220ms.
- Sheet spring: damping ratio 0.92, stiffness 650.
- Dismiss waits about 280ms before callback.
- Back press and scrim click can dismiss when enabled.

Modal wrapper:
- Uses `Dialog`.
- `usePlatformDefaultWidth = false`.
- `decorFitsSystemWindows = false`.
- Custom layout handles full-width edge-to-edge.

Sheet content pattern:
- Surface aligns bottom center.
- Offset uses animated sheet offset.
- Width fills max.
- Top corners: 28dp, 30dp, or 32dp depending on feature.
- Background: `ScreenBackground`.
- Elevation: zero.
- Drag handle: 36-46dp wide, 4-5dp high, Accent at 14-16 percent alpha.
- Sheet content uses `navigationBarsPadding()` and `imePadding()` when forms are present.

ORMA rule:
- Build `OrmaAnimatedBottomSheet` once and reuse it for OTP recovery, business address picker, logo upload options, country/currency picker, and tax settings.

## Dialogs and Scrims

Shared dialog:
- Custom `Dialog`, not default platform width.
- Full-screen scrim drawn manually.
- Dialog max width: 420dp.
- Card shape: 28dp large card by default.
- Card background: white.
- Card padding: 24dp.
- Scrim alpha: 22 percent black.
- Blur behind on Android S and later: radius 26.
- Fade: 180ms.
- Card scale animation: 0.96 to 1.0 with spring.

Auth dialog variant:
- White card.
- Shape: 24dp.
- Shadow: 18dp black low alpha.
- Card padding: 24dp horizontal, 28dp vertical.
- Center aligned content.
- Scrim alpha: 36 percent black.
- Background screen is blurred by 4dp while dialog is visible.

ORMA rule:
- Confirmation and error states in auth should use this custom card/scrim behavior.
- Avoid default AlertDialog styling.

## Loading, Empty, and Status States

Skeleton:
- Use shimmer block instead of blank flashes.
- Base color: Accent at 6-8 percent alpha.
- Highlight: Accent at 12-13 percent alpha.
- Radius: 6dp by default, or match the row/card shape.
- Animation duration: 800ms with ease.
- Shimmer offset moves from -280f to 560f.
- Pulse alpha moves 0.72 to 1.0.

Empty state:
- Horizontal padding: 20dp.
- Vertical padding: 40dp.
- Icon circle: 72dp.
- Icon: 32dp.
- Icon container: CellBackground.
- Title: titleMedium, Accent.
- Description: bodyLarge, secondary text.
- Optional CTA: primary capsule.

Status tones:
- Critical, payment, warning, success, complete, neutral.
- Map raw statuses through a central helper.
- Do not scatter status colors inside feature code.

Connectivity banner:
- Animated slide/fade from top.
- z-index high.
- Width max 560dp.
- Shape: large card, 28dp.
- Uses explicit shadow and frosted gradient.
- Icon circle 40dp, icon 20dp.

## Auth Flow Reference

DarDoc auth flow:

```text
Phone entry
-> phone confirmation dialog
-> registration for new user or OTP for existing user
-> OTP verification
-> notification permission if needed
-> tracking consent if needed
-> success
```

Phone entry screen:
- Root `Box` fills screen with `ScreenBackground`.
- Content column uses `statusBarsPadding()`, `imePadding()`, 24dp horizontal padding.
- Top back button at 20dp top.
- Title top gap: 16dp.
- Subtitle top gap: 10dp.
- Phone row top gap: 24dp.
- Country selector: 56dp high, 16dp radius, CellBackground, horizontal padding 16dp.
- Phone field: 56dp high, 16dp radius, CellBackground, horizontal padding 20dp.
- Bottom CTA: fills width, 56dp high, 16dp radius, `navigationBarsPadding()`, 16dp bottom.

Registration:
- Same 24dp mobile screen padding.
- Scrollable form content with bottom padding to avoid CTA.
- Bottom CTA anchored to bottom center.
- Labels sit above fields with 8dp gap.

OTP:
- Hidden `BasicTextField` captures digits.
- Visual OTP boxes are custom.
- 6 digits are split 3 + dash + 3.
- Cell width responds to screen width, clamped 36-52dp.
- Cell height responds to width, clamped 40-56dp.
- Current cell has 1.5dp border, Accent at 30 percent alpha.
- Filled cell shows a 10dp Accent dot.
- Cursor is custom 2dp wide with animated alpha.
- Verification loading uses skeleton plus secondary text.
- Resend countdown uses 14sp dialog body style.

Country picker:
- Custom animated bottom sheet at 92 percent screen height.
- Sheet top corners: 28dp.
- Header: centered title, close button on right.
- Search field: 40dp high, 12dp radius, Accent at 6 percent alpha.
- Country rows use CellBackground and inset dividers.

ORMA first milestone should map:

```text
Authentication
-> OTP verification
-> Business owner onboarding
-> Team member login
-> Business setup
```

Use DarDoc auth layout for mobile. For web/desktop, keep the same tokens and components but use a wider two-pane or centered panel layout appropriate to desktop.

## Business Setup UI Translation

Business setup should reuse the customer/address form patterns.

Recommended ORMA mapping:

| ORMA need | DarDoc pattern to copy |
| --- | --- |
| Business name, email, phone | Auth/customer `BasicTextField` filled tan field |
| GST/VAT number | Customer field with label above and 18dp radius |
| Business address | Address detail grouped card with right-aligned editable rows |
| Country/currency/tax type | Dropdown field with animated option list |
| Business type | Property picker pill segmented row |
| Invoice prefix/starting number | Settings row field inside grouped card |
| Logo upload | Premium card or 20-24dp image cell with action capsule |
| Tax settings | Grouped rows with inset dividers |
| Invoice settings | Grouped rows with right-side values and edit sheets |

Business setup card behavior:
- Use filled `CellBackground` grouped cards, not outlined forms.
- Use 14-20dp radii depending on density.
- Use inset dividers between rows.
- Use section label above grouped cards with tertiary label style.
- Use bottom CTA for "Continue", "Save", or "Finish setup".

## Platform Rules for ORMA

Mobile:
- Must feel like a native mobile app.
- Use 20-24dp horizontal padding.
- Use full-screen cream surfaces.
- Use bottom anchored CTAs.
- Use custom bottom sheets for pickers.
- Use mobile typography scale directly.
- Avoid desktop-style cards, wide tabs, or horizontal overflow.

Web:
- Use the same tokens and fields.
- Prefer centered or split layouts with max content width.
- Keep form cards compact and professional.
- Use desktop-friendly spacing, but do not change the component language.

Desktop:
- Use web/desktop shell patterns.
- Avoid mobile-only huge vertical gaps.
- Keep navigation, content, and form panels stable at larger widths.
- Reuse shared UI components rather than creating desktop-only visual styles.

## ORMA Rebuild Contract

Before rebuilding screens, create ORMA primitives:

1. `OrmaTheme`
   - Edge-to-edge where supported.
   - Google Sans typography.
   - Light color scheme mapped to ORMA tokens.
   - Font scale cap on mobile if the platform supports it.

2. `OrmaTokens`
   - Colors, spacing, radii, shapes, motion, skeleton defaults.

3. `OrmaSkeletonBlock`
   - Same shimmer/pulse system.

4. `OrmaBackButton` and `OrmaIconButton`
   - 44dp circle, 0.5dp border, zero elevation.

5. `OrmaPrimaryButton`
   - Full-width 56dp auth variant.
   - Capsule CTA variant.
   - Loading skeleton state.

6. `OrmaTextField`
   - `BasicTextField` implementation.
   - Filled tan background.
   - Manual placeholder.
   - No default Material text field chrome.

7. `OrmaFormCard`
   - Grouped filled cards.
   - Row dividers and row field variants.

8. `OrmaAnimatedBottomSheet`
   - Same spring/offset/scrim behavior.

9. `OrmaDialog`
   - Same custom scrim/card/scale behavior.

10. `OrmaNavigationTransitions`
   - Same forward/back/replace transitions.

11. `OrmaPinnedTopBar`
   - Same mobile top bar collapse behavior where needed.

12. `OrmaBottomBar`
   - If ORMA needs bottom nav, implement custom behavior rather than default Material nav.

Only after these are in place should ORMA rebuild:
- Auth start screen.
- Login with phone/email.
- OTP verification.
- Business owner onboarding.
- Team member login.
- Business details form.
- GST/VAT details.
- Business address.
- Logo upload.
- Invoice settings.
- Currency and tax settings.

## Do Not Carry Forward From Current ORMA UI

Remove these patterns from the current ORMA milestone UI:
- Oversized desktop/web chips on mobile.
- Horizontal content that clips on phone width.
- Marketing-style layout in mobile app flow.
- Default-looking cards stacked inside other cards.
- Generic Material text fields.
- Random corner radii not backed by tokens.
- Hardcoded color variations.
- Screen text that explains the UI instead of letting the UI work.

## Implementation Checklist

Use this checklist when rebuilding ORMA:

- Every screen root uses `OrmaTheme`.
- Every color comes from `OrmaColors` or a central status helper.
- Every text style comes from `OrmaTypography` or a small local auth override.
- Mobile screen padding is 20dp or 24dp, not arbitrary.
- Fields are filled `BasicTextField` components, not `OutlinedTextField`.
- Text fields use 16-18dp radius and tan fill.
- Primary mobile CTA is 56dp high and bottom anchored.
- Cards use zero default elevation unless a floating state explicitly needs shadow.
- Bottom sheets use custom spring sheet, not default Material sheet.
- Dialogs use custom scrim/card behavior.
- OTP boxes are custom cells with hidden input.
- Loading uses skeleton blocks before spinners.
- Dividers are thin, low-alpha, and inset.
- Web/desktop use the same design system but wider layout structure.
- DarDoc source remains read-only.
