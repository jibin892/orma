create extension if not exists pg_trgm;

-- Public catalog and product list views repeatedly fetch the first active image
-- for each product, ordered by sort priority.
create index if not exists product_images_active_cover_idx
    on product_images(workspace_id, product_id, sort_order asc, created_at desc)
    where status = 'active';

-- Customer order history filters by workspace/customer and sorts newest first.
create index if not exists orders_workspace_customer_created_idx
    on orders(workspace_id, customer_id, created_at desc)
    where customer_id is not null;

-- Supplier-scoped catalog filtering and joins.
create index if not exists products_workspace_supplier_created_idx
    on products(workspace_id, supplier_id, created_at desc)
    where supplier_id is not null and status <> 'archived';

-- Stock movement audit/history by workspace.
create index if not exists stock_movements_workspace_created_idx
    on stock_movements(workspace_id, created_at desc);

-- Product-offer lookup paths used by public catalog lateral joins.
create index if not exists product_offers_active_product_idx
    on product_offers(workspace_id, product_id, created_at desc)
    where status = 'active' and product_id is not null;

create index if not exists product_offers_active_category_idx
    on product_offers(workspace_id, category_id, created_at desc)
    where status = 'active' and category_id is not null;

-- Foreign-key and join helpers that are not covered by primary-key lookups.
create index if not exists order_items_product_id_idx
    on order_items(product_id)
    where product_id is not null;

create index if not exists stock_movements_workspace_product_created_idx
    on stock_movements(workspace_id, product_id, created_at desc);

create index if not exists products_supplier_id_idx
    on products(supplier_id)
    where supplier_id is not null;

create index if not exists orders_customer_id_idx
    on orders(customer_id)
    where customer_id is not null;

-- Fast active-list reads with default ordering.
create index if not exists team_invites_workspace_status_created_idx
    on team_invites(workspace_id, status, created_at desc);

create index if not exists workspace_members_workspace_status_created_idx
    on workspace_members(workspace_id, status, created_at);

create index if not exists workspace_members_user_status_created_idx
    on workspace_members(user_id, status, created_at);

create index if not exists notification_events_workspace_failures_idx
    on notification_events(workspace_id, created_at desc)
    where failure_count > 0;

create index if not exists workspace_payment_methods_active_type_idx
    on workspace_payment_methods(workspace_id, type, is_default desc, created_at desc)
    where status = 'active';

-- Trigram indexes for CPU-heavy ILIKE '%term%' searches. These match the
-- current query expressions in DashboardRepository.
create index if not exists customers_name_trgm_idx
    on customers using gin (name gin_trgm_ops)
    where status = 'active';

create index if not exists customers_phone_trgm_idx
    on customers using gin ((coalesce(phone_number, '')) gin_trgm_ops)
    where status = 'active';

create index if not exists customers_email_trgm_idx
    on customers using gin ((coalesce(email, '')) gin_trgm_ops)
    where status = 'active';

create index if not exists customers_tax_number_trgm_idx
    on customers using gin ((coalesce(tax_number, '')) gin_trgm_ops)
    where status = 'active';

create index if not exists suppliers_name_trgm_idx
    on suppliers using gin (name gin_trgm_ops)
    where status = 'active';

create index if not exists suppliers_phone_trgm_idx
    on suppliers using gin ((coalesce(phone_number, '')) gin_trgm_ops)
    where status = 'active';

create index if not exists suppliers_email_trgm_idx
    on suppliers using gin ((coalesce(email, '')) gin_trgm_ops)
    where status = 'active';

create index if not exists suppliers_tax_number_trgm_idx
    on suppliers using gin ((coalesce(tax_number, '')) gin_trgm_ops)
    where status = 'active';

create index if not exists products_name_trgm_idx
    on products using gin (name gin_trgm_ops)
    where status <> 'archived';

create index if not exists products_sku_trgm_idx
    on products using gin ((coalesce(sku, '')) gin_trgm_ops)
    where status <> 'archived';

create index if not exists products_barcode_trgm_idx
    on products using gin ((coalesce(barcode, '')) gin_trgm_ops)
    where status <> 'archived';

create index if not exists products_description_trgm_idx
    on products using gin ((coalesce(description, '')) gin_trgm_ops)
    where status <> 'archived';

create index if not exists product_categories_name_trgm_idx
    on product_categories using gin (name gin_trgm_ops)
    where status = 'active';

create index if not exists orders_order_number_trgm_idx
    on orders using gin (order_number gin_trgm_ops);

create index if not exists orders_notes_trgm_idx
    on orders using gin ((coalesce(notes, '')) gin_trgm_ops);

create index if not exists order_items_description_trgm_idx
    on order_items using gin ((coalesce(description, '')) gin_trgm_ops);

create index if not exists printer_profiles_name_trgm_idx
    on printer_profiles using gin (name gin_trgm_ops)
    where status = 'active';
