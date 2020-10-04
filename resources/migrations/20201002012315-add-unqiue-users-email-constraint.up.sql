ALTER TABLE users
ADD CONSTRAINT unique_users_email
UNIQUE USING INDEX users_email;
