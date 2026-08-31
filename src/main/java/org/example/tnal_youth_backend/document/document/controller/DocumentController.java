package org.example.tnal_youth_backend.document.document.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.document.document.dto.request.DocumentRequest;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentPageResponse;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentResponse;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentTypeOptionResponse;
import org.example.tnal_youth_backend.document.document.dto.response.MemberDocumentPageResponse;
import org.example.tnal_youth_backend.document.document.service.DocumentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(
        name = "B. Member Page - Documents"
)
public class DocumentController {

    private final DocumentService documentService;

    /*
     * =========================================================
     * ORGANIZATIONAL DOCUMENT TAB
     * =========================================================
     *
     * ADMIN:
     * - view all organizational documents
     *
     * SECRETARY / BRANCH_LEADER:
     * - view documents in accessible branch scope
     *
     * MEMBER:
     * - no access to the branch-wide organizational list, but the
     *   service layer still serves this same endpoint scoped to just
     *   their own documents (see DocumentServiceImpl.getDocuments) —
     *   this is what "My Account -> Documents" calls for any
     *   authenticated role. An activity's own attachments are NOT
     *   included here even for an activity the member joined — those
     *   stay visible only on that activity's own detail page.
     */
    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER','MEMBER','VIEWER')"
    )
    public ResponseEntity<DocumentPageResponse>
    getDocuments(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "")
            String search,

            @RequestParam(required = false)
            Short typeId,

            @RequestParam(required = false)
            Long branchId,

            @RequestParam(required = false)
            Long memberId,

            @RequestParam(required = false)
            Long activityId,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate date
    ) {
        return ResponseEntity.ok(
                documentService.getDocuments(
                        page,
                        size,
                        search,
                        typeId,
                        branchId,
                        memberId,
                        activityId,
                        date
                )
        );
    }


    /*
     * =========================================================
     * MEMBER DOCUMENT TAB
     * =========================================================
     *
     * ADMIN:
     * - view all member documents
     *
     * SECRETARY / BRANCH_LEADER:
     * - view member documents inside accessible branch scope
     *
     * MEMBER:
     * - view only documents belonging to their own member ID
     */
    @GetMapping("/member-documents")
    @PreAuthorize(
            "hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER','MEMBER','VIEWER')"
    )
    public ResponseEntity<MemberDocumentPageResponse>
    getMemberDocuments(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "")
            String search,

            @RequestParam(required = false)
            Short typeId,

            @RequestParam(required = false)
            Long branchId,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate date
    ) {
        return ResponseEntity.ok(
                documentService.getMemberDocuments(
                        page,
                        size,
                        search,
                        typeId,
                        branchId,
                        date
                )
        );
    }


    /*
     * =========================================================
     * CROSS-BRANCH CERTIFICATE TAB
     * =========================================================
     *
     * Activity certificates the current staff's own branch(es) issued to
     * another branch's member (see DocumentService.
     * getCrossBranchCertificateDocuments) -- these never appear in the
     * plain member-documents tab above, since that one is scoped to the
     * RECIPIENT's own branch, not the branch that actually issued the
     * certificate.
     */
    @GetMapping("/member-documents/cross-branch-certificates")
    @PreAuthorize(
            "hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER','VIEWER')"
    )
    public ResponseEntity<DocumentPageResponse>
    getCrossBranchCertificateDocuments(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "")
            String search,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate date
    ) {
        return ResponseEntity.ok(
                documentService.getCrossBranchCertificateDocuments(
                        page,
                        size,
                        search,
                        date
                )
        );
    }


    /*
     * =========================================================
     * CERTIFICATES RECEIVED FROM OTHER BRANCHES TAB
     * =========================================================
     *
     * The inverse of the cross-branch tab above: activity certificates a
     * member of the current staff's own branch(es) RECEIVED from an
     * activity hosted by another branch.
     */
    @GetMapping("/member-documents/received-from-other-branches")
    @PreAuthorize(
            "hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER','VIEWER')"
    )
    public ResponseEntity<DocumentPageResponse>
    getCertificatesReceivedFromOtherBranches(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "")
            String search,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate date
    ) {
        return ResponseEntity.ok(
                documentService.getCertificatesReceivedFromOtherBranches(
                        page,
                        size,
                        search,
                        date
                )
        );
    }


    /*
     * =========================================================
     * DOCUMENT TYPE OPTIONS
     * =========================================================
     *
     * Used by:
     * - organizational document filter
     * - member document filter
     * - upload document form
     */
    @GetMapping("/type-options")
    @PreAuthorize(
            "hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER','MEMBER','VIEWER')"
    )
    public ResponseEntity<List<DocumentTypeOptionResponse>>
    getDocumentTypeOptions() {
        return ResponseEntity.ok(
                documentService.getDocumentTypeOptions()
        );
    }


    /*
     * =========================================================
     * GET ONE DOCUMENT
     * =========================================================
     *
     * Access is also checked inside the service.
     *
     * ADMIN:
     * - any accessible document
     *
     * SECRETARY / BRANCH_LEADER:
     * - document inside accessible branch scope
     *
     * MEMBER:
     * - only their own member document
     */
    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER','MEMBER','VIEWER')"
    )
    public ResponseEntity<DocumentResponse>
    getDocumentById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                documentService.getDocumentById(
                        id
                )
        );
    }


    /*
     * =========================================================
     * CREATE DOCUMENT
     * =========================================================
     *
     * SECRETARY / BRANCH_LEADER only.
     *
     * Can create:
     * - branch-owned document
     * - member-owned document
     * - activity-owned document
     *
     * Owner scope is validated inside the service.
     */
    @PostMapping
    @PreAuthorize(
            "hasAnyRole('SECRETARY','BRANCH_LEADER')"
    )
    public ResponseEntity<DocumentResponse>
    createDocument(
            @Valid
            @RequestBody
            DocumentRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        documentService.createDocument(
                                request
                        )
                );
    }


    /*
     * =========================================================
     * UPDATE DOCUMENT
     * =========================================================
     *
     * SECRETARY / BRANCH_LEADER only.
     *
     * Service validates:
     * - access to existing document
     * - access to requested new owner
     */
    @PutMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('SECRETARY','BRANCH_LEADER')"
    )
    public ResponseEntity<DocumentResponse>
    updateDocument(

            @PathVariable Long id,

            @Valid
            @RequestBody
            DocumentRequest request
    ) {
        return ResponseEntity.ok(
                documentService.updateDocument(
                        id,
                        request
                )
        );
    }


    /*
     * =========================================================
     * DELETE DOCUMENT
     * =========================================================
     *
     * SECRETARY / BRANCH_LEADER only.
     *
     * Existing document access is validated
     * inside the service before deleting.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('SECRETARY','BRANCH_LEADER')"
    )
    public ResponseEntity<Void>
    deleteDocument(
            @PathVariable Long id
    ) {
        documentService.deleteDocument(
                id
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}
