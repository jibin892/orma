# Dashboard Blocks

Build dashboard pieces as reusable organisms/templates. Do not hard-code ORMA-only data unless the component lives in an ORMA feature package.

## Reusable Blocks

- `DashboardStats`: KPI cards for totals, revenue, customers, orders, stock, bookings.
- `MetricCard`: label, value, delta, status, supporting text.
- `AnalyticsCard`: chart or trend area with loading/empty/error slots.
- `OrderQueue`: active work queue with status filters and row actions.
- `CustomerList`: searchable customer records with compact mobile cards and desktop table.
- `ProductGrid`: catalog with stock status, supplier, SKU/barcode, price.
- `FilterBar`: query, date range, status chips, clear/apply actions.
- `ActionPanel`: primary workflow shortcuts.
- `ActivityFeed`: recent real events only.
- `EmptyState`: icon, title, body, action.
- `DashboardLayout`: mobile bottom nav, tablet two-pane, desktop sidebar + header + content.

## Mobile Rules

- Prefer cards/lists over tables.
- Keep bottom navigation to primary sections only.
- Put secondary workflows such as Team inside Account or a drill-in screen.
- Keep primary actions thumb-reachable.
- Use sheets for create/edit forms.

## Tablet Rules

- Use two-pane layouts for list + detail where useful.
- Keep forms constrained to readable width.
- Use tabs for related panels, not for primary app navigation.

## Desktop/Web Rules

- Use persistent sidebar for primary navigation.
- Use tables for repeated records.
- Use filters/search above the data region.
- Use right-side inspectors or split panes for selected details.
- Keep account/session settings separate from operational work.

## State Requirements

Every block must expose:

- Loading state.
- Empty state.
- Error state.
- Refresh/retry action where relevant.
- Real data only; no fake/sample rows in production components.

## Dashboard Accessibility

- KPI cards should have readable labels before values.
- Tables/lists need deterministic row actions with text labels or accessible descriptions.
- Filters must be keyboard navigable and clearable.
- Status must use text plus color, not color alone.
