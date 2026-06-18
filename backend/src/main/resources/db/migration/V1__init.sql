create extension if not exists pgcrypto;

create table if not exists app_users (
    id uuid primary key default gen_random_uuid(),
    firebase_uid text not null unique,
    email text,
    role text not null default 'customer',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists bookings (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references app_users(id),
    status text not null default 'draft',
    scheduled_at timestamptz,
    notes text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists audit_events (
    id uuid primary key default gen_random_uuid(),
    actor_user_id uuid references app_users(id),
    event_type text not null,
    entity_type text,
    entity_id uuid,
    metadata jsonb not null default '{}'::jsonb,
    created_at timestamptz not null default now()
);
