alter table printer_profiles
    add column if not exists print_logo boolean not null default false,
    add column if not exists header_alignment text not null default 'center',
    add column if not exists show_business_address boolean not null default true,
    add column if not exists show_catalog_qr boolean not null default false,
    add column if not exists show_timed_greeting boolean not null default true;
