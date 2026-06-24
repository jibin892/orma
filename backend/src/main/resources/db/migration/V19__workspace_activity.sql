create table if not exists workspace_activity (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid not null references business_workspaces(id) on delete cascade,
    actor_user_id uuid references app_users(id) on delete set null,
    actor_display_name text,
    actor_email text,
    actor_phone_number text,
    actor_role text,
    activity_type text not null,
    entity_type text not null,
    entity_id uuid,
    entity_label text,
    title text not null,
    body text not null,
    tone text not null default 'info',
    metadata jsonb not null default '{}'::jsonb,
    created_at timestamptz not null default now()
);

create index if not exists workspace_activity_workspace_created_idx
    on workspace_activity(workspace_id, created_at desc);

create index if not exists workspace_activity_entity_idx
    on workspace_activity(workspace_id, entity_type, entity_id, created_at desc)
    where entity_id is not null;

create index if not exists workspace_activity_actor_idx
    on workspace_activity(workspace_id, actor_user_id, created_at desc)
    where actor_user_id is not null;
