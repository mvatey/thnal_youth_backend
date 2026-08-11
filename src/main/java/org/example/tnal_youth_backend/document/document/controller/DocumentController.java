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
     * - no access to organizational document tab
     */
    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER')"
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
            "hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER','MEMBER')"
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
            "hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER','MEMBER')"
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
            "hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER','MEMBER')"
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