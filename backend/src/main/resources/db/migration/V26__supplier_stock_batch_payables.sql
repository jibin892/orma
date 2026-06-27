alter table stock_movements
    add column if not exists supplier_id uuid references suppliers(id) on delete set null,
    add column if not exists unit_cost numeric(12, 2) not null default 0,
    add column if not exists payable_amount numeric(12, 2) not null default 0,
    add column if not exists paid_amount numeric(12, 2) not null default 0,
    add column if not exists expiry_date date;

create index if not exists stock_movements_supplier_created_idx
    on stock_movements(workspace_id, supplier_id, created_at desc)
    where supplier_id is not null;
