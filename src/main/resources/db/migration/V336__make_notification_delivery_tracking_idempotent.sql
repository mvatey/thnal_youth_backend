-- Keep at most one delivery outcome per notification recipient/channel.
-- Older deployments may already contain duplicate audit rows, so retain the
-- newest row before adding the uniqueness guarantee.
DELETE FROM notification_deliveries older
USING notification_deliveries newer
WHERE older.notification_id = newer.notification_id
  AND older.user_id = newer.user_id
  AND older.channel = newer.channel
  AND older.id < newer.id;

ALTER TABLE notification_deliveries
    ADD CONSTRAINT uq_notification_delivery_recipient_channel
        UNIQUE (notification_id, user_id, channel);
