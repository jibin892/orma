alter table meta_connections
    add column if not exists connection_mode text not null default 'manual_setup',
    add column if not exists business_display_name text,
    add column if not exists whatsapp_display_number text,
    add column if not exists access_token_status text not null default 'not_configured',
    add column if not exists token_expires_at timestamptz,
    add column if not exists webhook_subscribed_at timestamptz,
    add column if not exists messaging_status text not null default 'not_configured';

create table if not exists meta_message_threads (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid references business_workspaces(id) on delete cascade,
    phone_number_id text,
    wa_id text,
    customer_name text,
    phone_number text,
    last_message_at timestamptz,
    status text not null default 'open',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists idx_meta_message_threads_workspace
    on meta_message_threads(workspace_id, updated_at desc);

create table if not exists meta_leads (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid references business_workspaces(id) on delete cascade,
    thread_id uuid references meta_message_threads(id) on delete set null,
    source text not null default 'whatsapp',
    customer_name text,
    phone_number text,
    status text not null default 'new',
    converted_customer_id uuid references customers(id) on delete set null,
    converted_order_id uuid references orders(id) on delete set null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists idx_meta_leads_workspace
    on meta_leads(workspace_id, created_at desc);
