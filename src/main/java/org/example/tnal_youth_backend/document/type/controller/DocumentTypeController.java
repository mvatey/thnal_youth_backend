package org.example.tnal_youth_backend.document.type.controller;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.document.type.dto.response.DocumentTypeResponse;
import org.example.tnal_youth_backend.document.type.enums.DocumentScope;
import org.example.tnal_youth_backend.document.type.service.DocumentTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/document-types")
@RequiredArgsConstructor
public class DocumentTypeController {

    private final DocumentTypeService documentTypeService;

    /**
     * Get all active document types.
     *
     * Example:
     *
     * GET /api/document-types
     */
    @GetMapping
    public ResponseEntity<List<DocumentTypeResponse>>
    getActiveDocumentTypes(
            @RequestParam(
                    name = "scope",
                    required = false
            )
            String scope
    ) {
        if (scope == null || scope.isBlank()) {
            return ResponseEntity.ok(
                    documentTypeService
                            .getActiveDocumentTypes()
            );
        }

        DocumentScope documentScope =
                parseScope(scope);

        return ResponseEntity.ok(
                documentTypeService
                        .getActiveDocumentTypesByScope(
                                documentScope
                        )
        );
    }

    /**
     * Get active institutional document types.
     *
     * GET /api/document-types/institutional
     */
    @GetMapping("/institutional")
    public ResponseEntity<List<DocumentTypeResponse>>
    getInstitutionalDocumentTypes() {
        return ResponseEntity.ok(
                documentTypeService
                        .getActiveInstitutionalTypes()
        );
    }

    /**
     * Get active member document types.
     *
     * GET /api/document-types/member
     */
    @GetMapping("/member")
    public ResponseEntity<List<DocumentTypeResponse>>
    getMemberDocumentTypes() {
        return ResponseEntity.ok(
                documentTypeService
                        .getActiveMemberTypes()
        );
    }

    /**
     * Get one active document type.
     *
     * GET /api/document-types/1
     */
    @GetMapping("/{typeId}")
    public ResponseEntity<DocumentTypeResponse>
    getDocumentTypeById(
            @PathVariable Short typeId
    ) {
        return ResponseEntity.ok(
                documentTypeService
                        .getActiveDocumentTypeById(typeId)
        );
    }

    private DocumentScope parseScope(
            String scope
    ) {
        try {
            return DocumentScope.valueOf(
                    scope.trim()
                            .toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Unsupported document scope: "
                            + scope
                            + ". Supported values are INSTITUTIONAL and MEMBER"
            );
        }
    }
}