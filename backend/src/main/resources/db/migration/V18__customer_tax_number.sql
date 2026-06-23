alter table customers
    add column if not exists tax_number text;

create index if not exists customers_workspace_tax_number_idx
    on customers(workspace_id, tax_number)
    where tax_number is not null;
