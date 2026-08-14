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
     * DOCUMENTS VISIBLE TO A MEMBER (self-service "My Account" tab)
     * ==========================================================
     *
     * Includes:
     * - documents owned directly by this member (d.memberId = :memberId)
     * - documents owned by an activity this member has actually
     *   joined (an ActivityParticipant row exists for that activity +
     *   member) — e.g. an attachment uploaded to an activity the
     *   member participated in
     *
     * Deliberately does NOT include plain branch-owned documents
     * (d.branchId set, d.memberId/d.activityId null) — those stay
     * restricted to the staff-only organizational tab above.
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

    AND (
        d.memberId = :memberId

        OR (
            d.activityId IS NOT NULL

            AND d.activityId IN (
                SELECT ap.activity.id
                FROM ActivityParticipant ap
                WHERE ap.member.id = :memberId
            )
        )
    )
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
