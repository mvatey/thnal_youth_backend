package org.example.tnal_youth_backend.document.option.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.document.option.dto.DocumentOptionResponse;
import org.example.tnal_youth_backend.document.option.service.DocumentOptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lookups/document-options")
@RequiredArgsConstructor
@Tag(name = "A. Lookup Options - Documents")
@PreAuthorize("hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER','MEMBER')")
public class DocumentOptionController {

    private final DocumentOptionService service;

    @GetMapping
    public ResponseEntity<List<DocumentOptionResponse>> getOptions(
            @RequestParam(required = false) String category
    ) {
        return ResponseEntity.ok(service.getActiveOptions(category));
    }

    @GetMapping("/fonts")
    public ResponseEntity<List<DocumentOptionResponse>> getFonts() {
        return ResponseEntity.ok(service.getActiveOptions("FONT"));
    }

    @GetMapping("/languages")
    public ResponseEntity<List<DocumentOptionResponse>> getLanguages() {
        return ResponseEntity.ok(service.getActiveOptions("LANGUAGE"));
    }

    @GetMapping("/card-sizes")
    public ResponseEntity<List<DocumentOptionResponse>> getCardSizes() {
        return ResponseEntity.ok(service.getActiveOptions("CARD_SIZE"));
    }
}
