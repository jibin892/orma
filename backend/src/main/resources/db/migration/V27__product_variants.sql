create table if not exists product_variants (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid not null references business_workspaces(id) on delete cascade,
    product_id uuid not null references products(id) on delete cascade,
    name text not null,
    sku text,
    barcode text,
    selling_price numeric(12, 2) not null default 0,
    cost_price numeric(12, 2) not null default 0,
    stock_quantity numeric(12, 2) not null default 0,
    duration_minutes integer,
    sort_order integer not null default 0,
    status text not null default 'active',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (workspace_id, product_id, name)
);

create unique index if not exists product_variants_workspace_sku_idx
    on product_variants(workspace_id, upper(sku))
    where sku is not null and status = 'active';

create index if not exists product_variants_product_sort_idx
    on product_variants(product_id, status, sort_order, name);

alter table order_items
    add column if not exists variant_id uuid references product_variants(id) on delete set null,
    add column if not exists variant_name text;

create index if not exists order_items_variant_idx
    on order_items(variant_id)
    where variant_id is not null;
