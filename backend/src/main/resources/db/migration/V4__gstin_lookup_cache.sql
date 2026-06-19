create table if not exists gstin_lookups (
    id uuid primary key default gen_random_uuid(),
    gstin text not null unique,
    flag boolean not null,
    message text not null,
    legal_name text,
    trade_name text,
    taxpayer_status text,
    taxpayer_type text,
    constitution text,
    registration_date text,
    cancellation_date text,
    principal_address text,
    state_jurisdiction text,
    central_jurisdiction text,
    response_data jsonb,
    raw_response jsonb not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists gstin_lookups_legal_name_idx
    on gstin_lookups(legal_name);

create index if not exists gstin_lookups_trade_name_idx
    on gstin_lookups(trade_name);
