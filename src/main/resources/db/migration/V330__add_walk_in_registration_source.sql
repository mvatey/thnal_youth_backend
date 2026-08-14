-- Allow recording a host-branch member's attendance without a prior
-- formal invite/registration step ("walk-in"), so staff can mark
-- real-world participation for members who weren't formally divided
-- or invited ahead of time. See ParticipantRegistrationSource.WALK_IN.

ALTER TABLE activity_participants
    DROP CONSTRAINT chk_activity_participant_source;

ALTER TABLE activity_participants
    ADD CONSTRAINT chk_activity_participant_source
        CHECK (
            registration_source IN (
                                    'MANUAL',
                                    'HOST_BRANCH',
                                    'INVITED_BRANCH',
                                    'SELF_REGISTERED',
                                    'WALK_IN'
                )
            );
