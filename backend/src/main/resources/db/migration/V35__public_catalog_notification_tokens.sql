create table if not exists public_catalog_notification_device_tokens (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid not null references business_workspaces(id) on delete cascade,
    firebase_uid text not null,
    email text,
    phone_number text,
    token text not null unique,
    platform text not null,
    device_name text,
    enabled boolean not null default true,
    last_seen_at timestamptz not null default now(),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists public_catalog_notification_tokens_workspace_uid_idx
    on public_catalog_notification_device_tokens(workspace_id, firebase_uid, enabled, last_seen_at desc);

create index if not exists public_catalog_notification_tokens_workspace_email_idx
    on public_catalog_notification_device_tokens(workspace_id, lower(email), enabled, last_seen_at desc)
    where email is not null;

create index if not exists public_catalog_notification_tokens_workspace_phone_idx
    on public_catalog_notification_device_tokens(workspace_id, phone_number, enabled, last_seen_at desc)
    where phone_number is not null;
