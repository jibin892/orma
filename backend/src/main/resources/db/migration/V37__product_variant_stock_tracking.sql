alter table product_variants
    add column if not exists track_stock boolean not null default true;

update product_variants pv
set track_stock = false
from products p
where pv.product_id = p.id
  and pv.workspace_id = p.workspace_id
  and (p.item_type <> 'product' or p.track_stock = false);

create index if not exists product_variants_workspace_product_tracked_stock_idx
    on product_variants(workspace_id, product_id, stock_quantity)
    where track_stock = true and status = 'active';
