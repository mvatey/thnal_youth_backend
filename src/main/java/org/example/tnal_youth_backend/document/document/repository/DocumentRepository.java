package org.example.tnal_youth_backend.document.document.repository;

import org.example.tnal_youth_backend.document.document.entity.Document;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface DocumentRepository
        extends JpaRepository<Document, Long>,
        JpaSpecificationExecutor<Document> {

    /**
     * Loads a document with all relationships needed for its detail response.
     */
    @EntityGraph(
            attributePaths = {
                    "documentType",
                    "file",
                    "branch",
                    "member",
                    "member.branch",
                    "activity",
                    "uploadedBy"
            }
    )
    @Query("""
            SELECT d
            FROM Document d
            WHERE d.id = :documentId
            """)
    Optional<Document> findDetailedById(
            @Param("documentId") Long documentId
    );

    /**
     * Loads one institutional document restricted to institutional type codes.
     */
    @EntityGraph(
            attributePaths = {
                    "documentType",
                    "file",
                    "branch",
                    "activity",
                    "uploadedBy"
            }
    )
    @Query("""
            SELECT d
            FROM Document d
            JOIN d.documentType dt
            WHERE d.id = :documentId
              AND dt.code IN :typeCodes
            """)
    Optional<Document> findInstitutionalDetailedById(
            @Param("documentId") Long documentId,
            @Param("typeCodes") Collection<String> typeCodes
    );

    /**
     * Loads one document belonging to a specific member.
     */
    @EntityGraph(
            attributePaths = {
                    "documentType",
                    "file",
                    "member",
                    "member.branch",
                    "activity",
                    "uploadedBy"
            }
    )
    @Query("""
            SELECT d
            FROM Document d
            JOIN d.documentType dt
            WHERE d.id = :documentId
              AND d.memberId = :memberId
              AND dt.code IN :typeCodes
            """)
    Optional<Document> findMemberDetailedById(
            @Param("memberId") Long memberId,
            @Param("documentId") Long documentId,
            @Param("typeCodes") Collection<String> typeCodes
    );

    /**
     * Checks whether a physical file is already linked to a document.
     */
    boolean existsByFileId(Long fileId);

    /**
     * Checks whether a member already has a document with the given type code.
     */
    @Query("""
            SELECT CASE
                       WHEN COUNT(d) > 0 THEN true
                       ELSE false
                   END
            FROM Document d
            JOIN d.documentType dt
            WHERE d.memberId = :memberId
              AND dt.code = :typeCode
            """)
    boolean existsMemberDocumentByTypeCode(
            @Param("memberId") Long memberId,
            @Param("typeCode") String typeCode
    );
}