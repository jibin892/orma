create table if not exists order_sessions (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid not null references workspaces(id) on delete cascade,
    order_id uuid not null references orders(id) on delete cascade,
    order_item_id uuid references order_items(id) on delete set null,
    sequence_number integer not null default 1,
    title text not null,
    scheduled_at timestamptz,
    status text not null default 'scheduled',
    addon_total numeric(12, 2) not null default 0,
    paid_total numeric(12, 2) not null default 0,
    notes text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists idx_order_sessions_workspace_order
    on order_sessions(workspace_id, order_id, sequence_number);

create index if not exists idx_order_sessions_scheduled_at
    on order_sessions(workspace_id, scheduled_at);
