-- ============================================================
-- V27: Activity branch invitation and participant workflow
-- ============================================================

CREATE TABLE activity_invited_branches (
                                           id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                           activity_id BIGINT NOT NULL,
                                           branch_id BIGINT NOT NULL,

                                           invitation_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

                                           can_manage_attendance BOOLEAN NOT NULL DEFAULT FALSE,
                                           can_record_donation BOOLEAN NOT NULL DEFAULT FALSE,

                                           invited_by BIGINT NOT NULL,
                                           invited_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                           responded_by BIGINT,
                                           responded_at TIMESTAMPTZ,

                                           note TEXT,

                                           created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                           updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                           CONSTRAINT fk_activity_invited_branch_activity
                                               FOREIGN KEY (activity_id)
                                                   REFERENCES activities(id)
                                                   ON DELETE CASCADE,

                                           CONSTRAINT fk_activity_invited_branch_branch
                                               FOREIGN KEY (branch_id)
                                                   REFERENCES branches(id)
                                                   ON DELETE CASCADE,

                                           CONSTRAINT fk_activity_invited_branch_inviter
                                               FOREIGN KEY (invited_by)
                                                   REFERENCES users(id)
                                                   ON DELETE RESTRICT,

                                           CONSTRAINT fk_activity_invited_branch_responder
                                               FOREIGN KEY (responded_by)
                                                   REFERENCES users(id)
                                                   ON DELETE SET NULL,

                                           CONSTRAINT uq_activity_invited_branch
                                               UNIQUE (activity_id, branch_id),

                                           CONSTRAINT chk_activity_invitation_status
                                               CHECK (
                                                   invitation_status IN (
                                                                         'PENDING',
                                                                         'ACCEPTED',
                                                                         'DECLINED',
                                                                         'CANCELLED'
                                                       )
                                                   )
);

CREATE INDEX idx_activity_invited_branches_activity
    ON activity_invited_branches(activity_id);

CREATE INDEX idx_activity_invited_branches_branch
    ON activity_invited_branches(branch_id);

CREATE INDEX idx_activity_invited_branches_status
    ON activity_invited_branches(invitation_status);


ALTER TABLE activity_participants
    ADD COLUMN invited_branch_id BIGINT,
    ADD COLUMN checked_out_at TIMESTAMPTZ,
    ADD COLUMN registration_source VARCHAR(30)
        NOT NULL DEFAULT 'MANUAL';

ALTER TABLE activity_participants
    ADD CONSTRAINT fk_activity_participant_invited_branch
        FOREIGN KEY (invited_branch_id)
            REFERENCES activity_invited_branches(id)
            ON DELETE SET NULL;

ALTER TABLE activity_participants
    ADD CONSTRAINT chk_activity_participant_source
        CHECK (
            registration_source IN (
                                    'MANUAL',
                                    'HOST_BRANCH',
                                    'INVITED_BRANCH',
                                    'SELF_REGISTERED'
                )
            );

ALTER TABLE activity_participants
    ADD CONSTRAINT chk_activity_participant_checkout
        CHECK (
            checked_out_at IS NULL
                OR checked_in_at IS NULL
                OR checked_out_at >= checked_in_at
            );

CREATE INDEX idx_activity_participants_invited_branch
    ON activity_participants(invited_branch_id);