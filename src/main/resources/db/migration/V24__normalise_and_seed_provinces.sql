-- Normalise and seed Cambodia provinces/capital using official codes.
-- This migration is required because V23 was repaired after its contents
-- changed, and Flyway repair does not rerun an already applied migration.

DO $$
    DECLARE
        r RECORD;
        target_id SMALLINT;
    BEGIN
        FOR r IN
            SELECT *
            FROM (
                     VALUES
                         ('01', 'បន្ទាយមានជ័យ', 'Banteay Meanchey'),
                         ('02', 'បាត់ដំបង', 'Battambang'),
                         ('03', 'កំពង់ចាម', 'Kampong Cham'),
                         ('04', 'កំពង់ឆ្នាំង', 'Kampong Chhnang'),
                         ('05', 'កំពង់ស្ពឺ', 'Kampong Speu'),
                         ('06', 'កំពង់ធំ', 'Kampong Thom'),
                         ('07', 'កំពត', 'Kampot'),
                         ('08', 'កណ្ដាល', 'Kandal'),
                         ('09', 'កោះកុង', 'Koh Kong'),
                         ('10', 'ក្រចេះ', 'Kratie'),
                         ('11', 'មណ្ឌលគិរី', 'Mondul Kiri'),
                         ('12', 'រាជធានីភ្នំពេញ', 'Phnom Penh'),
                         ('13', 'ព្រះវិហារ', 'Preah Vihear'),
                         ('14', 'ព្រៃវែង', 'Prey Veng'),
                         ('15', 'ពោធិ៍សាត់', 'Pursat'),
                         ('16', 'រតនគិរី', 'Ratanak Kiri'),
                         ('17', 'សៀមរាប', 'Siemreap'),
                         ('18', 'ព្រះសីហនុ', 'Sihanoukville'),
                         ('19', 'ស្ទឹងត្រែង', 'Stung Treng'),
                         ('20', 'ស្វាយរៀង', 'Svay Rieng'),
                         ('21', 'តាកែវ', 'Takeo'),
                         ('22', 'ឧត្ដរមានជ័យ', 'Otdar Meanchey'),
                         ('23', 'កែប', 'Kep'),
                         ('24', 'ប៉ៃលិន', 'Pailin'),
                         ('25', 'ត្បូងឃ្មុំ', 'Tboung Khmum')
                 ) AS seed(code, name_km, name_en)
            LOOP
                target_id := NULL;

                -- Match an existing row by official code.
                SELECT p.id
                INTO target_id
                FROM provinces p
                WHERE p.code = r.code
                LIMIT 1;

                -- Otherwise match manually inserted rows by English name.
                IF target_id IS NULL THEN
                    SELECT p.id
                    INTO target_id
                    FROM provinces p
                    WHERE LOWER(TRIM(p.name_en)) =
                          LOWER(TRIM(r.name_en))
                    ORDER BY p.id
                    LIMIT 1;
                END IF;

                -- Otherwise match by Khmer name, including common prefixes.
                IF target_id IS NULL THEN
                    SELECT p.id
                    INTO target_id
                    FROM provinces p
                    WHERE TRIM(p.name_km) = TRIM(r.name_km)
                       OR TRIM(p.name_km) =
                          TRIM('ខេត្ត' || r.name_km)
                       OR TRIM(p.name_km) =
                          TRIM('រាជធានី' || r.name_km)
                    ORDER BY p.id
                    LIMIT 1;
                END IF;

                IF target_id IS NULL THEN
                    INSERT INTO provinces (
                        code,
                        name_km,
                        name_en,
                        description,
                        is_active
                    )
                    VALUES (
                               r.code,
                               r.name_km,
                               r.name_en,
                               NULL,
                               TRUE
                           );
                ELSE
                    UPDATE provinces
                    SET code = r.code,
                        name_km = r.name_km,
                        name_en = r.name_en,
                        is_active = TRUE,
                        updated_at = NOW()
                    WHERE id = target_id;
                END IF;
            END LOOP;
    END
$$;