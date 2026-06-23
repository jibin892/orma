alter table meta_connections
    add column if not exists credential_source text not null default 'none',
    add column if not exists access_token_ciphertext text,
    add column if not exists access_token_last4 text,
    add column if not exists connected_at timestamptz,
    add column if not exists catalog_last_synced_at timestamptz;

create table if not exists meta_oauth_states (
    state text primary key,
    workspace_id uuid not null references business_workspaces(id) on delete cascade,
    user_id uuid not null references app_users(id) on delete cascade,
    created_at timestamptz not null default now(),
    expires_at timestamptz not null,
    used_at timestamptz
);

create index if not exists idx_meta_oauth_states_workspace
    on meta_oauth_states(workspace_id, created_at desc);

alter table meta_message_threads
    add column if not exists last_message_preview text;

create unique index if not exists idx_meta_message_threads_workspace_wa_id
    on meta_message_threads(workspace_id, wa_id)
    where wa_id is not null;

alter table meta_leads
    add column if not exists last_message_preview text;

create unique index if not exists idx_meta_leads_thread_unique
    on meta_leads(thread_id)
    where thread_id is not null;
