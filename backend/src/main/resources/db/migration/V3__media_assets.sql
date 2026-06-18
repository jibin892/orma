create table if not exists product_images (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid not null references business_workspaces(id) on delete cascade,
    product_id text not null,
    storage_path text not null unique,
    original_file_name text,
    content_type text not null,
    size_bytes bigint not null,
    sort_order integer not null default 0,
    status text not null default 'active',
    created_by_user_id uuid references app_users(id),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists product_images_workspace_product_idx
    on product_images(workspace_id, product_id);
