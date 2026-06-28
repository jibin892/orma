create extension if not exists pg_trgm;

-- Date presets and record queues filter orders by the effective work date:
-- scheduled date when present, otherwise creation date.
create index if not exists orders_workspace_effective_date_idx
    on orders(workspace_id, (coalesce(scheduled_at, created_at)), created_at desc);

create index if not exists orders_workspace_status_effective_date_idx
    on orders(workspace_id, status, (coalesce(scheduled_at, created_at)), created_at desc);

create index if not exists orders_workspace_type_status_effective_date_idx
    on orders(workspace_id, order_type, status, (coalesce(scheduled_at, created_at)), created_at desc);

create index if not exists orders_workspace_open_balance_idx
    on orders(workspace_id, status, created_at desc)
    where total > paid_total and status <> 'cancelled';

create index if not exists orders_workspace_source_created_idx
    on orders(workspace_id, source, created_at desc)
    where source is not null;

create index if not exists orders_workspace_payment_mode_created_idx
    on orders(workspace_id, payment_mode, created_at desc);

create index if not exists orders_workspace_fulfillment_created_idx
    on orders(workspace_id, fulfillment_type, created_at desc);

-- Customer searches now include location and notes from the detail/profile flow.
create index if not exists customers_address_line_trgm_idx
    on customers using gin ((coalesce(address_line, '')) gin_trgm_ops)
    where status = 'active';

create index if not exists customers_city_trgm_idx
    on customers using gin ((coalesce(city, '')) gin_trgm_ops)
    where status = 'active';

create index if not exists customers_region_trgm_idx
    on customers using gin ((coalesce(region, '')) gin_trgm_ops)
    where status = 'active';

create index if not exists customers_country_trgm_idx
    on customers using gin ((coalesce(country, '')) gin_trgm_ops)
    where status = 'active';

create index if not exists customers_postal_code_trgm_idx
    on customers using gin ((coalesce(postal_code, '')) gin_trgm_ops)
    where status = 'active';

create index if not exists customers_notes_trgm_idx
    on customers using gin ((coalesce(notes, '')) gin_trgm_ops)
    where status = 'active';

-- Supplier search and payable/balance actions use payment fields and batch totals.
create index if not exists suppliers_payment_terms_trgm_idx
    on suppliers using gin ((coalesce(payment_terms, '')) gin_trgm_ops)
    where status = 'active';

create index if not exists suppliers_payment_mode_trgm_idx
    on suppliers using gin ((coalesce(payment_mode, '')) gin_trgm_ops)
    where status = 'active';

create index if not exists suppliers_payment_reference_trgm_idx
    on suppliers using gin ((coalesce(payment_reference, '')) gin_trgm_ops)
    where status = 'active';

create index if not exists suppliers_notes_trgm_idx
    on suppliers using gin ((coalesce(notes, '')) gin_trgm_ops)
    where status = 'active';

create index if not exists stock_movements_workspace_supplier_payables_idx
    on stock_movements(workspace_id, supplier_id)
    where supplier_id is not null and (payable_amount <> 0 or paid_amount <> 0);

create index if not exists stock_movements_workspace_expiry_idx
    on stock_movements(workspace_id, expiry_date)
    where expiry_date is not null;

-- Public catalog, barcode scanner, bulk import, and package/variant flows.
create index if not exists products_workspace_upper_sku_active_idx
    on products(workspace_id, upper(sku))
    where sku is not null and status = 'active';

create index if not exists products_workspace_barcode_active_idx
    on products(workspace_id, barcode)
    where barcode is not null and status = 'active';

create index if not exists products_workspace_active_catalog_idx
    on products(workspace_id, category_id, name)
    where status = 'active';

create index if not exists products_workspace_item_type_created_idx
    on products(workspace_id, item_type, created_at desc)
    where status <> 'archived';

create index if not exists product_variants_workspace_barcode_idx
    on product_variants(workspace_id, barcode)
    where barcode is not null and status = 'active';

create index if not exists product_variants_workspace_active_id_idx
    on product_variants(workspace_id, id)
    where status = 'active';

create index if not exists product_variants_workspace_product_stock_idx
    on product_variants(workspace_id, product_id, stock_quantity)
    where status = 'active';

create index if not exists product_variants_name_trgm_idx
    on product_variants using gin (name gin_trgm_ops)
    where status <> 'archived';

create index if not exists product_variants_sku_trgm_idx
    on product_variants using gin ((coalesce(sku, '')) gin_trgm_ops)
    where status <> 'archived';

create index if not exists product_variants_barcode_trgm_idx
    on product_variants using gin ((coalesce(barcode, '')) gin_trgm_ops)
    where status <> 'archived';

create index if not exists product_categories_workspace_active_created_idx
    on product_categories(workspace_id, created_at desc)
    where status = 'active';

create index if not exists product_categories_workspace_type_sort_idx
    on product_categories(workspace_id, item_type, sort_order, name)
    where status = 'active';

-- Offer listing, public catalog offer resolution, coupon lookup, and checkout apply.
create index if not exists product_offers_active_window_idx
    on product_offers(workspace_id, applies_to, starts_at, ends_at, created_at desc)
    where status = 'active' and customer_id is null and coupon_code is null;

create index if not exists product_offers_active_all_idx
    on product_offers(workspace_id, created_at desc)
    where status = 'active' and applies_to = 'all' and customer_id is null and coupon_code is null;

create index if not exists product_offers_name_trgm_idx
    on product_offers using gin (name gin_trgm_ops)
    where status = 'active';

create index if not exists product_offers_description_trgm_idx
    on product_offers using gin ((coalesce(description, '')) gin_trgm_ops)
    where status = 'active';

create index if not exists product_offers_coupon_trgm_idx
    on product_offers using gin ((coalesce(coupon_code, '')) gin_trgm_ops)
    where status = 'active';

-- UPI/default payment method search and public checkout QR lookup.
create index if not exists workspace_payment_methods_label_trgm_idx
    on workspace_payment_methods using gin (label gin_trgm_ops)
    where status = 'active';

create index if not exists workspace_payment_methods_upi_id_trgm_idx
    on workspace_payment_methods using gin ((coalesce(upi_id, '')) gin_trgm_ops)
    where status = 'active';

create index if not exists workspace_payment_methods_payee_name_trgm_idx
    on workspace_payment_methods using gin ((coalesce(payee_name, '')) gin_trgm_ops)
    where status = 'active';

-- Printer and barcode-device setup search/default actions.
create index if not exists printer_profiles_connection_type_trgm_idx
    on printer_profiles using gin (connection_type gin_trgm_ops)
    where status = 'active';

create index if not exists printer_profiles_address_trgm_idx
    on printer_profiles using gin ((coalesce(address, '')) gin_trgm_ops)
    where status = 'active';

create index if not exists printer_profiles_notes_trgm_idx
    on printer_profiles using gin ((coalesce(notes, '')) gin_trgm_ops)
    where status = 'active';

create index if not exists printer_profiles_receipt_candidate_idx
    on printer_profiles(workspace_id, supports_receipts, created_at desc)
    where status = 'active';

create index if not exists printer_profiles_barcode_candidate_idx
    on printer_profiles(workspace_id, supports_barcodes, created_at desc)
    where status = 'active';

-- Push fan-out, logout cleanup, and notification activity views.
create index if not exists notification_tokens_workspace_active_platform_idx
    on notification_device_tokens(workspace_id, lower(platform), user_id)
    where enabled = true;

create index if not exists notification_tokens_user_token_idx
    on notification_device_tokens(user_id, token);

create index if not exists notification_events_workspace_status_created_idx
    on notification_events(workspace_id, status, created_at desc);

-- Team invite/member detail actions use text ids from shared KMM models.
create index if not exists team_invites_workspace_text_id_active_idx
    on team_invites(workspace_id, (id::text), status)
    where status in ('active', 'pending');

create index if not exists workspace_members_workspace_text_id_active_idx
    on workspace_members(workspace_id, (id::text), status)
    where status = 'active';

create index if not exists team_invites_active_lower_email_idx
    on team_invites(lower(invitee_email), created_at desc)
    where status in ('active', 'pending') and invitee_email is not null;

create index if not exists team_invites_active_phone_digits_idx
    on team_invites((regexp_replace(coalesce(invitee_phone_number, ''), '[^0-9]', '', 'g')), created_at desc)
    where status in ('active', 'pending') and invitee_phone_number is not null;

-- Package/session scheduling and completion queues.
create index if not exists order_sessions_workspace_status_scheduled_idx
    on order_sessions(workspace_id, status, scheduled_at, sequence_number);

create index if not exists order_sessions_order_item_idx
    on order_sessions(order_item_id)
    where order_item_id is not null;
