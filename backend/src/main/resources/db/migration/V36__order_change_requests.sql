create table if not exists order_change_requests (
    id uuid primary key default gen_random_uuid(),
    workspace_id uuid not null references business_workspaces(id) on delete cascade,
    order_id uuid not null references orders(id) on delete cascade,
    customer_id uuid references customers(id) on delete set null,
    firebase_uid text,
    status text not null default 'pending',
    requested_items jsonb not null default '[]'::jsonb,
    requested_payment_mode text,
    requested_paid_total numeric(12,2),
    payment_reference text,
    customer_notes text,
    business_notes text,
    resolved_by_user_id uuid references app_users(id) on delete set null,
    resolved_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists order_change_requests_workspace_order_idx
    on order_change_requests(workspace_id, order_id, status, created_at desc);

create index if not exists order_change_requests_workspace_pending_idx
    on order_change_requests(workspace_id, status, created_at desc)
    where status = 'pending';
