-- A member who was dismissed. Like INACTIVE and RESIGNED, it takes the
-- member out of the lists and turns their login off (the screens do that
-- when the status is saved).

INSERT INTO member_statuses (
    code,
    label_km,
    label_en,
    description,
    sort_order
)
VALUES (
    'FIRED',
    'ត្រូវបានបញ្ឈប់',
    'Fired',
    'The person was dismissed from membership.',
    5
)
ON CONFLICT (code) DO NOTHING;
