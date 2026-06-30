-- Focused indexes for high-traffic dashboard, catalog, checkout, and account APIs.
-- Keep these query-shaped and partial so write overhead stays bounded.

-- Product and package admin lists page by newest record, sometimes scoped by category.
create index if not exists products_workspace_visible_created_idx
    on products(workspace_id, created_at desc)
    where status <> 'archived';

create index if not exists products_workspace_category_visible_created_idx
    on products(workspace_id, category_id, created_at desc)
    where status <> 'archived' and category_id is not null;

-- Exact lookup helpers used by import/create flows.
create index if not exists suppliers_workspace_lower_name_active_idx
    on suppliers(workspace_id, lower(name))
    where status = 'active';

create index if not exists product_categories_workspace_lower_name_active_idx
    on product_categories(workspace_id, lower(name), item_type, sort_order)
    where status = 'active';

-- Public catalog account/order history can identify customers by lower(email).
create index if not exists customers_workspace_lower_email_active_idx
    on customers(workspace_id, lower(email), created_at desc)
    where status = 'active' and email is not null;

create index if not exists orders_workspace_public_customer_created_idx
    on orders(workspace_id, customer_id, created_at desc)
    where source = 'public_catalog' and customer_id is not null;

-- Checkout inventory reservation checks only care about un-applied public catalog orders.
create index if not exists orders_workspace_public_pending_inventory_idx
    on orders(workspace_id, status, created_at desc, id)
    where source = 'public_catalog' and inventory_applied = false;

create index if not exists order_items_order_product_variant_idx
    on order_items(order_id, product_id, variant_id)
    where product_id is not null;

-- Package/component stock checks filter by workspace and component item.
create index if not exists product_variant_components_workspace_component_active_idx
    on product_variant_components(workspace_id, component_product_id, component_variant_id, package_variant_id)
    where status = 'active';

create index if not exists product_variant_components_workspace_package_active_idx
    on product_variant_components(workspace_id, package_variant_id, sort_order)
    where status = 'active';
