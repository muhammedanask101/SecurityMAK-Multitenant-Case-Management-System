ALTER TABLE users
ADD COLUMN joined_via_invite_id BIGINT;

ALTER TABLE users
ADD CONSTRAINT fk_users_joined_via_invite
FOREIGN KEY (joined_via_invite_id)
REFERENCES invites(id)
ON DELETE SET NULL;