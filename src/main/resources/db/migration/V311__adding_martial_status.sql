-- Vxxx__add_member_marital_status.sql

ALTER TABLE members
    ADD COLUMN marital_status VARCHAR(20);

ALTER TABLE members
    ADD CONSTRAINT chk_members_marital_status
        CHECK (
            marital_status IS NULL
                OR marital_status IN (
                                      'SINGLE',
                                      'MARRIED'
                )
            );