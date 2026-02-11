-- Remove old composite constraint
ALTER TABLE users
DROP CONSTRAINT uq_users_email_tenant;

-- Add global unique constraint
ALTER TABLE users
ADD CONSTRAINT uq_users_email UNIQUE (email);