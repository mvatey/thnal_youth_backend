package org.example.tnal_youth_backend.member.member.repository;

import org.example.tnal_youth_backend.member.member.entity.Gender;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
                        m.joined_on
                    FROM members m
                    INNER JOIN branches b
                            ON b.id = m.branch_id
                    INNER JOIN member_statuses ms
                            ON ms.id = m.status_id
                    LEFT JOIN member_levels ml
                           ON ml.id = m.level_id
                    LEFT JOIN files f
                           ON f.id = m.profile_photo_id
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
                        m.joined_on
                    FROM members m
                    INNER JOIN branches b
                            ON b.id = m.branch_id
                    INNER JOIN member_statuses ms
                            ON ms.id = m.status_id
                    LEFT JOIN member_levels ml
                           ON ml.id = m.level_id
                    LEFT JOIN files f
                           ON f.id = m.profile_photo_id
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
                        m.joined_on
                    FROM members m
                    INNER JOIN branches b
                            ON b.id = m.branch_id
                    INNER JOIN member_statuses ms
                            ON ms.id = m.status_id
                    LEFT JOIN member_levels ml
                           ON ml.id = m.level_id
                    LEFT JOIN files f
                           ON f.id = m.profile_photo_id
                    WHERE m.branch_id = :branchId
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
                        m.joined_on
                    FROM members m
                    INNER JOIN branches b
                            ON b.id = m.branch_id
                    INNER JOIN member_statuses ms
                            ON ms.id = m.status_id
                    LEFT JOIN member_levels ml
                           ON ml.id = m.level_id
                    LEFT JOIN files f
                           ON f.id = m.profile_photo_id
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
                        m.joined_on
                    FROM members m
                    INNER JOIN branches b
                            ON b.id = m.branch_id
                    INNER JOIN member_statuses ms
                            ON ms.id = m.status_id
                    LEFT JOIN member_levels ml
                           ON ml.id = m.level_id
                    LEFT JOIN files f
                           ON f.id = m.profile_photo_id
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

    long countByGender(
            Gender gender
    );

    @Query("""
            SELECT COUNT(member)
            FROM Member member
            JOIN member.religion religion
            WHERE UPPER(religion.code) =
                  UPPER(:religionCode)
            """)
    long countByReligionCode(
            @Param("religionCode")
            String religionCode
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

                m.joined_on

            FROM members m

            INNER JOIN branches b
                    ON b.id = m.branch_id

            INNER JOIN member_statuses ms
                    ON ms.id = m.status_id

            LEFT JOIN member_levels ml
                   ON ml.id = m.level_id

            LEFT JOIN files f
                   ON f.id = m.profile_photo_id

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
             */
            AND (
                :unrestrictedScope = TRUE
                OR m.branch_id IN (:branchScope)
            )

            /*
             * Optional dropdown filter.
             */
            AND (
                :branchId IS NULL
                OR m.branch_id = :branchId
            )

            AND (
                :statusId IS NULL
                OR m.status_id = :statusId
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
            )

            AND (
                :branchId IS NULL
                OR m.branch_id = :branchId
            )

            AND (
                :statusId IS NULL
                OR m.status_id = :statusId
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

            @Param("gender")
            String gender,

            Pageable pageable
    );
}
