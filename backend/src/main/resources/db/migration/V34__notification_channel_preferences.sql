alter table app_users
    add column if not exists notification_catalog_orders_enabled boolean not null default true,
    add column if not exists notification_status_updates_enabled boolean not null default true,
    add column if not exists notification_billing_enabled boolean not null default true,
    add column if not exists notification_stock_enabled boolean not null default true,
    add column if not exists notification_team_enabled boolean not null default true,
    add column if not exists notification_marketing_enabled boolean not null default false;
