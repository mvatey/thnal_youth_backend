package org.example.tnal_youth_backend.document.type.controller;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.document.type.dto.response.DocumentTypeResponse;
import org.example.tnal_youth_backend.document.type.service.DocumentTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/document-types")
@RequiredArgsConstructor
public class DocumentTypeController {

    private final DocumentTypeService
            documentTypeService;

    @GetMapping
    public ResponseEntity<List<DocumentTypeResponse>>
    getDocumentTypes() {

        return ResponseEntity.ok(
                documentTypeService
                        .getActiveDocumentTypes()
        );
    }
}