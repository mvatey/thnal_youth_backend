--29. branch_staff
CREATE TABLE branch_staff (
                              id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                              branch_id BIGINT NOT NULL,
                              member_id BIGINT NOT NULL,
                              position_id SMALLINT NOT NULL,

                              started_on DATE NOT NULL,
                              ended_on DATE,

                              is_primary BOOLEAN NOT NULL DEFAULT FALSE,

                              appointed_by BIGINT,

                              created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                              updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                              CONSTRAINT fk_branch_staff_branch
                                  FOREIGN KEY (branch_id)
                                      REFERENCES branches(id)
                                      ON DELETE CASCADE,

                              CONSTRAINT fk_branch_staff_member
                                  FOREIGN KEY (member_id)
                                      REFERENCES members(id)
                                      ON DELETE CASCADE,

                              CONSTRAINT fk_branch_staff_position
                                  FOREIGN KEY (position_id)
                                      REFERENCES positions(id)
                                      ON DELETE RESTRICT,

                              CONSTRAINT fk_branch_staff_appointed_by
                                  FOREIGN KEY (appointed_by)
                                      REFERENCES users(id)
                                      ON DELETE SET NULL,

                              CONSTRAINT chk_branch_staff_dates
                                  CHECK (
                                      ended_on IS NULL
                                          OR ended_on >= started_on
                                      )

);

CREATE INDEX idx_branch_staff_branch_id
    ON branch_staff(branch_id);

CREATE INDEX idx_branch_staff_member_id
    ON branch_staff(member_id);

CREATE INDEX idx_branch_staff_position_id
    ON branch_staff(position_id);

CREATE INDEX idx_branch_staff_current
    ON branch_staff(branch_id, position_id)
    WHERE ended_on IS NULL;

CREATE UNIQUE INDEX uq_branch_staff_active_assignment
    ON branch_staff(branch_id, member_id, position_id)
    WHERE ended_on IS NULL;

CREATE UNIQUE INDEX uq_branch_primary_position
    ON branch_staff(branch_id, position_id)
    WHERE ended_on IS NULL
      AND is_primary = TRUE;

--30. sponsors
CREATE TABLE sponsors (
                          id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                          sponsor_type_id SMALLINT NOT NULL,

                          name VARCHAR(255) NOT NULL,

                          phone VARCHAR(30),
                          email CITEXT,
                          address TEXT,

                          note TEXT,

                          is_active BOOLEAN NOT NULL DEFAULT TRUE,

                          created_by BIGINT,

                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                          CONSTRAINT fk_sponsor_type
                              FOREIGN KEY (sponsor_type_id)
                                  REFERENCES sponsor_types(id)
                                  ON DELETE RESTRICT,

                          CONSTRAINT fk_sponsor_created_by
                              FOREIGN KEY (created_by)
                                  REFERENCES users(id)
                                  ON DELETE SET NULL,

                          CONSTRAINT chk_sponsor_name
                              CHECK (BTRIM(name) <> ''),

                          CONSTRAINT chk_sponsor_phone
                              CHECK (
                                  phone IS NULL
                                      OR BTRIM(phone) <> ''
                                  ),

                          CONSTRAINT chk_sponsor_email
                              CHECK (
                                  email IS NULL
                                      OR BTRIM(email::TEXT) <> ''
                                  )
);

CREATE INDEX idx_sponsors_type_id
    ON sponsors(sponsor_type_id);

CREATE INDEX idx_sponsors_active
    ON sponsors(is_active);

CREATE INDEX idx_sponsors_name
    ON sponsors(LOWER(name));