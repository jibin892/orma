# shadcn to KMM Mapping

The shadcn docs describe source-owned, composable React components. In KMM, translate those ideas into Compose primitives with controlled state and slots.

Sources checked:

- shadcn components index: `https://ui.shadcn.com/docs/components`
- Table: `https://ui.shadcn.com/docs/components/radix/table`
- Tabs: `https://ui.shadcn.com/docs/components/radix/tabs`
- Date Picker: `https://ui.shadcn.com/docs/components/radix/date-picker`

## Component Catalog

Create equivalents as needed:

- Layout/shell: AspectRatio, Breadcrumb, Card, Carousel, Collapsible, Drawer, Resizable, ScrollArea, Separator, Sheet, Sidebar.
- Inputs/forms: Button, ButtonGroup, Checkbox, Combobox, Field, Input, InputGroup, InputOTP, Label, NativeSelect, RadioGroup, Select, Slider, Switch, Textarea, Toggle, ToggleGroup.
- Overlays: AlertDialog, ContextMenu, Dialog, DropdownMenu, HoverCard, Menubar, Popover, Tooltip.
- Feedback/data: Alert, Avatar, Badge, Calendar, Chart, DataTable, DatePicker, Empty, Item, Kbd, Pagination, Progress, Skeleton, Sonner/Toast, Spinner, Table, Tabs, Typography.
- Direction/i18n: Direction and RTL-aware layout helpers.

## Table

shadcn Table is a responsive composition:

```text
Table
├── TableCaption
├── TableHeader
│   └── TableRow
│       └── TableHead
├── TableBody
│   └── TableRow
│       └── TableCell
└── TableFooter
```

KMM equivalent:

- `OrmaTable<T>`: organism-ready data table.
- `OrmaTableHeader`, `OrmaTableRow`, `OrmaTableCell` if low-level composition is needed.
- `OrmaTableColumn<T>` typed column definitions.
- Support empty/loading/error states, sticky header where possible, row actions, sorting, filtering, pagination.

Responsive behavior:

- Mobile: render rows as stacked cards or compact list items, not a wide table.
- Tablet: table with horizontal scroll or two-column row cards depending on density.
- Desktop/Web: full table with header, row hover, keyboard focus, sorting/filter controls, pagination.

Accessibility:

- Announce caption/title.
- Use row/cell semantics where Compose target supports it.
- Row actions must be reachable by keyboard and screen readers.

## Tabs

shadcn Tabs composition:

```text
Tabs
├── TabsList
│   └── TabsTrigger
└── TabsContent
```

KMM equivalent:

- `OrmaTabs<T>` with typed tab model.
- `OrmaTabItem<T>(key, label, icon, enabled, badge)`.
- Support `Segmented`, `Line`, `Pill`, `Vertical`, and `Icon` variants.

Responsive behavior:

- Mobile: horizontal scroll or segmented/pill tabs; avoid vertical tabs.
- Tablet: horizontal tabs or two-column settings tabs.
- Desktop/Web: horizontal, line, or vertical tabs with keyboard arrow navigation.

Accessibility:

- Selected tab has state description.
- Disabled tabs are non-clickable and announced disabled.
- Content should preserve meaningful focus after selection.

## Date Picker

shadcn Date Picker is not a single root primitive; it composes Popover plus Calendar. It includes basic, range, date of birth, input, time, and natural-language examples.

KMM equivalent:

- `OrmaDatePickerField`: input/button trigger plus platform-specific picker surface.
- `OrmaCalendar`: calendar grid primitive.
- `OrmaDateRangePickerField`: start/end range control.
- `OrmaDateTimePickerField`: date plus time.

Responsive behavior:

- Mobile: open as bottom sheet or native platform picker where available.
- Tablet: popover or sheet depending on width.
- Desktop/Web: popover anchored to the trigger, with keyboard navigation.

Accessibility:

- Trigger announces selected date or empty state.
- Calendar cells announce day, selected, disabled, today.
- Range selection announces start/end.
- Manual input validates with inline errors.

## Primitive Mapping Notes

- Dialog/AlertDialog: use controlled `open` state; trap focus on desktop/web; sheet-style full-width bottom presentation on mobile when appropriate.
- Sheet/Drawer: mobile bottom sheet; tablet/desktop side sheet.
- Popover/HoverCard/Tooltip: use desktop/web hover/focus behavior; mobile should use tap-triggered sheet or inline help.
- Select/Combobox/Command: mobile sheet picker; desktop popover with keyboard search.
- Toast/Sonner: short, non-blocking status; do not use for critical errors that need user action.
- Skeleton/Spinner: prefer skeletons for content loading; use spinners only for small inline actions.
- Forms: use `Field` composition: label, control, description, error.

## Documentation Template

Each reusable component should document:

- Purpose.
- Variants and states.
- Responsive behavior.
- Accessibility behavior.
- Usage example.
- Do/don't notes.
