-- Phone becomes optional on standalone login accounts, as long as email is
-- present -- enforced at the application layer via CreateUserRequest /
-- UpdateUserRequest.isPhoneOrEmailPresent(). The existing chk_users_phone
-- format check already passes on NULL (a CHECK only evaluates non-null
-- values in Postgres), so nothing else needs to change here.
ALTER TABLE users
    ALTER COLUMN phone DROP NOT NULL;
