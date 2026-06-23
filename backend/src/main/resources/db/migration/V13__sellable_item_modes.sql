alter table business_workspaces
    add column if not exists business_mode text not null default 'product_selling';

update business_workspaces
set business_mode = case
    when lower(industry) in ('services', 'service', 'repair', 'professional services', 'professional service') then 'service_selling'
    when lower(industry) in ('salon', 'healthcare', 'clinic', 'fitness', 'education') then 'appointment'
    when lower(industry) = 'b2b' then 'mixed'
    else 'product_selling'
end
where business_mode = 'product_selling';

alter table products
    add column if not exists item_type text not null default 'product',
    add column if not exists duration_minutes int,
    add column if not exists booking_required boolean not null default false;

update products
set item_type = 'service',
    track_stock = false,
    stock_quantity = 0,
    reorder_level = 0
where item_type = 'product'
  and track_stock = false;

update products
set track_stock = false,
    stock_quantity = 0,
    reorder_level = 0,
    booking_required = case when item_type = 'appointment' then true else booking_required end
where item_type in ('service', 'appointment');

alter table orders
    add column if not exists order_type text not null default 'sale';

update orders
set order_type = case
    when fulfillment_type = 'booking' then 'appointment'
    when fulfillment_type = 'scheduled' and scheduled_at is not null then 'service'
    else 'sale'
end
where order_type = 'sale';

create index if not exists products_workspace_item_type_idx
    on products(workspace_id, item_type, status, created_at desc);

create index if not exists orders_workspace_order_type_idx
    on orders(workspace_id, order_type, status, created_at desc);
