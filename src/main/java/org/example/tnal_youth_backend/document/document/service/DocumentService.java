package org.example.tnal_youth_backend.document.document.service;

import org.example.tnal_youth_backend.document.document.dto.request.DocumentFilterRequest;
import org.example.tnal_youth_backend.document.document.dto.request.InstitutionalDocumentRequest;
import org.example.tnal_youth_backend.document.document.dto.request.MemberDocumentRequest;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentDetailResponse;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentPageResponse;
import org.springframework.data.domain.Pageable;

public interface DocumentService {

    // =========================================================
    // INSTITUTIONAL DOCUMENTS
    // =========================================================

    /**
     * Returns paginated institutional documents.
     */
    DocumentPageResponse getInstitutionalDocuments(
            DocumentFilterRequest filter,
            Pageable pageable
    );

    /**
     * Returns one institutional document.
     */
    DocumentDetailResponse getInstitutionalDocumentById(
            Long documentId
    );

    /**
     * Creates a new institutional document.
     */
    DocumentDetailResponse createInstitutionalDocument(
            InstitutionalDocumentRequest.Create request
    );

    /**
     * Updates an existing institutional document.
     */
    DocumentDetailResponse updateInstitutionalDocument(
            Long documentId,
            InstitutionalDocumentRequest.Update request
    );

    /**
     * Deletes an institutional document.
     */
    void deleteInstitutionalDocument(
            Long documentId
    );

    // =========================================================
    // MEMBER PERSONAL DOCUMENTS
    // =========================================================

    /**
     * Returns all member-owned documents required by the Member Documents tab.
     */
    DocumentPageResponse getMemberDocuments(
            DocumentFilterRequest filter,
            Pageable pageable
    );

    /**
     * Returns one member-owned document for preview or download.
     */
    DocumentDetailResponse getMemberDocumentById(
            Long documentId
    );

    /**
     * Creates a member-owned document.
     *
     * The same method supports:
     *
     * 1. Generated member cards, letters, and certificates.
     * 2. Uploaded member personal documents.
     *
     * The service uploads the multipart file, creates the file record,
     * assigns the selected member as the owner, and creates the document.
     *
     * There is intentionally no member-document update or delete operation
     * because the current UI does not provide those actions.
     */
    DocumentDetailResponse createMemberDocument(
            MemberDocumentRequest.Create request
    );
}