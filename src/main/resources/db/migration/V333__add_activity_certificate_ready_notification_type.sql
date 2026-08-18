--seed: notification_types — activity certificates ready for a co-hosting branch
--
-- Sent to a co-hosting branch's leader/secretary when the organizer branch's
-- secretary marks that activity's certificates as ready for that branch's
-- own members. Distinct from ACTIVITY_BRANCH_INVITATION (V328), which is
-- sent when the branch is first invited to co-host, and from
-- ACTIVITY_INVITATION (V13), which is sent to an individual MEMBER invited
-- to participate. Only the branch's leadership is notified here -- never
-- its individual members.
INSERT INTO notification_types (
    code,
    label_km,
    label_en,
    description,
    sort_order
)
VALUES
    (
        'ACTIVITY_CERTIFICATE_READY',
        'វិញ្ញាបនបត្រកម្មវិធីបានរួចរាល់សម្រាប់សាខា',
        'Activity Certificates Ready For Branch',
        'Sent to a co-hosting branch''s leadership when that branch''s members'' certificates from an activity are ready.',
        9
    )
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              description = EXCLUDED.description,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();
