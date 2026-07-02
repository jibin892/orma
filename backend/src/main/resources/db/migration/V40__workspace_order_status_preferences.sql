alter table business_workspaces
    add column if not exists enabled_order_statuses text[] null;
