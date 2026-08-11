package org.example.tnal_youth_backend.document.document.service;

import org.example.tnal_youth_backend.document.document.dto.request.DocumentRequest;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentPageResponse;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentResponse;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentTypeOptionResponse;
import org.example.tnal_youth_backend.document.document.dto.response.MemberDocumentPageResponse;

import java.time.LocalDate;
import java.util.List;

public interface DocumentService {

    List<DocumentResponse> getAllDocuments();

    List<DocumentResponse> getDocumentsByMemberId(Long memberId);

    DocumentResponse getDocumentById(
            Long id
    );

    DocumentResponse createDocument(
            DocumentRequest request
    );

    DocumentResponse updateDocument(
            Long id,
            DocumentRequest request
    );

    void deleteDocument(
            Long id
    );

    DocumentPageResponse getDocuments(
            int page,
            int size,
            String search,
            Short typeId,
            Long branchId,
            Long memberId,
            Long activityId,
            LocalDate date
    );

    List<DocumentTypeOptionResponse>
    getDocumentTypeOptions();

    MemberDocumentPageResponse getMemberDocuments(
            int page,
            int size,
            String search,
            Short typeId,
            Long branchId,
            LocalDate date
    );
}
