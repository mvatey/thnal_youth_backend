CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

--1. roles
CREATE TABLE roles (
                       id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                       code VARCHAR(50) NOT NULL UNIQUE,

                       label_km VARCHAR(100) NOT NULL,
                       label_en VARCHAR(100),

                       description TEXT,

                       is_active BOOLEAN NOT NULL DEFAULT TRUE,
                       sort_order INT NOT NULL DEFAULT 0,

                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                       CONSTRAINT chk_role_code_not_blank
                           CHECK (BTRIM(code) <> ''),

                       CONSTRAINT chk_role_label_km_not_blank
                           CHECK (BTRIM(label_km) <> ''),

                       CONSTRAINT chk_role_sort_order
                           CHECK (sort_order >= 0)
);

--2. account_statuses
CREATE TABLE account_statuses (
                                  id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                  code VARCHAR(50) NOT NULL UNIQUE,

                                  label_km VARCHAR(100) NOT NULL,
                                  label_en VARCHAR(100),

                                  description TEXT,

                                  is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                  sort_order INT NOT NULL DEFAULT 0,

                                  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                  CONSTRAINT chk_account_status_code_not_blank
                                      CHECK (BTRIM(code) <> ''),

                                  CONSTRAINT chk_account_status_label_km_not_blank
                                      CHECK (BTRIM(label_km) <> ''),

                                  CONSTRAINT chk_account_status_sort_order
                                      CHECK (sort_order >= 0)
);

--3. member_statuses
CREATE TABLE member_statuses (
                                 id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                 code VARCHAR(30) NOT NULL UNIQUE,

                                 label_km VARCHAR(100) NOT NULL,
                                 label_en VARCHAR(100),

                                 description TEXT,

                                 is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                 sort_order INT NOT NULL DEFAULT 0,

                                 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                 CONSTRAINT chk_member_status_code
                                     CHECK (BTRIM(code) <> ''),

                                 CONSTRAINT chk_member_status_label_km
                                     CHECK (BTRIM(label_km) <> ''),

                                 CONSTRAINT chk_member_status_sort_order
                                     CHECK (sort_order >= 0)
);

--4. religions
CREATE TABLE religions (
                           id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                           code VARCHAR(30) NOT NULL UNIQUE,

                           label_km VARCHAR(100) NOT NULL,
                           label_en VARCHAR(100),

                           description TEXT,

                           is_active BOOLEAN NOT NULL DEFAULT TRUE,
                           sort_order INT NOT NULL DEFAULT 0,

                           created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                           CONSTRAINT chk_religion_code
                               CHECK (BTRIM(code) <> ''),

                           CONSTRAINT chk_religion_label_km
                               CHECK (BTRIM(label_km) <> ''),

                           CONSTRAINT chk_religion_sort_order
                               CHECK (sort_order >= 0)
);

--5. member_levels
CREATE TABLE member_levels (
                               id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                               code VARCHAR(30) NOT NULL UNIQUE,

                               label_km VARCHAR(100) NOT NULL,
                               label_en VARCHAR(100),

                               description TEXT,

                               is_active BOOLEAN NOT NULL DEFAULT TRUE,
                               sort_order INT NOT NULL DEFAULT 0,

                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                               CONSTRAINT chk_member_level_code
                                   CHECK (BTRIM(code) <> ''),

                               CONSTRAINT chk_member_level_label_km
                                   CHECK (BTRIM(label_km) <> ''),

                               CONSTRAINT chk_member_level_sort_order
                                   CHECK (sort_order >= 0)
);

--6. employment_sectors
CREATE TABLE employment_sectors (
                                    id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                    code VARCHAR(50) NOT NULL UNIQUE,

                                    label_km VARCHAR(100) NOT NULL,
                                    label_en VARCHAR(100),

                                    description TEXT,

                                    is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                    sort_order INT NOT NULL DEFAULT 0,

                                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                    CONSTRAINT chk_employment_sector_code
                                        CHECK (BTRIM(code) <> ''),

                                    CONSTRAINT chk_employment_sector_label
                                        CHECK (BTRIM(label_km) <> ''),

                                    CONSTRAINT chk_employment_sector_sort_order
                                        CHECK (sort_order >= 0)
);

--7. education_levels
CREATE TABLE education_levels (
                                  id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                  code VARCHAR(50) NOT NULL UNIQUE,

                                  label_km VARCHAR(100) NOT NULL,
                                  label_en VARCHAR(100),

                                  description TEXT,

                                  is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                  sort_order INT NOT NULL DEFAULT 0,

                                  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                  CONSTRAINT chk_education_level_code
                                      CHECK (BTRIM(code) <> ''),

                                  CONSTRAINT chk_education_level_label
                                      CHECK (BTRIM(label_km) <> ''),

                                  CONSTRAINT chk_education_level_sort_order
                                      CHECK (sort_order >= 0)
);

--8. proficiency_levels
CREATE TABLE proficiency_levels (
                                    id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                    code VARCHAR(30) NOT NULL UNIQUE,

                                    label_km VARCHAR(100) NOT NULL,
                                    label_en VARCHAR(100) NOT NULL,

                                    description TEXT,

                                    is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                    sort_order INT NOT NULL DEFAULT 0,

                                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                    CONSTRAINT chk_proficiency_level_code
                                        CHECK (BTRIM(code) <> ''),

                                    CONSTRAINT chk_proficiency_level_label_km
                                        CHECK (BTRIM(label_km) <> ''),

                                    CONSTRAINT chk_proficiency_level_label_en
                                        CHECK (BTRIM(label_en) <> ''),

                                    CONSTRAINT chk_proficiency_level_sort_order
                                        CHECK (sort_order >= 0)
);

--9. branch_levels
CREATE TABLE branch_levels (
                               id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                               code VARCHAR(30) NOT NULL UNIQUE,

                               label_km VARCHAR(100) NOT NULL,
                               label_en VARCHAR(100) NOT NULL,

                               hierarchy_order SMALLINT NOT NULL,

                               description VARCHAR(255),

                               is_active BOOLEAN NOT NULL DEFAULT TRUE,
                               sort_order INT NOT NULL DEFAULT 0,

                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                               CONSTRAINT uq_branch_level_hierarchy_order
                                   UNIQUE (hierarchy_order),

                               CONSTRAINT chk_branch_level_code
                                   CHECK (BTRIM(code) <> ''),

                               CONSTRAINT chk_branch_level_label_km
                                   CHECK (BTRIM(label_km) <> ''),

                               CONSTRAINT chk_branch_level_label_en
                                   CHECK (BTRIM(label_en) <> ''),

                               CONSTRAINT chk_branch_level_hierarchy_order
                                   CHECK (hierarchy_order > 0),

                               CONSTRAINT chk_branch_level_sort_order
                                   CHECK (sort_order >= 0)
);

--10. branch_statuses
CREATE TABLE branch_statuses (
                                 id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                 code VARCHAR(30) NOT NULL UNIQUE,

                                 label_km VARCHAR(100) NOT NULL,
                                 label_en VARCHAR(100),

                                 description VARCHAR(255),

                                 is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                 sort_order INT NOT NULL DEFAULT 0,

                                 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                 CONSTRAINT chk_branch_status_code
                                     CHECK (BTRIM(code) <> ''),

                                 CONSTRAINT chk_branch_status_label_km
                                     CHECK (BTRIM(label_km) <> ''),

                                 CONSTRAINT chk_branch_status_sort_order
                                     CHECK (sort_order >= 0)
);

--11. positions
CREATE TABLE positions (
                           id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                           code VARCHAR(50) NOT NULL UNIQUE,

                           label_km VARCHAR(100) NOT NULL,
                           label_en VARCHAR(100),

                           description TEXT,

                           is_active BOOLEAN NOT NULL DEFAULT TRUE,
                           sort_order INT NOT NULL DEFAULT 0,

                           created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                           CONSTRAINT chk_position_code
                               CHECK (BTRIM(code) <> ''),

                           CONSTRAINT chk_position_label_km
                               CHECK (BTRIM(label_km) <> ''),

                           CONSTRAINT chk_position_sort_order
                               CHECK (sort_order >= 0)

);

--12. activity_types
CREATE TABLE activity_types (
                                id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                code VARCHAR(50) NOT NULL UNIQUE,

                                label_km VARCHAR(100) NOT NULL,
                                label_en VARCHAR(100),

                                description TEXT,

                                is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                sort_order INT NOT NULL DEFAULT 0,

                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                CONSTRAINT chk_activity_type_code
                                    CHECK (BTRIM(code) <> ''),

                                CONSTRAINT chk_activity_type_label_km
                                    CHECK (BTRIM(label_km) <> ''),

                                CONSTRAINT chk_activity_type_sort_order
                                    CHECK (sort_order >= 0)
);

--13. activity_sectors
CREATE TABLE activity_sectors (
                                  id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                  code VARCHAR(50) NOT NULL UNIQUE,

                                  label_km VARCHAR(100) NOT NULL,
                                  label_en VARCHAR(100),

                                  description TEXT,

                                  is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                  sort_order INT NOT NULL DEFAULT 0,

                                  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                  CONSTRAINT chk_activity_sector_code
                                      CHECK (BTRIM(code) <> ''),

                                  CONSTRAINT chk_activity_sector_label_km
                                      CHECK (BTRIM(label_km) <> ''),

                                  CONSTRAINT chk_activity_sector_sort_order
                                      CHECK (sort_order >= 0)
);

--14. activity_statuses
CREATE TABLE activity_statuses (
                                   id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                   code VARCHAR(50) NOT NULL UNIQUE,

                                   label_km VARCHAR(100) NOT NULL,
                                   label_en VARCHAR(100),

                                   description TEXT,

                                   is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                   sort_order INT NOT NULL DEFAULT 0,

                                   created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                   updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                   CONSTRAINT chk_activity_status_code
                                       CHECK (BTRIM(code) <> ''),

                                   CONSTRAINT chk_activity_status_label_km
                                       CHECK (BTRIM(label_km) <> ''),

                                   CONSTRAINT chk_activity_status_sort_order
                                       CHECK (sort_order >= 0)
);

--15. attendance_statuses
CREATE TABLE attendance_statuses (
                                     id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                     code VARCHAR(50) NOT NULL UNIQUE,

                                     label_km VARCHAR(50) NOT NULL,
                                     label_en VARCHAR(50),

                                     description TEXT,

                                     is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                     sort_order INT NOT NULL DEFAULT 0,

                                     created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                     updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                     CONSTRAINT chk_attendance_status_code
                                         CHECK (BTRIM(code) <> ''),

                                     CONSTRAINT chk_attendance_status_label_km
                                         CHECK (BTRIM(label_km) <> ''),

                                     CONSTRAINT chk_attendance_status_sort_order
                                         CHECK (sort_order >= 0)
);

--16. donation_types
CREATE TABLE donation_types (
                                id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                code VARCHAR(30) NOT NULL UNIQUE,

                                label_km VARCHAR(100) NOT NULL,
                                label_en VARCHAR(100),

                                description TEXT,

                                is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                sort_order INT NOT NULL DEFAULT 0,

                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                CONSTRAINT chk_donation_type_code
                                    CHECK (BTRIM(code) <> ''),

                                CONSTRAINT chk_donation_type_label_km
                                    CHECK (BTRIM(label_km) <> ''),

                                CONSTRAINT chk_donation_type_sort_order
                                    CHECK (sort_order >= 0)
);

--17. payment_methods
CREATE TABLE payment_methods (
                                 id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                 code VARCHAR(30) NOT NULL UNIQUE,

                                 label_km VARCHAR(100) NOT NULL,
                                 label_en VARCHAR(100) NOT NULL,

                                 description TEXT,

                                 is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                 sort_order INT NOT NULL DEFAULT 0,

                                 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                 CONSTRAINT chk_payment_method_code
                                     CHECK (BTRIM(code) <> ''),

                                 CONSTRAINT chk_payment_method_label_km
                                     CHECK (BTRIM(label_km) <> ''),

                                 CONSTRAINT chk_payment_method_label_en
                                     CHECK (BTRIM(label_en) <> ''),

                                 CONSTRAINT chk_payment_method_sort_order
                                     CHECK (sort_order >= 0)
);

--18. sponsor_types
CREATE TABLE sponsor_types (
                               id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                               code VARCHAR(30) NOT NULL UNIQUE,

                               label_km VARCHAR(100) NOT NULL,
                               label_en VARCHAR(100) NOT NULL,

                               description TEXT,

                               is_active BOOLEAN NOT NULL DEFAULT TRUE,
                               sort_order INT NOT NULL DEFAULT 0,

                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                               CONSTRAINT chk_sponsor_type_code
                                   CHECK (BTRIM(code) <> ''),

                               CONSTRAINT chk_sponsor_type_label_km
                                   CHECK (BTRIM(label_km) <> ''),

                               CONSTRAINT chk_sponsor_type_label_en
                                   CHECK (BTRIM(label_en) <> ''),

                               CONSTRAINT chk_sponsor_type_sort_order
                                   CHECK (sort_order >= 0)
);

--19. document_types
CREATE TABLE document_types (
                                id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                code VARCHAR(50) NOT NULL UNIQUE,

                                label_km VARCHAR(100) NOT NULL,
                                label_en VARCHAR(100) NOT NULL,

                                description TEXT,

                                is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                sort_order INT NOT NULL DEFAULT 0,

                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                CONSTRAINT chk_document_type_code
                                    CHECK (BTRIM(code) <> ''),

                                CONSTRAINT chk_document_type_label_km
                                    CHECK (BTRIM(label_km) <> ''),

                                CONSTRAINT chk_document_type_label_en
                                    CHECK (BTRIM(label_en) <> ''),

                                CONSTRAINT chk_document_type_sort_order
                                    CHECK (sort_order >= 0)
);

--20. notification_types
CREATE TABLE notification_types (
                                    id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                    code VARCHAR(50) NOT NULL UNIQUE,

                                    label_km VARCHAR(100) NOT NULL,
                                    label_en VARCHAR(100),

                                    description TEXT,

                                    is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                    sort_order INT NOT NULL DEFAULT 0,

                                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                    CONSTRAINT chk_notification_type_code
                                        CHECK (BTRIM(code) <> ''),

                                    CONSTRAINT chk_notification_type_label_km
                                        CHECK (BTRIM(label_km) <> ''),

                                    CONSTRAINT chk_notification_type_sort_order
                                        CHECK (sort_order >= 0)
);