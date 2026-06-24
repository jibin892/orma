alter table product_categories
    add column if not exists item_type text not null default 'all';

update product_categories
set item_type = 'all'
where item_type is null or item_type = '';

alter table product_categories
    drop constraint if exists product_categories_workspace_id_name_key;

create unique index if not exists product_categories_workspace_type_name_idx
    on product_categories(workspace_id, item_type, lower(name))
    where status = 'active';

create index if not exists product_categories_workspace_type_sort_idx
    on product_categories(workspace_id, item_type, sort_order, name)
    where status = 'active';
