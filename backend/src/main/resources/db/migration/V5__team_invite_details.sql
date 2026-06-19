alter table team_invites
    add column if not exists invitee_name text,
    add column if not exists invitee_email text,
    add column if not exists invitee_phone_number text;

create index if not exists team_invites_invitee_email_idx
    on team_invites(invitee_email)
    where invitee_email is not null;

create index if not exists team_invites_invitee_phone_number_idx
    on team_invites(invitee_phone_number)
    where invitee_phone_number is not null;
