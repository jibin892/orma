alter table product_offers
    add column if not exists discount_cap_amount numeric(12, 2),
    add column if not exists coupon_code text,
    add column if not exists customer_id uuid references customers(id) on delete set null;

create index if not exists product_offers_workspace_coupon_idx
    on product_offers (workspace_id, lower(coupon_code))
    where coupon_code is not null and status = 'active';

create index if not exists product_offers_workspace_customer_idx
    on product_offers (workspace_id, customer_id)
    where customer_id is not null and status = 'active';
