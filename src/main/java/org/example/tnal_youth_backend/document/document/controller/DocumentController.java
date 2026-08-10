package org.example.tnal_youth_backend.document.document.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.document.document.dto.request.DocumentRequest;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentResponse;
import org.example.tnal_youth_backend.document.document.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(
        name = "B. Member Page - Documents"
)
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER','MEMBER')")
    public ResponseEntity<List<DocumentResponse>>
    getDocuments(
            @RequestParam(name = "owner_type", required = false) String ownerType,
            @RequestParam(name = "owner_id", required = false) Long ownerId,
            @RequestParam(name = "type_id", required = false) Short typeId,
            @RequestParam(required = false) String search
    ) {

        return ResponseEntity.ok(
                documentService.getDocuments(ownerType, ownerId, typeId, search)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER','MEMBER')")
    public ResponseEntity<DocumentResponse>
    getDocumentById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                documentService.getDocumentById(id)
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER')")
    public ResponseEntity<DocumentResponse>
    createDocument(
            @Valid
            @RequestBody
            DocumentRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        documentService
                                .createDocument(request)
                );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER')")
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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER')")
    public ResponseEntity<Void>
    deleteDocument(
            @PathVariable Long id
    ) {
        documentService.deleteDocument(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}
