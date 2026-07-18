--21. provinces
CREATE TABLE provinces (
                           id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                           code VARCHAR(20) NOT NULL UNIQUE,

                           name_km VARCHAR(100) NOT NULL,
                           name_en VARCHAR(100),

                           description VARCHAR(255),

                           is_active BOOLEAN NOT NULL DEFAULT TRUE,

                           created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                           CONSTRAINT chk_province_code
                               CHECK (BTRIM(code) <> ''),

                           CONSTRAINT chk_province_name_km
                               CHECK (BTRIM(name_km) <> ''),

                           CONSTRAINT uq_province_name_km
                               UNIQUE (name_km),

                           CONSTRAINT uq_province_name_en
                               UNIQUE (name_en)
);

--22. districts
CREATE TABLE districts (
                           id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                           code VARCHAR(20) NOT NULL UNIQUE,

                           province_id SMALLINT NOT NULL,

                           name_km VARCHAR(100) NOT NULL,
                           name_en VARCHAR(100),

                           description VARCHAR(255),

                           is_active BOOLEAN NOT NULL DEFAULT TRUE,

                           created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                           CONSTRAINT fk_district_province
                               FOREIGN KEY (province_id)
                                   REFERENCES provinces(id)
                                   ON DELETE RESTRICT,

                           CONSTRAINT chk_district_code
                               CHECK (BTRIM(code) <> ''),

                           CONSTRAINT chk_district_name_km
                               CHECK (BTRIM(name_km) <> ''),

                           CONSTRAINT uq_district_name_km
                               UNIQUE (province_id, name_km)
);

--23. communes
CREATE TABLE communes (
                          id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                          code VARCHAR(20) NOT NULL UNIQUE,

                          district_id INT NOT NULL,

                          name_km VARCHAR(100) NOT NULL,
                          name_en VARCHAR(100),

                          description VARCHAR(255),

                          is_active BOOLEAN NOT NULL DEFAULT TRUE,

                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                          CONSTRAINT fk_commune_district
                              FOREIGN KEY (district_id)
                                  REFERENCES districts(id)
                                  ON DELETE RESTRICT,

                          CONSTRAINT chk_commune_code
                              CHECK (BTRIM(code) <> ''),

                          CONSTRAINT chk_commune_name_km
                              CHECK (BTRIM(name_km) <> ''),

                          CONSTRAINT uq_commune_name_km
                              UNIQUE (district_id, name_km)
);

--indexes
CREATE INDEX idx_districts_province_id
    ON districts(province_id);

CREATE INDEX idx_communes_district_id
    ON communes(district_id);