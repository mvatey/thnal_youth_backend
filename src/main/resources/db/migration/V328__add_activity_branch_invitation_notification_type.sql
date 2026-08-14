--seed: notification_types — activity branch (co-host) invitation
--
-- Sent to a branch's leader/secretary when their branch is invited by
-- another branch's activity to co-host it. Distinct from ACTIVITY_INVITATION
-- (V13), which is sent to an individual MEMBER invited to participate.
INSERT INTO notification_types (
    code,
    label_km,
    label_en,
    description,
    sort_order
)
VALUES
    (
        'ACTIVITY_BRANCH_INVITATION',
        'ការអញ្ជើញសាខាចូលរួមរៀបចំកម្មវិធី',
        'Activity Branch Invitation',
        'Sent to a branch''s leadership when their branch is invited to co-host an activity.',
        8
    )
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              description = EXCLUDED.description,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();
