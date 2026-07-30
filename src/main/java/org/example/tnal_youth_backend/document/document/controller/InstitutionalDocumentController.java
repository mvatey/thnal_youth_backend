package org.example.tnal_youth_backend.document.document.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.document.document.dto.request.DocumentFilterRequest;
import org.example.tnal_youth_backend.document.document.dto.request.InstitutionalDocumentRequest;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentDetailResponse;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentPageResponse;
import org.example.tnal_youth_backend.document.document.service.DocumentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/documents/institutional")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "F. Document Page - institutional"
)
public class InstitutionalDocumentController {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String DEFAULT_SORT_FIELD = "documentDate";

    private final DocumentService documentService;

    /**
     * Returns branch institutional documents for the institution-document UI.
     *
     * Supported filters:
     * - search
     * - branchId
     * - date
     * - page
     * - size
     * - direction
     */
    @GetMapping
    public ResponseEntity<DocumentPageResponse> getInstitutionalDocuments(
            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            @Positive(message = "Branch ID must be positive")
            Long branchId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @RequestParam(defaultValue = "0")
            @PositiveOrZero(message = "Page must be zero or positive")
            int page,

            @RequestParam(defaultValue = "10")
            @Positive(message = "Page size must be positive")
            int size,

            @RequestParam(defaultValue = "DESC")
            Sort.Direction direction
    ) {
        DocumentFilterRequest filter = new DocumentFilterRequest(
                normalizeSearch(search),
                null,
                branchId,
                date,
                null,
                null
        );

        Pageable pageable = createPageable(
                page,
                size,
                direction
        );

        DocumentPageResponse response =
                documentService.getInstitutionalDocuments(
                        filter,
                        pageable
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Returns the full detail of one branch institutional document.
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentDetailResponse> getInstitutionalDocumentById(
            @PathVariable
            @Positive(message = "Document ID must be positive")
            Long documentId
    ) {
        DocumentDetailResponse response =
                documentService.getInstitutionalDocumentById(documentId);

        return ResponseEntity.ok(response);
    }

    /**
     * Creates a branch institutional document.
     *
     * multipart/form-data fields:
     * - branchId
     * - title
     * - description
     * - file
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDetailResponse> createInstitutionalDocument(
            @Valid
            @ModelAttribute
            InstitutionalDocumentRequest.Create request
    ) {
        DocumentDetailResponse response =
                documentService.createInstitutionalDocument(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Updates a branch institutional document.
     *
     * multipart/form-data fields:
     * - branchId
     * - title
     * - description
     * - documentDate
     * - file, optional
     */
    @PutMapping(
            value = "/{documentId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DocumentDetailResponse> updateInstitutionalDocument(
            @PathVariable
            @Positive(message = "Document ID must be positive")
            Long documentId,

            @Valid
            @ModelAttribute
            InstitutionalDocumentRequest.Update request
    ) {
        DocumentDetailResponse response =
                documentService.updateInstitutionalDocument(
                        documentId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Deletes the document and its linked uploaded file.
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteInstitutionalDocument(
            @PathVariable
            @Positive(message = "Document ID must be positive")
            Long documentId
    ) {
        documentService.deleteInstitutionalDocument(documentId);

        return ResponseEntity.noContent().build();
    }

    private Pageable createPageable(
            int page,
            int size,
            Sort.Direction direction
    ) {
        int safeSize = size <= 0
                ? DEFAULT_PAGE_SIZE
                : Math.min(size, MAX_PAGE_SIZE);

        Sort.Direction safeDirection =
                direction == null ? Sort.Direction.DESC : direction;

        Sort sort = Sort.by(
                safeDirection,
                DEFAULT_SORT_FIELD
        ).and(
                Sort.by(
                        safeDirection,
                        "id"
                )
        );

        return PageRequest.of(
                page,
                safeSize,
                sort
        );
    }

    private String normalizeSearch(String search) {
        if (search == null) {
            return null;
        }

        String normalized = search.trim();

        return normalized.isEmpty() ? null : normalized;
    }
}