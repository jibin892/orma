create table if not exists notification_device_tokens (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references app_users(id) on delete cascade,
    workspace_id uuid references business_workspaces(id) on delete cascade,
    token text not null unique,
    platform text not null,
    device_name text,
    enabled boolean not null default true,
    last_seen_at timestamptz not null default now(),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists notification_device_tokens_workspace_enabled_idx
    on notification_device_tokens(workspace_id, enabled, last_seen_at desc);

create index if not exists notification_device_tokens_user_idx
    on notification_device_tokens(user_id, enabled, last_seen_at desc);

create table if not exists notification_events (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid not null references business_workspaces(id) on delete cascade,
    order_id uuid references orders(id) on delete set null,
    event_type text not null,
    title text not null,
    body text not null,
    payload jsonb not null default '{}'::jsonb,
    status text not null default 'queued',
    target_count integer not null default 0,
    success_count integer not null default 0,
    failure_count integer not null default 0,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists notification_events_workspace_created_idx
    on notification_events(workspace_id, created_at desc);

create index if not exists notification_events_order_idx
    on notification_events(order_id)
    where order_id is not null;
