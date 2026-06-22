create table if not exists meta_connections (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid not null references business_workspaces(id) on delete cascade,
    status text not null default 'not_connected',
    business_id text,
    whatsapp_business_account_id text,
    phone_number_id text,
    catalog_id text,
    page_id text,
    instagram_business_account_id text,
    scopes text[] not null default '{}',
    last_sync_at timestamptz,
    last_error text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint meta_connections_workspace_unique unique (workspace_id)
);

create table if not exists meta_product_sync (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid not null references business_workspaces(id) on delete cascade,
    product_id uuid not null references products(id) on delete cascade,
    meta_product_id text,
    status text not null default 'not_synced',
    readiness_issues text[] not null default '{}',
    last_sync_at timestamptz,
    last_error text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint meta_product_sync_workspace_product_unique unique (workspace_id, product_id)
);

create table if not exists meta_webhook_events (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid references business_workspaces(id) on delete set null,
    object_type text,
    event_type text,
    payload jsonb not null default '{}'::jsonb,
    received_at timestamptz not null default now(),
    processed_at timestamptz,
    status text not null default 'received'
);

create index if not exists idx_meta_connections_workspace on meta_connections(workspace_id);
create index if not exists idx_meta_product_sync_workspace on meta_product_sync(workspace_id);
create index if not exists idx_meta_webhook_events_received_at on meta_webhook_events(received_at desc);
