create table if not exists product_categories (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid not null references business_workspaces(id) on delete cascade,
    name text not null,
    sort_order int not null default 0,
    status text not null default 'active',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (workspace_id, name)
);

alter table products
    add column if not exists category_id uuid references product_categories(id) on delete set null;

create index if not exists products_workspace_category_idx
    on products(workspace_id, category_id)
    where status = 'active';

create table if not exists product_offers (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid not null references business_workspaces(id) on delete cascade,
    product_id uuid references products(id) on delete cascade,
    category_id uuid references product_categories(id) on delete cascade,
    applies_to text not null default 'product',
    name text not null,
    description text,
    discount_type text not null default 'percentage',
    discount_value numeric(12, 2) not null default 0,
    starts_at timestamptz,
    ends_at timestamptz,
    status text not null default 'active',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists product_offers_workspace_status_idx
    on product_offers(workspace_id, status, created_at desc);

create table if not exists workspace_payment_methods (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid not null references business_workspaces(id) on delete cascade,
    type text not null default 'upi',
    label text not null,
    upi_id text,
    payee_name text,
    is_default boolean not null default false,
    status text not null default 'active',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create unique index if not exists workspace_payment_methods_one_default_idx
    on workspace_payment_methods(workspace_id)
    where is_default = true and status = 'active';

create index if not exists workspace_payment_methods_workspace_status_idx
    on workspace_payment_methods(workspace_id, status, created_at desc);

alter table orders
    add column if not exists source text not null default 'dashboard',
    add column if not exists fulfillment_type text not null default 'standard',
    add column if not exists payment_mode text not null default 'pay_on_spot';
