create unique index if not exists idx_meta_connections_phone_number_unique
    on meta_connections(phone_number_id)
    where phone_number_id is not null and btrim(phone_number_id) <> '';
