alter table products
    add column if not exists expiry_date date;

create index if not exists products_workspace_expiry_idx
    on products(workspace_id, expiry_date)
    where expiry_date is not null and status = 'active';
