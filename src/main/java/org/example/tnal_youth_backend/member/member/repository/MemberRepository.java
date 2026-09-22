package org.example.tnal_youth_backend.member.member.repository;

import org.example.tnal_youth_backend.authentication.model.entity.Role;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.example.tnal_youth_backend.member.branch.dto.projection.BranchManagementProjection;
import org.example.tnal_youth_backend.member.member.entity.Gender;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MemberRepository
        extends JpaRepository<Member, Long> {

    boolean existsByMemberNoIgnoreCase(
            String memberNo
    );

    boolean existsByMemberNoIgnoreCaseAndIdNot(
            String memberNo,
            Long id
    );

    boolean existsByPhone(
            String phone
    );

    boolean existsByPhoneAndIdNot(
            String phone,
            Long id
    );

    boolean existsByEmailIgnoreCase(
            String email
    );

    boolean existsByEmailIgnoreCaseAndIdNot(
            String email,
            Long id
    );

    @EntityGraph(attributePaths = {
            "status",
            "level",
            "religion",
            "nationality",
            "ethnicity",
            "profilePhoto",
            "cvFile"
    })
    @Query("""
            SELECT member
            FROM Member member
            WHERE member.id = :id
            """)
    Optional<Member> findDetailedById(
            @Param("id")
            Long id
    );

    /*
     * ==========================================================
     * MEMBER LIST
     * ==========================================================
     *
     * The entity keeps branchId as a Long.
     * These native queries join branches only for the list response.
     *
     * Column order must match MemberMapper.toListResponse(Object[]).
     */

    @Query(
            value = """
                    SELECT
                        m.id,
                        m.full_name_km,
                        m.full_name_en,
                        m.gender,
                        CASE m.gender
                            WHEN 'MALE' THEN 'ប្រុស'
                            WHEN 'FEMALE' THEN 'ស្រី'
                            WHEN 'OTHER' THEN 'ព្រះសង្ឃ'
                            ELSE m.gender
                        END AS gender_label_km,
                        b.id AS branch_id,
                        b.name_km AS branch_name_km,
                        ms.id AS status_id,
                        ms.code AS status_code,
                        ms.label_km AS status_label_km,
                        ms.label_en AS status_label_en,
                        ml.id AS level_id,
                        ml.code AS level_code,
                        ml.label_km AS level_label_km,
                        ml.label_en AS level_label_en,
                        f.id AS profile_photo_id,
                        f.file_path AS profile_photo_url,
                        m.joined_on,
                        m.email AS email,
                        u.role AS account_role_code,
                        CASE u.role
                            WHEN 'ADMIN' THEN 'អ្នកគ្រប់គ្រង'
                            WHEN 'SECRETARY' THEN 'លេខាធិការ'
                            WHEN 'BRANCH_LEADER' THEN 'ប្រធានសាខា'
                            WHEN 'MEMBER' THEN 'សមាជិក'
                            ELSE u.role
                        END AS account_role_label_km
                    FROM members m
                    INNER JOIN branches b
                            ON b.id = m.branch_id
                    INNER JOIN member_statuses ms
                            ON ms.id = m.status_id
                    LEFT JOIN member_levels ml
                           ON ml.id = m.level_id
                    LEFT JOIN files f
                           ON f.id = m.profile_photo_id
                    LEFT JOIN users u
                           ON u.member_id = m.id
                    ORDER BY
                        m.created_at DESC,
                        m.id DESC
                    """,
            nativeQuery = true
    )
    List<Object[]> findAllListRows();

    @Query(
            value = """
                    SELECT
                        m.id,
                        m.full_name_km,
                        m.full_name_en,
                        m.gender,
                        CASE m.gender
                            WHEN 'MALE' THEN 'ប្រុស'
                            WHEN 'FEMALE' THEN 'ស្រី'
                            WHEN 'OTHER' THEN 'ព្រះសង្ឃ'
                            ELSE m.gender
                        END AS gender_label_km,
                        b.id AS branch_id,
                        b.name_km AS branch_name_km,
                        ms.id AS status_id,
                        ms.code AS status_code,
                        ms.label_km AS status_label_km,
                        ms.label_en AS status_label_en,
                        ml.id AS level_id,
                        ml.code AS level_code,
                        ml.label_km AS level_label_km,
                        ml.label_en AS level_label_en,
                        f.id AS profile_photo_id,
                        f.file_path AS profile_photo_url,
                        m.joined_on,
                        m.email AS email,
                        u.role AS account_role_code,
                        CASE u.role
                            WHEN 'ADMIN' THEN 'អ្នកគ្រប់គ្រង'
                            WHEN 'SECRETARY' THEN 'លេខាធិការ'
                            WHEN 'BRANCH_LEADER' THEN 'ប្រធានសាខា'
                            WHEN 'MEMBER' THEN 'សមាជិក'
                            ELSE u.role
                        END AS account_role_label_km
                    FROM members m
                    INNER JOIN branches b
                            ON b.id = m.branch_id
                    INNER JOIN member_statuses ms
                            ON ms.id = m.status_id
                    LEFT JOIN member_levels ml
                           ON ml.id = m.level_id
                    LEFT JOIN files f
                           ON f.id = m.profile_photo_id
                    LEFT JOIN users u
                           ON u.member_id = m.id
                    WHERE
                        LOWER(m.full_name_km)
                            LIKE LOWER(CONCAT('%', :name, '%'))
                        OR LOWER(COALESCE(m.full_name_en, ''))
                            LIKE LOWER(CONCAT('%', :name, '%'))
                    ORDER BY
                        m.created_at DESC,
                        m.id DESC
                    """,
            nativeQuery = true
    )
    List<Object[]> searchListRowsByName(
            @Param("name")
            String name
    );

    @Query(
            value = """
                    SELECT
                        m.id,
                        m.full_name_km,
                        m.full_name_en,
                        m.gender,
                        CASE m.gender
                            WHEN 'MALE' THEN 'ប្រុស'
                            WHEN 'FEMALE' THEN 'ស្រី'
                            WHEN 'OTHER' THEN 'ព្រះសង្ឃ'
                            ELSE m.gender
                        END AS gender_label_km,
                        b.id AS branch_id,
                        b.name_km AS branch_name_km,
                        ms.id AS status_id,
                        ms.code AS status_code,
                        ms.label_km AS status_label_km,
                        ms.label_en AS status_label_en,
                        ml.id AS level_id,
                        ml.code AS level_code,
                        ml.label_km AS level_label_km,
                        ml.label_en AS level_label_en,
                        f.id AS profile_photo_id,
                        f.file_path AS profile_photo_url,
                        m.joined_on,
                        m.email AS email,
                        u.role AS account_role_code,
                        CASE u.role
                            WHEN 'ADMIN' THEN 'អ្នកគ្រប់គ្រង'
                            WHEN 'SECRETARY' THEN 'លេខាធិការ'
                            WHEN 'BRANCH_LEADER' THEN 'ប្រធានសាខា'
                            WHEN 'MEMBER' THEN 'សមាជិក'
                            ELSE u.role
                        END AS account_role_label_km
                    FROM members m
                    INNER JOIN branches b
                            ON b.id = m.branch_id
                    INNER JOIN member_statuses ms
                            ON ms.id = m.status_id
                    LEFT JOIN member_levels ml
                           ON ml.id = m.level_id
                    LEFT JOIN files f
                           ON f.id = m.profile_photo_id
                    LEFT JOIN users u
                           ON u.member_id = m.id
                    WHERE (
                        m.branch_id = :branchId
                        OR EXISTS (
                            SELECT 1 FROM branch_staff bs
                            WHERE bs.member_id = m.id
                              AND bs.branch_id = :branchId
                              AND bs.ended_on IS NULL
                        )
                    )
                    ORDER BY
                        m.created_at DESC,
                        m.id DESC
                    """,
            nativeQuery = true
    )
    List<Object[]> findListRowsByBranchId(
            @Param("branchId")
            Long branchId
    );

    @Query(
            value = """
                    SELECT
                        m.id,
                        m.full_name_km,
                        m.full_name_en,
                        m.gender,
                        CASE m.gender
                            WHEN 'MALE' THEN 'ប្រុស'
                            WHEN 'FEMALE' THEN 'ស្រី'
                            WHEN 'OTHER' THEN 'ព្រះសង្ឃ'
                            ELSE m.gender
                        END AS gender_label_km,
                        b.id AS branch_id,
                        b.name_km AS branch_name_km,
                        ms.id AS status_id,
                        ms.code AS status_code,
                        ms.label_km AS status_label_km,
                        ms.label_en AS status_label_en,
                        ml.id AS level_id,
                        ml.code AS level_code,
                        ml.label_km AS level_label_km,
                        ml.label_en AS level_label_en,
                        f.id AS profile_photo_id,
                        f.file_path AS profile_photo_url,
                        m.joined_on,
                        m.email AS email,
                        u.role AS account_role_code,
                        CASE u.role
                            WHEN 'ADMIN' THEN 'អ្នកគ្រប់គ្រង'
                            WHEN 'SECRETARY' THEN 'លេខាធិការ'
                            WHEN 'BRANCH_LEADER' THEN 'ប្រធានសាខា'
                            WHEN 'MEMBER' THEN 'សមាជិក'
                            ELSE u.role
                        END AS account_role_label_km
                    FROM members m
                    INNER JOIN branches b
                            ON b.id = m.branch_id
                    INNER JOIN member_statuses ms
                            ON ms.id = m.status_id
                    LEFT JOIN member_levels ml
                           ON ml.id = m.level_id
                    LEFT JOIN files f
                           ON f.id = m.profile_photo_id
                    LEFT JOIN users u
                           ON u.member_id = m.id
                    WHERE m.status_id = :statusId
                    ORDER BY
                        m.created_at DESC,
                        m.id DESC
                    """,
            nativeQuery = true
    )
    List<Object[]> findListRowsByStatusId(
            @Param("statusId")
            Short statusId
    );

    @Query(
            value = """
                    SELECT
                        m.id,
                        m.full_name_km,
                        m.full_name_en,
                        m.gender,
                        CASE m.gender
                            WHEN 'MALE' THEN 'ប្រុស'
                            WHEN 'FEMALE' THEN 'ស្រី'
                            WHEN 'OTHER' THEN 'ព្រះសង្ឃ'
                            ELSE m.gender
                        END AS gender_label_km,
                        b.id AS branch_id,
                        b.name_km AS branch_name_km,
                        ms.id AS status_id,
                        ms.code AS status_code,
                        ms.label_km AS status_label_km,
                        ms.label_en AS status_label_en,
                        ml.id AS level_id,
                        ml.code AS level_code,
                        ml.label_km AS level_label_km,
                        ml.label_en AS level_label_en,
                        f.id AS profile_photo_id,
                        f.file_path AS profile_photo_url,
                        m.joined_on,
                        m.email AS email,
                        u.role AS account_role_code,
                        CASE u.role
                            WHEN 'ADMIN' THEN 'អ្នកគ្រប់គ្រង'
                            WHEN 'SECRETARY' THEN 'លេខាធិការ'
                            WHEN 'BRANCH_LEADER' THEN 'ប្រធានសាខា'
                            WHEN 'MEMBER' THEN 'សមាជិក'
                            ELSE u.role
                        END AS account_role_label_km
                    FROM members m
                    INNER JOIN branches b
                            ON b.id = m.branch_id
                    INNER JOIN member_statuses ms
                            ON ms.id = m.status_id
                    LEFT JOIN member_levels ml
                           ON ml.id = m.level_id
                    LEFT JOIN files f
                           ON f.id = m.profile_photo_id
                    LEFT JOIN users u
                           ON u.member_id = m.id
                    WHERE m.gender = :gender
                    ORDER BY
                        m.created_at DESC,
                        m.id DESC
                    """,
            nativeQuery = true
    )
    List<Object[]> findListRowsByGender(
            @Param("gender")
            String gender
    );

    /*
     * Finds the latest generated number matching TNAL-M-####.
     */
    @Query(
            value = """
                    SELECT member_no
                    FROM members
                    WHERE member_no ~ '^TNAL-M-[0-9]+$'
                    ORDER BY
                        CAST(
                            SUBSTRING(member_no FROM 8)
                            AS INTEGER
                        ) DESC
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Optional<String> findLatestGeneratedMemberNo();

    /*
     * ==========================================================
     * MEMBER SUMMARY
     * ==========================================================
     */

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM members m
                    WHERE NOT EXISTS (
                        SELECT 1 FROM users u
                        WHERE u.member_id = m.id
                          AND u.status = 'INACTIVE'
                    )
                    """,
            nativeQuery = true
    )
    long countAllMembers();

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM members m
                    WHERE UPPER(m.gender) = UPPER(:gender)
                      AND NOT EXISTS (
                          SELECT 1 FROM users u
                          WHERE u.member_id = m.id
                            AND u.status = 'INACTIVE'
                      )
                    """,
            nativeQuery = true
    )
    long countByGender(
            @Param("gender") String gender
    );

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM members m
                    WHERE (
                        m.branch_id = :branchId
                        OR EXISTS (
                            SELECT 1 FROM branch_staff bs
                            WHERE bs.member_id = m.id
                              AND bs.branch_id = :branchId
                              AND bs.ended_on IS NULL
                        )
                    )
                      AND NOT EXISTS (
                          SELECT 1 FROM users u
                          WHERE u.member_id = m.id
                            AND u.status = 'INACTIVE'
                      )
                    """,
            nativeQuery = true
    )
    long countByBranchId(
            @Param("branchId") Long branchId
    );

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM members m
                    WHERE UPPER(m.gender) = UPPER(:gender)
                      AND (
                          m.branch_id = :branchId
                          OR EXISTS (
                              SELECT 1 FROM branch_staff bs
                              WHERE bs.member_id = m.id
                                AND bs.branch_id = :branchId
                                AND bs.ended_on IS NULL
                          )
                      )
                      AND NOT EXISTS (
                          SELECT 1 FROM users u
                          WHERE u.member_id = m.id
                            AND u.status = 'INACTIVE'
                      )
                    """,
            nativeQuery = true
    )
    long countByGenderAndBranchId(
            @Param("gender") String gender,
            @Param("branchId") Long branchId
    );

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM members m
                    JOIN religions r ON r.id = m.religion_id
                    WHERE UPPER(r.code) = UPPER(:religionCode)
                      AND NOT EXISTS (
                          SELECT 1 FROM users u
                          WHERE u.member_id = m.id
                            AND u.status = 'INACTIVE'
                      )
                    """,
            nativeQuery = true
    )
    long countByReligionCode(
            @Param("religionCode")
            String religionCode
    );

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM members m
                    JOIN religions r ON r.id = m.religion_id
                    WHERE UPPER(r.code) = UPPER(:religionCode)
                      AND (
                          m.branch_id = :branchId
                          OR EXISTS (
                              SELECT 1 FROM branch_staff bs
                              WHERE bs.member_id = m.id
                                AND bs.branch_id = :branchId
                                AND bs.ended_on IS NULL
                          )
                      )
                      AND NOT EXISTS (
                          SELECT 1 FROM users u
                          WHERE u.member_id = m.id
                            AND u.status = 'INACTIVE'
                      )
                    """,
            nativeQuery = true
    )
    long countByReligionCodeAndBranchId(
            @Param("religionCode") String religionCode,
            @Param("branchId") Long branchId
    );

    @Query("""
            SELECT COUNT(member)
            FROM Member member
            JOIN member.status status
            WHERE UPPER(status.code) = 'INACTIVE'
            """)
    long countInactiveMembers();

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM members m
                    WHERE COALESCE(m.joined_on, m.created_at::date)
                          < :exclusiveEndDate
                      AND NOT EXISTS (
                          SELECT 1 FROM users u
                          WHERE u.member_id = m.id
                            AND u.status = 'INACTIVE'
                      )
                    """,
            nativeQuery = true
    )
    long countBefore(
            @Param("exclusiveEndDate") LocalDate exclusiveEndDate
    );

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM members m
                    WHERE (
                        m.branch_id = :branchId
                        OR EXISTS (
                            SELECT 1 FROM branch_staff bs
                            WHERE bs.member_id = m.id
                              AND bs.branch_id = :branchId
                              AND bs.ended_on IS NULL
                        )
                    )
                      AND COALESCE(m.joined_on, m.created_at::date)
                          < :exclusiveEndDate
                      AND NOT EXISTS (
                          SELECT 1 FROM users u
                          WHERE u.member_id = m.id
                            AND u.status = 'INACTIVE'
                      )
                    """,
            nativeQuery = true
    )
    long countByBranchIdBefore(
            @Param("branchId") Long branchId,
            @Param("exclusiveEndDate") LocalDate exclusiveEndDate
    );

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM members m
                    WHERE UPPER(m.gender) = UPPER(:gender)
                      AND COALESCE(m.joined_on, m.created_at::date)
                          < :exclusiveEndDate
                      AND NOT EXISTS (
                          SELECT 1 FROM users u
                          WHERE u.member_id = m.id
                            AND u.status = 'INACTIVE'
                      )
                    """,
            nativeQuery = true
    )
    long countByGenderBefore(
            @Param("gender") String gender,
            @Param("exclusiveEndDate") LocalDate exclusiveEndDate
    );

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM members m
                    WHERE UPPER(m.gender) = UPPER(:gender)
                      AND (
                          m.branch_id = :branchId
                          OR EXISTS (
                              SELECT 1 FROM branch_staff bs
                              WHERE bs.member_id = m.id
                                AND bs.branch_id = :branchId
                                AND bs.ended_on IS NULL
                          )
                      )
                      AND COALESCE(m.joined_on, m.created_at::date)
                          < :exclusiveEndDate
                      AND NOT EXISTS (
                          SELECT 1 FROM users u
                          WHERE u.member_id = m.id
                            AND u.status = 'INACTIVE'
                      )
                    """,
            nativeQuery = true
    )
    long countByGenderAndBranchIdBefore(
            @Param("gender") String gender,
            @Param("branchId") Long branchId,
            @Param("exclusiveEndDate") LocalDate exclusiveEndDate
    );

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM members m
                    JOIN religions r ON r.id = m.religion_id
                    WHERE UPPER(r.code) = UPPER(:religionCode)
                      AND COALESCE(m.joined_on, m.created_at::date)
                          < :exclusiveEndDate
                      AND NOT EXISTS (
                          SELECT 1 FROM users u
                          WHERE u.member_id = m.id
                            AND u.status = 'INACTIVE'
                      )
                    """,
            nativeQuery = true
    )
    long countByReligionCodeBefore(
            @Param("religionCode") String religionCode,
            @Param("exclusiveEndDate") LocalDate exclusiveEndDate
    );

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM members m
                    JOIN religions r ON r.id = m.religion_id
                    WHERE UPPER(r.code) = UPPER(:religionCode)
                      AND (
                          m.branch_id = :branchId
                          OR EXISTS (
                              SELECT 1 FROM branch_staff bs
                              WHERE bs.member_id = m.id
                                AND bs.branch_id = :branchId
                                AND bs.ended_on IS NULL
                          )
                      )
                      AND COALESCE(m.joined_on, m.created_at::date)
                          < :exclusiveEndDate
                      AND NOT EXISTS (
                          SELECT 1 FROM users u
                          WHERE u.member_id = m.id
                            AND u.status = 'INACTIVE'
                      )
                    """,
            nativeQuery = true
    )
    long countByReligionCodeAndBranchIdBefore(
            @Param("religionCode") String religionCode,
            @Param("branchId") Long branchId,
            @Param("exclusiveEndDate") LocalDate exclusiveEndDate
    );

    @Query(
            value = """
                    SELECT COUNT(DISTINCT u.member_id)
                    FROM users u
                    INNER JOIN members m
                            ON m.id = u.member_id
                    WHERE UPPER(u.role) = 'BRANCH_LEADER'
                      AND u.member_id IS NOT NULL
                    """,
            nativeQuery = true
    )
    long countLeaderMembers();

    @Query(
            value = """
            SELECT
                m.id,
                m.full_name_km,
                m.full_name_en,
                m.gender,

                CASE m.gender
                    WHEN 'MALE' THEN 'ប្រុស'
                    WHEN 'FEMALE' THEN 'ស្រី'
                    WHEN 'MONK' THEN 'ព្រះសង្ឃ'
                    WHEN 'OTHER' THEN 'ផ្សេងៗ'
                    ELSE m.gender
                END AS gender_label_km,

                b.id AS branch_id,
                b.name_km AS branch_name_km,

                ms.id AS status_id,
                ms.code AS status_code,
                ms.label_km AS status_label_km,
                ms.label_en AS status_label_en,

                ml.id AS level_id,
                ml.code AS level_code,
                ml.label_km AS level_label_km,
                ml.label_en AS level_label_en,

                f.id AS profile_photo_id,
                f.file_path AS profile_photo_url,

                m.joined_on,
                m.email AS email,
                u.role AS account_role_code,
                CASE u.role
                    WHEN 'ADMIN' THEN 'អ្នកគ្រប់គ្រង'
                    WHEN 'SECRETARY' THEN 'លេខាធិការ'
                    WHEN 'BRANCH_LEADER' THEN 'ប្រធានសាខា'
                    WHEN 'MEMBER' THEN 'សមាជិក'
                    ELSE u.role
                END AS account_role_label_km,

                u.status AS account_status_code,
                CASE u.status
                    WHEN 'PENDING_ACTIVATION' THEN 'រង់ចាំដំណើរការ'
                    WHEN 'ACTIVE' THEN 'សកម្ម'
                    WHEN 'INACTIVE' THEN 'អសកម្ម'
                    WHEN 'LOCKED' THEN 'បានចាក់សោ'
                    ELSE u.status
                END AS account_status_label_km,

                b.name_en AS branch_name_en,

                m.status_changed_at,

                pos.id AS position_id,
                pos.code AS position_code,
                pos.label_km AS position_label_km,
                pos.label_en AS position_label_en

            FROM members m

            INNER JOIN branches b
                    ON b.id = m.branch_id

            INNER JOIN member_statuses ms
                    ON ms.id = m.status_id

            LEFT JOIN member_levels ml
                   ON ml.id = m.level_id

            LEFT JOIN files f
                   ON f.id = m.profile_photo_id
            LEFT JOIN users u
                   ON u.member_id = m.id

            /*
             * The member's current job title within their own branch --
             * excludes the primary (branch leader) row, a separate concept
             * with its own dedicated assignment flow. LATERAL + LIMIT 1
             * keeps this to at most one row per member even if the
             * ended_on/is_primary invariant were ever violated, so this
             * join can never itself multiply the member rows returned.
             */
            LEFT JOIN LATERAL (
                SELECT p2.id, p2.code, p2.label_km, p2.label_en
                FROM branch_staff bs2
                JOIN positions p2
                     ON p2.id = bs2.position_id
                WHERE bs2.member_id = m.id
                  AND bs2.branch_id = m.branch_id
                  AND bs2.ended_on IS NULL
                  AND bs2.is_primary = FALSE
                ORDER BY bs2.started_on DESC, bs2.id DESC
                LIMIT 1
            ) pos ON TRUE

            WHERE (
                :search IS NULL
                OR :search = ''
                OR LOWER(m.full_name_km)
                    LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(m.full_name_en, ''))
                    LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(m.member_no, ''))
                    LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(m.phone, ''))
                    LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(m.email, ''))
                    LIKE LOWER(CONCAT('%', :search, '%'))
            )

            /*
             * Security scope:
             * Admin bypasses this condition.
             * Secretary/branch leader only see accessible branches.
             * A member whose PRIMARY branch is outside scope still counts
             * if they have an active branch_staff assignment to one that
             * is in scope -- e.g. a secretary staffing a second branch
             * beyond their own members.branch_id.
             */
            AND (
                :unrestrictedScope = TRUE
                OR m.branch_id IN (:branchScope)
                OR EXISTS (
                    SELECT 1 FROM branch_staff bs
                    WHERE bs.member_id = m.id
                      AND bs.branch_id IN (:branchScope)
                      AND bs.ended_on IS NULL
                )
            )

            /*
             * Optional dropdown filter.
             */
            AND (
                :branchId IS NULL
                OR m.branch_id = :branchId
                OR EXISTS (
                    SELECT 1 FROM branch_staff bs
                    WHERE bs.member_id = m.id
                      AND bs.branch_id = :branchId
                      AND bs.ended_on IS NULL
                )
            )

            AND (
                :statusId IS NULL
                OR m.status_id = :statusId
            )

            AND (
                :accountStatus IS NULL
                OR u.status = :accountStatus
            )

            /*
             * A deleted (soft-removed) login account permanently hides its
             * member from this list, regardless of any accountStatus filter
             * the caller passed -- same unconditional INACTIVE exclusion
             * rule the Users page itself uses. The member row and all its
             * other data (donations, participation, ...) is untouched; it
             * just no longer shows up here or in any member picker built on
             * this same query.
             */
            AND (
                u.status IS NULL
                OR u.status <> 'INACTIVE'
            )

            AND (
                :gender IS NULL
                OR m.gender = :gender
            )

            ORDER BY
                m.created_at DESC,
                m.id DESC
            """,

            countQuery = """
            SELECT COUNT(*)

            FROM members m
            LEFT JOIN users u
                   ON u.member_id = m.id

            WHERE (
                :search IS NULL
                OR :search = ''
                OR LOWER(m.full_name_km)
                    LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(m.full_name_en, ''))
                    LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(m.member_no, ''))
                    LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(m.phone, ''))
                    LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(m.email, ''))
                    LIKE LOWER(CONCAT('%', :search, '%'))
            )

            AND (
                :unrestrictedScope = TRUE
                OR m.branch_id IN (:branchScope)
                OR EXISTS (
                    SELECT 1 FROM branch_staff bs
                    WHERE bs.member_id = m.id
                      AND bs.branch_id IN (:branchScope)
                      AND bs.ended_on IS NULL
                )
            )

            AND (
                :branchId IS NULL
                OR m.branch_id = :branchId
                OR EXISTS (
                    SELECT 1 FROM branch_staff bs
                    WHERE bs.member_id = m.id
                      AND bs.branch_id = :branchId
                      AND bs.ended_on IS NULL
                )
            )

            AND (
                :statusId IS NULL
                OR m.status_id = :statusId
            )

            AND (
                :accountStatus IS NULL
                OR u.status = :accountStatus
            )

            AND (
                u.status IS NULL
                OR u.status <> 'INACTIVE'
            )

            AND (
                :gender IS NULL
                OR m.gender = :gender
            )
            """,

            nativeQuery = true
    )
    Page<Object[]> findMemberPage(
            @Param("search")
            String search,

            @Param("branchId")
            Long branchId,

            @Param("branchScope")
            Set<Long> branchScope,

            @Param("unrestrictedScope")
            boolean unrestrictedScope,

            @Param("statusId")
            Short statusId,

            @Param("accountStatus")
            String accountStatus,

            @Param("gender")
            String gender,

            Pageable pageable
    );

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM members m
                    WHERE m.branch_id IN (:branchIds)
                      AND NOT EXISTS (
                          SELECT 1 FROM users u
                          WHERE u.member_id = m.id
                            AND u.status = 'INACTIVE'
                      )
                    """,
            nativeQuery = true
    )
    long countByBranchIdIn(
            @Param("branchIds") Iterable<Long> branchIds
    );

    @Query("""
    SELECT
        m AS member,
        u.role AS role
    FROM Member m
    JOIN User u
        ON u.memberId = m.id
    WHERE (
        m.branchId = :branchId
        OR m.id IN :branchStaffMemberIds
    )
      AND u.role IN :roles
      AND u.status <> :deletedStatus
    ORDER BY m.fullNameKm ASC
""")
    List<BranchManagementProjection>
    findBranchManagementMembers(
            @Param("branchId") Long branchId,
            @Param("branchStaffMemberIds") Collection<Long> branchStaffMemberIds,
            @Param("roles") Collection<UserRole> roles,
            @Param("deletedStatus") UserStatus deletedStatus
    );

    @Query("""
    SELECT CASE
        WHEN COUNT(m) > 0
            THEN true
        ELSE false
    END
    FROM Member m
    JOIN User u
        ON u.memberId = m.id
    WHERE m.branchId = :branchId
      AND u.role = :role
""")
    boolean existsBranchMemberWithRole(
            @Param("branchId")
            Long branchId,

            @Param("role")
            UserRole role
    );

    @Query("""
SELECT
    m AS member,
    u.role AS role
FROM Member m
LEFT JOIN User u
    ON u.memberId = m.id
WHERE (
      m.branchId = :branchId
      OR m.id IN :branchStaffMemberIds
  )

  AND (
      u.id IS NULL
      OR u.role <> :excludedRole
  )

  AND (
      u.id IS NULL
      OR u.status <> :deletedStatus
  )

  AND (
      :search = ''
      OR LOWER(m.fullNameKm)
            LIKE CONCAT('%', LOWER(:search), '%')
      OR LOWER(COALESCE(m.fullNameEn, ''))
            LIKE CONCAT('%', LOWER(:search), '%')
      OR COALESCE(m.phone, '')
            LIKE CONCAT('%', :search, '%')
  )

  AND (
      :gender IS NULL
      OR m.gender = :gender
  )

  AND (
      :statusId IS NULL
      OR m.status.id = :statusId
  )

ORDER BY
    m.createdAt DESC,
    m.id DESC
""")
    Page<BranchManagementProjection>
    findBranchMembersExcludingRole(
            @Param("branchId")
            Long branchId,

            @Param("branchStaffMemberIds")
            Collection<Long> branchStaffMemberIds,

            @Param("excludedRole")
            UserRole excludedRole,

            @Param("deletedStatus")
            UserStatus deletedStatus,

            @Param("search")
            String search,

            @Param("gender")
            Gender gender,

            @Param("statusId")
            Short statusId,

            Pageable pageable
    );

    @Query("""
    SELECT
        m AS member,
        u.role AS role
    FROM Member m
    JOIN User u
        ON u.memberId = m.id
    WHERE u.role = :role
      AND (
          :currentLeaderMemberId IS NULL
          OR m.id <> :currentLeaderMemberId
      )
    ORDER BY m.fullNameKm ASC
""")
    List<BranchManagementProjection>
    findBranchLeaderCandidates(
            @Param("role")
            UserRole role,

            @Param("currentLeaderMemberId")
            Long currentLeaderMemberId
    );
    @Query("""
    SELECT
        m AS member,
        u.role AS role
    FROM Member m
    JOIN User u
        ON u.memberId = m.id
    WHERE m.branchId = :branchId
      AND u.role IN :eligibleRoles
    ORDER BY m.fullNameKm ASC
""")
    List<BranchManagementProjection>
    findBranchLeaderCandidates(
            @Param("branchId")
            Long branchId,

            @Param("eligibleRoles")
            Collection<UserRole> eligibleRoles
    );


}
