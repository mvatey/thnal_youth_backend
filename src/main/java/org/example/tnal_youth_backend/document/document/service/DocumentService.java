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

    /**
     * excludeCrossBranchIssuedCertificates: when true, drops any
     * member-owned document that is an activity certificate whose
     * ACTIVITY was hosted by a different branch than the recipient
     * member's own branch (see DocumentRepository.
     * findCrossBranchCertificateDocumentPage) -- those belong in the
     * "certificates received from other branches" tab instead, not
     * mixed into a plain "my branch's documents" listing. Defaults to
     * false everywhere else this method is already called (myAcc
     * self-service, the organization documents tab), so it changes
     * nothing for them.
     */
    DocumentPageResponse getDocuments(
            int page,
            int size,
            String search,
            Short typeId,
            Long branchId,
            Long memberId,
            Long activityId,
            LocalDate date,
            boolean excludeCrossBranchIssuedCertificates
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

    /**
     * Activity certificates the current staff's own branch(es) issued to
     * another branch's member (see DocumentRepository.
     * findCrossBranchCertificateDocumentPage). Staff/admin/viewer only —
     * a plain MEMBER never calls this. branchId narrows a multi-branch
     * secretary's scope to the one branch currently selected on the
     * sidebar (see BranchContext) instead of every branch they staff —
     * null keeps the old whole-scope behavior.
     */
    DocumentPageResponse getCrossBranchCertificateDocuments(
            int page,
            int size,
            String search,
            LocalDate date,
            Long branchId
    );

    /**
     * The inverse of getCrossBranchCertificateDocuments: activity
     * certificates the current staff's own branch(es) RECEIVED from an
     * activity hosted by another branch (see DocumentRepository.
     * findCertificatesReceivedFromOtherBranchesPage). Staff/admin/viewer
     * only — a plain MEMBER never calls this. branchId narrows the same
     * way as above.
     */
    DocumentPageResponse getCertificatesReceivedFromOtherBranches(
            int page,
            int size,
            String search,
            LocalDate date,
            Long branchId
    );
}
