alter table meta_webhook_events
    add column if not exists external_message_id text,
    add column if not exists converted_order_id uuid references orders(id) on delete set null,
    add column if not exists processing_error text;

create unique index if not exists idx_meta_webhook_events_workspace_external_message
    on meta_webhook_events(workspace_id, external_message_id)
    where workspace_id is not null and external_message_id is not null;
