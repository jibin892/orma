alter table app_users
    add column if not exists phone_number text,
    add column if not exists display_name text,
    add column if not exists provider text,
    add column if not exists notifications_enabled boolean not null default false,
    add column if not exists onboarding_status text not null default 'pending',
    add column if not exists last_login_at timestamptz;

update app_users
set role = 'business_owner'
where role = 'customer';

create table if not exists business_workspaces (
    id uuid primary key default gen_random_uuid(),
    owner_user_id uuid not null references app_users(id),
    business_name text not null,
    legal_name text not null,
    industry text not null,
    website text,
    is_tax_registered boolean not null default false,
    tax_number text,
    tax_label text not null default 'GST/VAT',
    address_line text not null,
    city text not null,
    region text,
    country text not null,
    postal_code text,
    logo_file_name text,
    invoice_prefix text not null,
    next_invoice_number text not null,
    payment_terms text not null,
    invoice_footer text not null,
    currency text not null,
    tax_mode text not null,
    prices_include_tax boolean not null default false,
    onboarding_completed_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists business_workspaces_owner_user_id_idx
    on business_workspaces(owner_user_id);

create table if not exists workspace_members (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid not null references business_workspaces(id) on delete cascade,
    user_id uuid not null references app_users(id) on delete cascade,
    role text not null,
    status text not null default 'active',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (workspace_id, user_id)
);

create index if not exists workspace_members_user_id_idx
    on workspace_members(user_id);

create table if not exists team_invites (
    id uuid primary key default gen_random_uuid(),
    code text not null unique,
    workspace_id uuid not null references business_workspaces(id) on delete cascade,
    created_by_user_id uuid references app_users(id),
    role text not null default 'team_member',
    status text not null default 'active',
    expires_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists team_invites_workspace_id_idx
    on team_invites(workspace_id);
