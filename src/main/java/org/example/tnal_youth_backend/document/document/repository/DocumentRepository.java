package org.example.tnal_youth_backend.document.document.repository;

import org.example.tnal_youth_backend.document.document.entity.Document;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DocumentRepository
        extends JpaRepository<Document, Long>,
        JpaSpecificationExecutor<Document> {

    /**
     * Loads any document with the relationships needed for the shared
     * detail response.
     */
    @EntityGraph(attributePaths = {
            "documentType",
            "file",
            "branch",
            "member",
            "activity",
            "uploadedBy"
    })
    @Query("""
            SELECT d
            FROM Document d
            WHERE d.id = :documentId
            """)
    Optional<Document> findDetailedById(
            @Param("documentId") Long documentId
    );

    // =========================================================
    // INSTITUTIONAL DOCUMENT
    // Existing logic unchanged
    // =========================================================

    @EntityGraph(attributePaths = {
            "documentType",
            "file",
            "branch",
            "uploadedBy"
    })
    @Query("""
            SELECT d
            FROM Document d
            JOIN d.documentType dt
            WHERE d.id = :documentId
              AND dt.code = 'BRANCH_DOCUMENT'
              AND d.branchId IS NOT NULL
              AND d.memberId IS NULL
              AND d.activityId IS NULL
            """)
    Optional<Document> findInstitutionalDetailedById(
            @Param("documentId") Long documentId
    );

    // =========================================================
    // MEMBER PERSONAL DOCUMENT
    // =========================================================

    /**
     * Loads one personal document belonging directly to a member.
     *
     * The member's branch is not loaded as a Member relationship because
     * Member currently stores branchId as a scalar field.
     */
    @EntityGraph(attributePaths = {
            "documentType",
            "file",
            "member",
            "uploadedBy"
    })
    @Query("""
            SELECT d
            FROM Document d
            WHERE d.id = :documentId
              AND d.memberId IS NOT NULL
              AND d.branchId IS NULL
              AND d.activityId IS NULL
            """)
    Optional<Document> findMemberDetailedById(
            @Param("documentId") Long documentId
    );

    boolean existsByFileId(
            Long fileId
    );

    boolean existsByFileIdAndIdNot(
            Long fileId,
            Long id
    );
}