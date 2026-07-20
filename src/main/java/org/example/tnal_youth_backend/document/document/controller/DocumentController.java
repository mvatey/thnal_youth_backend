package org.example.tnal_youth_backend.document.document.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.document.document.dto.request.DocumentRequest;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentResponse;
import org.example.tnal_youth_backend.document.document.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    public ResponseEntity<List<DocumentResponse>>
    getAllDocuments() {

        return ResponseEntity.ok(
                documentService.getAllDocuments()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse>
    getDocumentById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                documentService.getDocumentById(id)
        );
    }

    @PostMapping
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