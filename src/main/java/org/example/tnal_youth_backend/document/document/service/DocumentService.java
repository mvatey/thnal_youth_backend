package org.example.tnal_youth_backend.document.document.service;

import org.example.tnal_youth_backend.document.document.dto.request.DocumentFilterRequest;
import org.example.tnal_youth_backend.document.document.dto.request.InstitutionalDocumentRequest;
import org.example.tnal_youth_backend.document.document.dto.request.MemberDocumentRequest;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentDetailResponse;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentPageResponse;
import org.springframework.data.domain.Pageable;

public interface DocumentService {

    DocumentPageResponse getInstitutionalDocuments(
            DocumentFilterRequest filter,
            Pageable pageable
    );

    DocumentDetailResponse getInstitutionalDocumentById(
            Long documentId
    );

    DocumentDetailResponse createInstitutionalDocument(
            InstitutionalDocumentRequest request,
            Long uploadedById
    );

    DocumentDetailResponse updateInstitutionalDocument(
            Long documentId,
            InstitutionalDocumentRequest request
    );

    void deleteInstitutionalDocument(
            Long documentId
    );

    DocumentPageResponse getMemberDocuments(
            DocumentFilterRequest filter,
            Pageable pageable
    );

    DocumentPageResponse getMemberDocumentsByMemberId(
            Long memberId,
            DocumentFilterRequest filter,
            Pageable pageable
    );

    DocumentDetailResponse getMemberDocumentById(
            Long memberId,
            Long documentId
    );

    DocumentDetailResponse createMemberDocument(
            Long memberId,
            MemberDocumentRequest request
    );

    DocumentDetailResponse updateMemberDocument(
            Long memberId,
            Long documentId,
            MemberDocumentRequest request
    );

    void deleteMemberDocument(
            Long memberId,
            Long documentId
    );
}