package org.example.tnal_youth_backend.document.type.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.document.type.dto.response.DocumentTypeResponse;
import org.example.tnal_youth_backend.document.type.service.DocumentTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({
        "/api/lookups/document-types",
        "/api/document-types"
})
@RequiredArgsConstructor
@Tag(
        name = "C. Member Page - Document-types"
)
public class DocumentTypeController {

    private final DocumentTypeService
            documentTypeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER')")
    public ResponseEntity<List<DocumentTypeResponse>>
    getDocumentTypes() {

        return ResponseEntity.ok(
                documentTypeService
                        .getActiveDocumentTypes()
        );
    }
}
