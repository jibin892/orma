create table if not exists product_variant_components (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid not null references business_workspaces(id) on delete cascade,
    package_variant_id uuid not null references product_variants(id) on delete cascade,
    component_product_id uuid not null references products(id) on delete restrict,
    component_variant_id uuid references product_variants(id) on delete restrict,
    quantity numeric(12, 2) not null default 1,
    sort_order integer not null default 0,
    status text not null default 'active',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists product_variant_components_package_idx
    on product_variant_components(package_variant_id, status, sort_order);

create index if not exists product_variant_components_product_idx
    on product_variant_components(component_product_id)
    where status = 'active';

create index if not exists product_variant_components_variant_idx
    on product_variant_components(component_variant_id)
    where component_variant_id is not null and status = 'active';
