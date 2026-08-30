-- Lets the "document issued" notification's rich email/Telegram builders
-- re-fetch the Document entity (for its title) at send time, the same way
-- the activity-related notification types already re-fetch via
-- notifications.activity_id. Nullable and unused by every other type.
ALTER TABLE notifications
    ADD COLUMN document_id BIGINT REFERENCES documents(id) ON DELETE SET NULL;
