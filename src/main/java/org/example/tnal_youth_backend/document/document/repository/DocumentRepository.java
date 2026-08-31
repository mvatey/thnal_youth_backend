package org.example.tnal_youth_backend.document.document.repository;

import org.example.tnal_youth_backend.document.document.entity.Document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

import java.util.List;
import java.util.Set;

public interface DocumentRepository
        extends JpaRepository<Document, Long> {

    /*
     * ==========================================================
     * BASIC DOCUMENT QUERIES
     * ==========================================================
     */

    List<Document>
    findAllByOrderByCreatedAtDescIdDesc();

    List<Document>
    findAllByMemberIdOrderByCreatedAtDescIdDesc(
            Long memberId
    );

    List<Document>
    findAllByBranchIdOrderByCreatedAtDescIdDesc(
            Long branchId
    );

    List<Document>
    findAllByActivityIdOrderByCreatedAtDescIdDesc(
            Long activityId
    );

    boolean existsByFileId(
            Long fileId
    );

    java.util.Optional<Document> findByFileId(
            Long fileId
    );


    /*
     * ==========================================================
     * ORGANIZATIONAL DOCUMENT PAGE
     * ==========================================================
     *
     * IMPORTANT:
     *
     * startDateTime and endDateTime must ALWAYS
     * be non-null when this query is called.
     *
     * Do not use:
     *
     * :startDateTime IS NULL
     *
     * because PostgreSQL can fail to determine
     * the SQL type of a null OffsetDateTime
     * parameter.
     */

    @Query("""
    SELECT d
    FROM Document d

    WHERE (
        :search = ''

        OR LOWER(d.title)
            LIKE CONCAT(
                '%',
                LOWER(:search),
                '%'
            )

        OR LOWER(
            COALESCE(
                d.description,
                ''
            )
        )
            LIKE CONCAT(
                '%',
                LOWER(:search),
                '%'
            )
    )

    AND (
        :typeId IS NULL
        OR d.typeId = :typeId
    )

    AND (
        :branchId IS NULL
        OR d.branchId = :branchId
    )

    AND (
        :memberId IS NULL
        OR d.memberId = :memberId
    )

    AND (
        :activityId IS NULL
        OR d.activityId = :activityId
    )

    AND d.createdAt >= :startDateTime

    AND d.createdAt < :endDateTime

    AND (
        :isAdmin = true

        OR d.branchId IN :accessibleBranchIds

        OR d.memberId IN (
            SELECT m.id
            FROM Member m
            WHERE m.branchId
                IN :accessibleBranchIds
        )

        OR d.activityId IN (
            SELECT a.id
            FROM Activity a
            WHERE a.branchId
                IN :accessibleBranchIds
        )
    )
    """)
    Page<Document> findDocumentPage(

            @Param("search")
            String search,

            @Param("typeId")
            Short typeId,

            @Param("branchId")
            Long branchId,

            @Param("memberId")
            Long memberId,

            @Param("activityId")
            Long activityId,

            @Param("startDateTime")
            OffsetDateTime startDateTime,

            @Param("endDateTime")
            OffsetDateTime endDateTime,

            @Param("isAdmin")
            boolean isAdmin,

            @Param("accessibleBranchIds")
            Set<Long> accessibleBranchIds,

            Pageable pageable
    );


    /*
     * ==========================================================
     * MEMBER DOCUMENT PAGE
     * ==========================================================
     */

    @Query("""
    SELECT d
    FROM Document d

    WHERE d.memberId IS NOT NULL

    AND (
        :search = ''

        OR LOWER(d.title)
            LIKE CONCAT(
                '%',
                LOWER(:search),
                '%'
            )

        OR LOWER(
            COALESCE(
                d.description,
                ''
            )
        )
            LIKE CONCAT(
                '%',
                LOWER(:search),
                '%'
            )

        OR d.memberId IN (
            SELECT m.id
            FROM Member m

            WHERE LOWER(m.fullNameKm)
                LIKE CONCAT(
                    '%',
                    LOWER(:search),
                    '%'
                )

            OR LOWER(
                COALESCE(
                    m.fullNameEn,
                    ''
                )
            )
                LIKE CONCAT(
                    '%',
                    LOWER(:search),
                    '%'
                )
        )
    )

    AND (
        :typeId IS NULL
        OR d.typeId = :typeId
    )

    AND (
        :branchId IS NULL

        OR d.memberId IN (
            SELECT m.id
            FROM Member m
            WHERE m.branchId = :branchId
        )
    )

    AND d.createdAt >= :startDateTime

    AND d.createdAt < :endDateTime

    AND (
        :isAdmin = true

        OR (
            :isMember = true
            AND d.memberId =
                :currentMemberId
        )

        OR (
            :isStaff = true

            AND d.memberId IN (
                SELECT m.id
                FROM Member m

                WHERE m.branchId
                    IN :accessibleBranchIds
            )
        )
    )
    """)
    Page<Document> findMemberDocumentPage(

            @Param("search")
            String search,

            @Param("typeId")
            Short typeId,

            @Param("branchId")
            Long branchId,

            @Param("startDateTime")
            OffsetDateTime startDateTime,

            @Param("endDateTime")
            OffsetDateTime endDateTime,

            @Param("isAdmin")
            boolean isAdmin,

            @Param("isMember")
            boolean isMember,

            @Param("isStaff")
            boolean isStaff,

            @Param("currentMemberId")
            Long currentMemberId,

            @Param("accessibleBranchIds")
            Set<Long> accessibleBranchIds,

            Pageable pageable
    );

    /*
     * ==========================================================
     * CROSS-BRANCH CERTIFICATES (activity certificates a staff member's
     * own branch(es) issued to another branch's member, via an activity
     * they organize/co-organize)
     * ==========================================================
     *
     * findMemberDocumentPage above only ever returns documents whose
     * OWNING member belongs to an accessible branch -- an activity's
     * host branch can legitimately issue a personal certificate to a
     * co-hosting branch's member (see MemberCredentialServiceImpl's
     * host-branch carve-out), but that document then belongs to a member
     * outside the issuing staff's normal branch scope and would
     * otherwise be invisible to them entirely. This surfaces exactly
     * those: certificates whose ACTIVITY is hosted by one of the current
     * staff's own branches, but whose RECIPIENT belongs to some other
     * branch. Native query because Document and MemberCredential have no
     * mapped JPA relationship to each other -- they're linked only by
     * (member_id, file_id) coincidentally matching.
     */
    @Query(
            value = """
            SELECT d.*
            FROM documents d
            JOIN member_credentials mc
                ON mc.member_id = d.member_id
               AND mc.file_id = d.file_id
               AND mc.credential_kind = 'ACTIVITY_CERTIFICATE'
            JOIN activities a ON a.id = mc.activity_id
            JOIN members m ON m.id = d.member_id
            WHERE a.branch_id IN (:accessibleBranchIds)
              AND m.branch_id NOT IN (:accessibleBranchIds)
              AND (
                  :search = ''
                  OR LOWER(d.title) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%')
                  OR LOWER(COALESCE(m.full_name_km, '')) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%')
                  OR LOWER(COALESCE(m.full_name_en, '')) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%')
              )
              AND d.created_at >= :startDateTime
              AND d.created_at < :endDateTime
            ORDER BY d.created_at DESC, d.id DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM documents d
            JOIN member_credentials mc
                ON mc.member_id = d.member_id
               AND mc.file_id = d.file_id
               AND mc.credential_kind = 'ACTIVITY_CERTIFICATE'
            JOIN activities a ON a.id = mc.activity_id
            JOIN members m ON m.id = d.member_id
            WHERE a.branch_id IN (:accessibleBranchIds)
              AND m.branch_id NOT IN (:accessibleBranchIds)
              AND (
                  :search = ''
                  OR LOWER(d.title) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%')
                  OR LOWER(COALESCE(m.full_name_km, '')) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%')
                  OR LOWER(COALESCE(m.full_name_en, '')) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%')
              )
              AND d.created_at >= :startDateTime
              AND d.created_at < :endDateTime
            """,
            nativeQuery = true
    )
    Page<Document> findCrossBranchCertificateDocumentPage(

            @Param("search")
            String search,

            @Param("startDateTime")
            OffsetDateTime startDateTime,

            @Param("endDateTime")
            OffsetDateTime endDateTime,

            @Param("accessibleBranchIds")
            Set<Long> accessibleBranchIds,

            Pageable pageable
    );

    /*
     * ==========================================================
     * CERTIFICATES RECEIVED FROM OTHER BRANCHES (the inverse of the query
     * above: a member of the current staff's own branch(es) who received
     * an activity certificate from an activity hosted by ANOTHER branch)
     * ==========================================================
     *
     * Same join, WHERE flipped: the ACTIVITY's host branch is now outside
     * the current staff's scope, and the RECIPIENT belongs to one of
     * their own branches.
     */
    @Query(
            value = """
            SELECT d.*
            FROM documents d
            JOIN member_credentials mc
                ON mc.member_id = d.member_id
               AND mc.file_id = d.file_id
               AND mc.credential_kind = 'ACTIVITY_CERTIFICATE'
            JOIN activities a ON a.id = mc.activity_id
            JOIN members m ON m.id = d.member_id
            WHERE a.branch_id NOT IN (:accessibleBranchIds)
              AND m.branch_id IN (:accessibleBranchIds)
              AND (
                  :search = ''
                  OR LOWER(d.title) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%')
                  OR LOWER(COALESCE(m.full_name_km, '')) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%')
                  OR LOWER(COALESCE(m.full_name_en, '')) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%')
              )
              AND d.created_at >= :startDateTime
              AND d.created_at < :endDateTime
            ORDER BY d.created_at DESC, d.id DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM documents d
            JOIN member_credentials mc
                ON mc.member_id = d.member_id
               AND mc.file_id = d.file_id
               AND mc.credential_kind = 'ACTIVITY_CERTIFICATE'
            JOIN activities a ON a.id = mc.activity_id
            JOIN members m ON m.id = d.member_id
            WHERE a.branch_id NOT IN (:accessibleBranchIds)
              AND m.branch_id IN (:accessibleBranchIds)
              AND (
                  :search = ''
                  OR LOWER(d.title) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%')
                  OR LOWER(COALESCE(m.full_name_km, '')) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%')
                  OR LOWER(COALESCE(m.full_name_en, '')) LIKE CONCAT('%', LOWER(CAST(:search AS text)), '%')
              )
              AND d.created_at >= :startDateTime
              AND d.created_at < :endDateTime
            """,
            nativeQuery = true
    )
    Page<Document> findCertificatesReceivedFromOtherBranchesPage(

            @Param("search")
            String search,

            @Param("startDateTime")
            OffsetDateTime startDateTime,

            @Param("endDateTime")
            OffsetDateTime endDateTime,

            @Param("accessibleBranchIds")
            Set<Long> accessibleBranchIds,

            Pageable pageable
    );


    /*
     * ==========================================================
     * DOCUMENTS VISIBLE TO A MEMBER (self-service "My Account" tab)
     * ==========================================================
     *
     * Only documents owned directly by this member (d.memberId =
     * :memberId) — a personal certificate, membership letter, etc.
     *
     * Deliberately excludes activity-owned documents (an activity's
     * uploaded attachment) even for an activity this member joined —
     * those stay visible only on that activity's own detail page, not
     * mixed into the member's personal document list. Also excludes
     * plain branch-owned documents (d.branchId set, d.memberId/
     * d.activityId null) — those stay restricted to the staff-only
     * organizational tab above.
     */
    @Query("""
    SELECT d
    FROM Document d

    WHERE (
        :search = ''

        OR LOWER(d.title)
            LIKE CONCAT('%', LOWER(:search), '%')

        OR LOWER(COALESCE(d.description, ''))
            LIKE CONCAT('%', LOWER(:search), '%')
    )

    AND (
        :typeId IS NULL
        OR d.typeId = :typeId
    )

    AND d.createdAt >= :startDateTime
    AND d.createdAt < :endDateTime

    AND d.memberId = :memberId
    """)
    Page<Document> findVisibleToMemberPage(

            @Param("memberId")
            Long memberId,

            @Param("search")
            String search,

            @Param("typeId")
            Short typeId,

            @Param("startDateTime")
            OffsetDateTime startDateTime,

            @Param("endDateTime")
            OffsetDateTime endDateTime,

            Pageable pageable
    );
}
