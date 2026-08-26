CREATE TABLE activity_daily_schedules (
    id BIGSERIAL PRIMARY KEY,
    activity_id BIGINT NOT NULL REFERENCES activities(id) ON DELETE CASCADE,
    schedule_date DATE NOT NULL,
    starts_at TIME NOT NULL,
    ends_at TIME NOT NULL,
    CONSTRAINT uq_activity_daily_schedule UNIQUE (activity_id, schedule_date),
    CONSTRAINT chk_activity_daily_schedule_times CHECK (ends_at > starts_at)
);
