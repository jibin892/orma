alter table orders
    add column if not exists client_request_id text;

create unique index if not exists orders_workspace_source_client_request_uidx
    on orders(workspace_id, source, client_request_id)
    where client_request_id is not null;

create index if not exists orders_workspace_client_request_idx
    on orders(workspace_id, client_request_id)
    where client_request_id is not null;

alter table products
    add column if not exists client_request_id text;

create unique index if not exists products_workspace_client_request_uidx
    on products(workspace_id, client_request_id)
    where client_request_id is not null;
