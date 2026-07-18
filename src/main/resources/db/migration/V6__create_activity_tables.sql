--31. activities
CREATE TABLE activities (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                            title_km VARCHAR(255) NOT NULL,
                            title_en VARCHAR(255),
                            description TEXT,

                            type_id SMALLINT NOT NULL,
                            sector_id SMALLINT NOT NULL,
                            status_id SMALLINT NOT NULL,
                            branch_id BIGINT NOT NULL,

                            is_public BOOLEAN NOT NULL DEFAULT FALSE,

                            starts_at TIMESTAMPTZ NOT NULL,
                            ends_at TIMESTAMPTZ NOT NULL,

                            province_id SMALLINT,
                            district_id INTEGER,
                            commune_id INTEGER,

                            location_name VARCHAR(255),
                            address TEXT,
                            google_map_url TEXT,

                            capacity INTEGER,

                            cover_image_id BIGINT,

                            created_by BIGINT NOT NULL,

                            created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                            updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                            CONSTRAINT fk_activity_type
                                FOREIGN KEY (type_id)
                                    REFERENCES activity_types(id)
                                    ON DELETE RESTRICT,

                            CONSTRAINT fk_activity_sector
                                FOREIGN KEY (sector_id)
                                    REFERENCES activity_sectors(id)
                                    ON DELETE RESTRICT,

                            CONSTRAINT fk_activity_status
                                FOREIGN KEY (status_id)
                                    REFERENCES activity_statuses(id)
                                    ON DELETE RESTRICT,

                            CONSTRAINT fk_activity_branch
                                FOREIGN KEY (branch_id)
                                    REFERENCES branches(id)
                                    ON DELETE RESTRICT,

                            CONSTRAINT fk_activity_province
                                FOREIGN KEY (province_id)
                                    REFERENCES provinces(id)
                                    ON DELETE RESTRICT,

                            CONSTRAINT fk_activity_district
                                FOREIGN KEY (district_id)
                                    REFERENCES districts(id)
                                    ON DELETE RESTRICT,

                            CONSTRAINT fk_activity_commune
                                FOREIGN KEY (commune_id)
                                    REFERENCES communes(id)
                                    ON DELETE RESTRICT,

                            CONSTRAINT fk_activity_cover_image
                                FOREIGN KEY (cover_image_id)
                                    REFERENCES files(id)
                                    ON DELETE SET NULL,

                            CONSTRAINT fk_activity_creator
                                FOREIGN KEY (created_by)
                                    REFERENCES users(id)
                                    ON DELETE RESTRICT,

                            CONSTRAINT chk_activity_title_km
                                CHECK (BTRIM(title_km) <> ''),

                            CONSTRAINT chk_activity_time
                                CHECK (ends_at > starts_at),

                            CONSTRAINT chk_activity_capacity
                                CHECK (
                                    capacity IS NULL
                                        OR capacity > 0
                                    ),

                            CONSTRAINT chk_activity_location_selection
                                CHECK (
                                    commune_id IS NULL
                                        OR district_id IS NOT NULL
                                    )
);

CREATE INDEX idx_activities_branch_id
    ON activities(branch_id);

CREATE INDEX idx_activities_status_id
    ON activities(status_id);

CREATE INDEX idx_activities_type_id
    ON activities(type_id);

CREATE INDEX idx_activities_sector_id
    ON activities(sector_id);

CREATE INDEX idx_activities_starts_at
    ON activities(starts_at);

CREATE INDEX idx_activities_public_starts_at
    ON activities(starts_at)
    WHERE is_public = TRUE;


--32. activity_expenses
CREATE TABLE activity_expenses (
                                   id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                   activity_id BIGINT NOT NULL,

                                   name VARCHAR(255) NOT NULL,
                                   description TEXT,

                                   quantity NUMERIC(10,2) NOT NULL DEFAULT 1,
                                   unit_price NUMERIC(14,2) NOT NULL,

                                   currency CHAR(3) NOT NULL,

                                   total_amount NUMERIC(14,2) NOT NULL DEFAULT 0,

                                   spent_on DATE,

                                   receipt_file_id BIGINT,

                                   recorded_by BIGINT NOT NULL,

                                   created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                   updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                   CONSTRAINT fk_activity_expense_activity
                                       FOREIGN KEY (activity_id)
                                           REFERENCES activities(id)
                                           ON DELETE CASCADE,

                                   CONSTRAINT fk_activity_expense_receipt
                                       FOREIGN KEY (receipt_file_id)
                                           REFERENCES files(id)
                                           ON DELETE SET NULL,

                                   CONSTRAINT fk_activity_expense_recorder
                                       FOREIGN KEY (recorded_by)
                                           REFERENCES users(id)
                                           ON DELETE RESTRICT,

                                   CONSTRAINT chk_activity_expense_name
                                       CHECK (BTRIM(name) <> ''),

                                   CONSTRAINT chk_activity_expense_quantity
                                       CHECK (quantity > 0),

                                   CONSTRAINT chk_activity_expense_unit_price
                                       CHECK (unit_price >= 0),

                                   CONSTRAINT chk_activity_expense_total
                                       CHECK (total_amount >= 0),

                                   CONSTRAINT chk_activity_expense_currency
                                       CHECK (
                                           currency IN ('KHR', 'USD')
                                           )
);

CREATE INDEX idx_activity_expenses_activity_id
    ON activity_expenses(activity_id);

CREATE INDEX idx_activity_expenses_spent_on
    ON activity_expenses(spent_on);

CREATE INDEX idx_activity_expenses_recorded_by
    ON activity_expenses(recorded_by);


--33. activity_photos
CREATE TABLE activity_photos (
                                 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                 activity_id BIGINT NOT NULL,
                                 file_id BIGINT NOT NULL,

                                 caption TEXT,
                                 sort_order INT NOT NULL DEFAULT 0,

                                 uploaded_by BIGINT NOT NULL,
                                 uploaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                 CONSTRAINT fk_activity_photo_activity
                                     FOREIGN KEY (activity_id)
                                         REFERENCES activities(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT fk_activity_photo_file
                                     FOREIGN KEY (file_id)
                                         REFERENCES files(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT fk_activity_photo_uploader
                                     FOREIGN KEY (uploaded_by)
                                         REFERENCES users(id)
                                         ON DELETE RESTRICT,

                                 CONSTRAINT uq_activity_photo_file
                                     UNIQUE (activity_id, file_id),

                                 CONSTRAINT chk_activity_photo_sort_order
                                     CHECK (sort_order >= 0)
);

CREATE INDEX idx_activity_photos_activity_sort
    ON activity_photos(activity_id, sort_order);


--34. activity_participants
CREATE TABLE activity_participants (
                                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                       activity_id BIGINT NOT NULL,
                                       member_id BIGINT NOT NULL,

                                       attendance_status_id SMALLINT,

                                       registered_at TIMESTAMPTZ,
                                       checked_in_at TIMESTAMPTZ,

                                       invited_by BIGINT,

                                       note TEXT,

                                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                       updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                       CONSTRAINT fk_activity_participant_activity
                                           FOREIGN KEY (activity_id)
                                               REFERENCES activities(id)
                                               ON DELETE CASCADE,

                                       CONSTRAINT fk_activity_participant_member
                                           FOREIGN KEY (member_id)
                                               REFERENCES members(id)
                                               ON DELETE CASCADE,

                                       CONSTRAINT fk_activity_participant_inviter
                                           FOREIGN KEY (invited_by)
                                               REFERENCES users(id)
                                               ON DELETE SET NULL,

                                       CONSTRAINT fk_activity_participant_attendance
                                           FOREIGN KEY (attendance_status_id)
                                               REFERENCES attendance_statuses(id)
                                               ON DELETE RESTRICT,

                                       CONSTRAINT uq_activity_participant
                                           UNIQUE (activity_id, member_id),

                                       CONSTRAINT chk_activity_participant_check_in
                                           CHECK (
                                               checked_in_at IS NULL
                                                   OR registered_at IS NULL
                                                   OR checked_in_at >= registered_at
                                               )
);

CREATE INDEX idx_activity_participants_activity_id
    ON activity_participants(activity_id);

CREATE INDEX idx_activity_participants_member_id
    ON activity_participants(member_id);

CREATE INDEX idx_activity_participants_attendance_status
    ON activity_participants(attendance_status_id);