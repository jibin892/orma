alter table product_variants
    add column if not exists included_quantity integer not null default 1,
    add column if not exists addons_json jsonb not null default '[]'::jsonb;

update product_variants
set included_quantity = 1
where included_quantity < 1;
