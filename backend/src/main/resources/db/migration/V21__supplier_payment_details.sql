alter table suppliers
    add column if not exists payment_terms text,
    add column if not exists payment_mode text,
    add column if not exists payment_reference text,
    add column if not exists payable_total numeric(12, 2) not null default 0,
    add column if not exists paid_total numeric(12, 2) not null default 0,
    add column if not exists currency text not null default 'INR',
    add column if not exists last_payment_at timestamptz;

create index if not exists suppliers_workspace_payment_idx
    on suppliers(workspace_id, status, payable_total, paid_total);
