create table if not exists printer_profiles (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid not null references business_workspaces(id) on delete cascade,
    name text not null,
    connection_type text not null default 'mtp_usb',
    address text,
    paper_width_mm integer not null default 80,
    dpi integer not null default 203,
    supports_receipts boolean not null default true,
    supports_barcodes boolean not null default true,
    is_default_receipt boolean not null default false,
    is_default_barcode boolean not null default false,
    notes text,
    status text not null default 'active',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists printer_profiles_workspace_status_idx
    on printer_profiles(workspace_id, status, created_at desc);

create unique index if not exists printer_profiles_default_receipt_idx
    on printer_profiles(workspace_id)
    where is_default_receipt = true and status = 'active';

create unique index if not exists printer_profiles_default_barcode_idx
    on printer_profiles(workspace_id)
    where is_default_barcode = true and status = 'active';

create index if not exists customers_workspace_search_idx
    on customers(workspace_id, lower(name), lower(coalesce(phone_number, '')), lower(coalesce(email, '')))
    where status = 'active';

create index if not exists suppliers_workspace_search_idx
    on suppliers(workspace_id, lower(name), lower(coalesce(phone_number, '')), lower(coalesce(email, '')))
    where status = 'active';

create index if not exists products_workspace_search_idx
    on products(workspace_id, lower(name), lower(coalesce(sku, '')), lower(coalesce(barcode, '')))
    where status = 'active';

create index if not exists orders_workspace_search_idx
    on orders(workspace_id, lower(order_number), lower(coalesce(notes, '')), status, created_at desc)
    where status <> 'cancelled';
