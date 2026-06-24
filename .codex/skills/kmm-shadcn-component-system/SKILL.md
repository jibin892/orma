---
name: kmm-shadcn-component-system
description: Build or refactor reusable Kotlin Multiplatform / Compose Multiplatform component libraries inspired by shadcn/ui and Radix composition. Use when creating KMM components, design-system primitives, atomic UI layers, dashboard blocks, reusable tables/tabs/date pickers/forms/dialogs, responsive Web/Mobile/Tablet/Desktop layouts, accessibility-aware components, theme-aware Light/Dark variants, or documentation for scalable cross-app Compose UI.
---

# KMM Shadcn Component System

Use this skill to build a reusable Compose Multiplatform component library that follows shadcn-style ownership and composition while staying idiomatic Kotlin.

## Required References

Read only what the task needs:

- `references/component-architecture.md`: mandatory before creating folders, naming APIs, or deciding atom/molecule/organism/template placement.
- `references/shadcn-kmm-mapping.md`: mandatory when translating shadcn/Radix components such as Table, Tabs, Date Picker, Dialog, Sheet, Popover, Select, forms, or navigation.
- `references/dashboard-blocks.md`: mandatory when building dashboards, analytics blocks, operational tables, filters, order/customer/product modules, or app shells.

## Workflow

1. Inspect the existing KMM project structure and design tokens before editing.
2. Identify the target layer: `components/ui`, `atoms`, `molecules`, `organisms`, or `templates`.
3. Define the public API first:
   - Stable type-safe models: `enum class`, `sealed interface`, immutable data classes.
   - Variant parameters: `variant`, `size`, `tone`, `state`, `density`, `orientation`, `enabled`, `loading`.
   - Slot parameters for composition: `leading`, `trailing`, `header`, `footer`, `actions`, `content`.
4. Make platform behavior explicit:
   - Mobile: touch-first, one-column, bottom sheets, large hit targets.
   - Tablet: two-pane where useful, keep forms constrained.
   - Desktop/Web: persistent navigation, tables, split panes, hover/focus affordances.
5. Implement using existing tokens, typography, shapes, and color roles before adding new tokens.
6. Add accessibility:
   - `Modifier.semantics`, role/state descriptions, content descriptions, focus order, minimum 48dp touch targets.
   - Keyboard navigation for desktop/web-like components.
   - WCAG contrast for all states.
7. Document every reusable component with KDoc and a small usage example near the component or in the local docs/showcase pattern used by the project.
8. Verify with compile commands for affected targets and, when available, screenshots across mobile and wide breakpoints.

## Rules

- Do not translate React/shadcn code literally. Translate composition, API shape, variants, accessibility behavior, and theming into Compose.
- Keep primitives small. Build richer workflows by composing primitives.
- Do not create app-specific components in `components/ui`; put business-specific components under organisms/templates or feature packages.
- Never show fake data in reusable dashboard blocks; provide empty/loading/error slots and sample data only in previews/showcases.
- Prefer one source of truth for tokens and state models; do not duplicate per-platform styles unless behavior truly differs.
- Make all components usable by future apps, not only ORMA.

## Output Contract

When completing work with this skill, report:

- Component layer and files changed.
- Public APIs/variants added.
- Mobile/tablet/desktop behavior.
- Accessibility coverage.
- Verification commands and any visual checks.
