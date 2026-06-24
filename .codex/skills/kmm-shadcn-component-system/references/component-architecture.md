# Component Architecture

Use this structure for shared Compose Multiplatform component libraries:

```text
components/
├── ui/              # shadcn-style base primitives
│   ├── button/
│   ├── input/
│   ├── dialog/
│   ├── table/
│   └── ...
├── atoms/           # small branded pieces
│   ├── StatusBadge/
│   ├── Avatar/
│   ├── CurrencyText/
│   └── ...
├── molecules/       # composed controls/cards
│   ├── SearchInput/
│   ├── UserCard/
│   ├── FilterBar/
│   └── ...
├── organisms/       # feature-scale reusable blocks
│   ├── CustomerTable/
│   ├── ProductGrid/
│   ├── DashboardStats/
│   └── ...
└── templates/       # layout shells
    ├── DashboardLayout/
    ├── AuthLayout/
    ├── SettingsLayout/
    └── ...
```

## Layer Rules

- `components/ui`: buttons, fields, popovers, sheets, dialogs, tabs, table primitives, calendar, skeleton, toast, tooltip. No business nouns.
- `atoms`: branded labels, badges, avatars, icons, currency/date text, status text, empty indicators.
- `molecules`: search box, filter bar, card header, user card, product card, status card, date range control.
- `organisms`: customer table, product grid, order queue, analytics cards, team invite form, printer settings block.
- `templates`: mobile app shell, desktop sidebar shell, dashboard layout, settings layout, auth layout.

## Public API Pattern

Prefer stable parameter objects for complex components:

```kotlin
@Immutable
data class OrmaTableColumn<T>(
    val key: String,
    val title: String,
    val width: Dp? = null,
    val alignment: Alignment.Horizontal = Alignment.Start,
    val sortable: Boolean = false,
    val cell: @Composable (T) -> Unit,
)

enum class OrmaButtonVariant { Primary, Secondary, Outline, Ghost, Destructive }
enum class OrmaComponentSize { Sm, Md, Lg }
enum class OrmaComponentDensity { Compact, Comfortable }
```

For every public component include:

- `modifier: Modifier = Modifier`
- State: `enabled`, `loading`, selected/expanded/open state when controlled.
- Slots: `leading`, `trailing`, `actions`, `content` where useful.
- Events: `onClick`, `onValueChange`, `onDismiss`, `onSelected`.
- Theme: use project colors/typography/shapes, not hard-coded colors.

## Variants Requirement

Show all variants when building a reusable component:

- Visual variants: primary, secondary, outline, ghost, destructive, success/warning/info.
- Size variants: small, medium, large.
- State variants: default, hover/focus where supported, disabled, loading, selected, error.
- Density variants for data-heavy desktop/tablet components.

## Responsive Behavior

- Mobile: one column, bottom sheets, sticky bottom actions, horizontal scroll only for optional chips, 48dp minimum touch targets.
- Tablet: two-column forms/lists where width supports it; keep primary forms readable.
- Desktop/Web: sidebar/top navigation, data tables, split panes, inspectors, keyboard focus and hover states.

## Accessibility Checklist

- Provide semantic roles for buttons, tabs, dialogs, switches, checkboxes, and menu items.
- Provide `contentDescription` for non-text icons and `null` for decorative icons.
- Provide `stateDescription` for selected, expanded, checked, error, loading.
- Maintain visible focus on desktop/web surfaces.
- Make touch targets at least 48dp.
- Verify contrast in light and dark themes.
- Avoid using color alone to communicate status.
