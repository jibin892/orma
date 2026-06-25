alter table workspace_members
    add column if not exists permissions text[] not null default '{}';

alter table team_invites
    add column if not exists permissions text[] not null default '{}';

update workspace_members
set permissions = case role
    when 'business_owner' then array[
        'create_sale',
        'edit_sale',
        'change_booking_status',
        'create_product',
        'create_service',
        'create_appointment',
        'create_offer',
        'manage_stock',
        'manage_customers',
        'download_invoice',
        'read_only'
    ]
    when 'manager' then array[
        'create_sale',
        'edit_sale',
        'change_booking_status',
        'create_product',
        'create_service',
        'create_appointment',
        'create_offer',
        'manage_stock',
        'manage_customers',
        'download_invoice'
    ]
    when 'cashier' then array['create_sale', 'change_booking_status', 'download_invoice']
    when 'accountant' then array['download_invoice']
    when 'inventory_manager' then array[
        'create_product',
        'create_service',
        'create_appointment',
        'create_offer',
        'manage_stock'
    ]
    when 'sales_staff' then array[
        'create_sale',
        'edit_sale',
        'change_booking_status',
        'manage_customers',
        'download_invoice'
    ]
    when 'read_only' then array['read_only']
    else array['read_only']
end
where permissions = '{}';

update team_invites
set permissions = case role
    when 'business_owner' then array[
        'create_sale',
        'edit_sale',
        'change_booking_status',
        'create_product',
        'create_service',
        'create_appointment',
        'create_offer',
        'manage_stock',
        'manage_customers',
        'download_invoice',
        'read_only'
    ]
    when 'manager' then array[
        'create_sale',
        'edit_sale',
        'change_booking_status',
        'create_product',
        'create_service',
        'create_appointment',
        'create_offer',
        'manage_stock',
        'manage_customers',
        'download_invoice'
    ]
    when 'cashier' then array['create_sale', 'change_booking_status', 'download_invoice']
    when 'accountant' then array['download_invoice']
    when 'inventory_manager' then array[
        'create_product',
        'create_service',
        'create_appointment',
        'create_offer',
        'manage_stock'
    ]
    when 'sales_staff' then array[
        'create_sale',
        'edit_sale',
        'change_booking_status',
        'manage_customers',
        'download_invoice'
    ]
    when 'read_only' then array['read_only']
    else array['read_only']
end
where permissions = '{}';
