//package org.example.tnal_youth_backend.activity.activity.repository;
//
//import org.example.tnal_youth_backend.activity.activity.entity.Activity;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.List;
//
//public interface ActivityRepository
//        extends JpaRepository<Activity, Long> {
//
//    /*
//     * Full Activity entities are still used by the detail,
//     * create, update, and delete flows.
//     */
//    List<Activity> findAllByOrderByStartsAtDescIdDesc();
//
//    /*
//     * ==========================================================
//     * ACTIVITY LIST PAGE
//     * ==========================================================
//     *
//     * These native queries return only the fields required by
//     * the Activity table UI, together with display labels for
//     * type, sector, status, and branch.
//     */
//    @Query(
//            value = """
//                    SELECT
//                        activity.id                    AS activity_id,
//                        activity.title_km              AS title_km,
//                        activity.title_en              AS title_en,
//
//                        activity_type.id               AS type_id,
//                        activity_type.code             AS type_code,
//                        activity_type.label_km         AS type_label_km,
//
//                        activity_sector.id             AS sector_id,
//                        activity_sector.code           AS sector_code,
//                        activity_sector.label_km       AS sector_label_km,
//
//                        activity_status.id             AS status_id,
//                        activity_status.code           AS status_code,
//                        activity_status.label_km       AS status_label_km,
//
//                        branch.id                      AS branch_id,
//                        branch.name_km                 AS branch_name_km,
//
//                        activity.is_public             AS is_public,
//                        activity.starts_at             AS starts_at,
//                        activity.ends_at               AS ends_at,
//                        activity.location_name         AS location_name
//
//                    FROM activities activity
//
//                    INNER JOIN activity_types activity_type
//                            ON activity_type.id = activity.type_id
//
//                    INNER JOIN activity_sectors activity_sector
//                            ON activity_sector.id = activity.sector_id
//
//                    INNER JOIN activity_statuses activity_status
//                            ON activity_status.id = activity.status_id
//
//                    INNER JOIN branches branch
//                            ON branch.id = activity.branch_id
//
//                    ORDER BY
//                        activity.starts_at DESC,
//                        activity.id DESC
//                    """,
//            nativeQuery = true
//    )
//    List<Object[]> findAllActivityListRows();
//
//    @Query(
//            value = """
//                    SELECT
//                        activity.id                    AS activity_id,
//                        activity.title_km              AS title_km,
//                        activity.title_en              AS title_en,
//
//                        activity_type.id               AS type_id,
//                        activity_type.code             AS type_code,
//                        activity_type.label_km         AS type_label_km,
//
//                        activity_sector.id             AS sector_id,
//                        activity_sector.code           AS sector_code,
//                        activity_sector.label_km       AS sector_label_km,
//
//                        activity_status.id             AS status_id,
//                        activity_status.code           AS status_code,
//                        activity_status.label_km       AS status_label_km,
//
//                        branch.id                      AS branch_id,
//                        branch.name_km                 AS branch_name_km,
//
//                        activity.is_public             AS is_public,
//                        activity.starts_at             AS starts_at,
//                        activity.ends_at               AS ends_at,
//                        activity.location_name         AS location_name
//
//                    FROM activities activity
//
//                    INNER JOIN activity_types activity_type
//                            ON activity_type.id = activity.type_id
//
//                    INNER JOIN activity_sectors activity_sector
//                            ON activity_sector.id = activity.sector_id
//
//                    INNER JOIN activity_statuses activity_status
//                            ON activity_status.id = activity.status_id
//
//                    INNER JOIN branches branch
//                            ON branch.id = activity.branch_id
//
//                    WHERE
//                        LOWER(activity.title_km)
//                            LIKE LOWER(CONCAT('%', :search, '%'))
//                        OR LOWER(COALESCE(activity.title_en, ''))
//                            LIKE LOWER(CONCAT('%', :search, '%'))
//
//                    ORDER BY
//                        activity.starts_at DESC,
//                        activity.id DESC
//                    """,
//            nativeQuery = true
//    )
//    List<Object[]> searchActivityListRows(
//            @Param("search")
//            String search
//    );
//
//    @Query(
//            value = """
//                    SELECT
//                        activity.id                    AS activity_id,
//                        activity.title_km              AS title_km,
//                        activity.title_en              AS title_en,
//
//                        activity_type.id               AS type_id,
//                        activity_type.code             AS type_code,
//                        activity_type.label_km         AS type_label_km,
//
//                        activity_sector.id             AS sector_id,
//                        activity_sector.code           AS sector_code,
//                        activity_sector.label_km       AS sector_label_km,
//
//                        activity_status.id             AS status_id,
//                        activity_status.code           AS status_code,
//                        activity_status.label_km       AS status_label_km,
//
//                        branch.id                      AS branch_id,
//                        branch.name_km                 AS branch_name_km,
//
//                        activity.is_public             AS is_public,
//                        activity.starts_at             AS starts_at,
//                        activity.ends_at               AS ends_at,
//                        activity.location_name         AS location_name
//
//                    FROM activities activity
//
//                    INNER JOIN activity_types activity_type
//                            ON activity_type.id = activity.type_id
//
//                    INNER JOIN activity_sectors activity_sector
//                            ON activity_sector.id = activity.sector_id
//
//                    INNER JOIN activity_statuses activity_status
//                            ON activity_status.id = activity.status_id
//
//                    INNER JOIN branches branch
//                            ON branch.id = activity.branch_id
//
//                    WHERE activity.type_id = :typeId
//
//                    ORDER BY
//                        activity.starts_at DESC,
//                        activity.id DESC
//                    """,
//            nativeQuery = true
//    )
//    List<Object[]> findActivityListRowsByTypeId(
//            @Param("typeId")
//            Short typeId
//    );
//}
