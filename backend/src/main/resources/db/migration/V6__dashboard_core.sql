create table if not exists customers (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid not null references business_workspaces(id) on delete cascade,
    name text not null,
    phone_number text,
    email text,
    address_line text,
    city text,
    region text,
    country text,
    postal_code text,
    notes text,
    status text not null default 'active',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists customers_workspace_status_idx
    on customers(workspace_id, status, created_at desc);

create index if not exists customers_workspace_phone_idx
    on customers(workspace_id, phone_number)
    where phone_number is not null;

create table if not exists suppliers (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid not null references business_workspaces(id) on delete cascade,
    name text not null,
    phone_number text,
    email text,
    tax_number text,
    address_line text,
    notes text,
    status text not null default 'active',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists suppliers_workspace_status_idx
    on suppliers(workspace_id, status, created_at desc);

create table if not exists products (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid not null references business_workspaces(id) on delete cascade,
    supplier_id uuid references suppliers(id) on delete set null,
    name text not null,
    sku text,
    barcode text,
    description text,
    unit text not null default 'pcs',
    selling_price numeric(12, 2) not null default 0,
    cost_price numeric(12, 2) not null default 0,
    currency text not null,
    tax_rate numeric(5, 2) not null default 0,
    prices_include_tax boolean not null default false,
    stock_quantity numeric(12, 2) not null default 0,
    reorder_level numeric(12, 2) not null default 0,
    track_stock boolean not null default true,
    status text not null default 'active',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (workspace_id, sku)
);

create index if not exists products_workspace_status_idx
    on products(workspace_id, status, created_at desc);

create index if not exists products_workspace_low_stock_idx
    on products(workspace_id, stock_quantity, reorder_level)
    where track_stock = true and status = 'active';

create table if not exists stock_movements (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid not null references business_workspaces(id) on delete cascade,
    product_id uuid not null references products(id) on delete cascade,
    movement_type text not null,
    quantity_delta numeric(12, 2) not null,
    balance_after numeric(12, 2) not null,
    note text,
    created_by_user_id uuid references app_users(id),
    created_at timestamptz not null default now()
);

create index if not exists stock_movements_product_created_idx
    on stock_movements(product_id, created_at desc);

create table if not exists orders (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid not null references business_workspaces(id) on delete cascade,
    customer_id uuid references customers(id) on delete set null,
    order_number text not null,
    status text not null default 'confirmed',
    scheduled_at timestamptz,
    subtotal numeric(12, 2) not null default 0,
    tax_total numeric(12, 2) not null default 0,
    discount_total numeric(12, 2) not null default 0,
    paid_total numeric(12, 2) not null default 0,
    total numeric(12, 2) not null default 0,
    currency text not null,
    notes text,
    inventory_applied boolean not null default false,
    created_by_user_id uuid references app_users(id),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (workspace_id, order_number)
);

create index if not exists orders_workspace_status_idx
    on orders(workspace_id, status, created_at desc);

create index if not exists orders_workspace_scheduled_idx
    on orders(workspace_id, scheduled_at)
    where scheduled_at is not null;

create table if not exists order_items (
    id uuid primary key default gen_random_uuid(),
    order_id uuid not null references orders(id) on delete cascade,
    product_id uuid references products(id) on delete set null,
    description text not null,
    quantity numeric(12, 2) not null,
    unit_price numeric(12, 2) not null,
    tax_rate numeric(5, 2) not null default 0,
    line_subtotal numeric(12, 2) not null,
    line_tax numeric(12, 2) not null,
    line_total numeric(12, 2) not null
);

create index if not exists order_items_order_id_idx
    on order_items(order_id);
