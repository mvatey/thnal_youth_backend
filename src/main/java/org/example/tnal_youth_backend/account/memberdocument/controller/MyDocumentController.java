package org.example.tnal_youth_backend.account.memberdocument.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.memberdocument.dto.response.MyDocumentResponse;
import org.example.tnal_youth_backend.account.memberdocument.dto.response.MyDocumentSummaryResponse;
import org.example.tnal_youth_backend.account.memberdocument.service.MyDocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-account/documents")
@RequiredArgsConstructor
@Tag(
        name = "My Account - Documents",
        description = " ឯកសារ (my - account ) "
)
public class MyDocumentController {

    private final MyDocumentService myDocumentService;

    /*
     * GET /api/my-account/documents
     */
    @GetMapping
    public ResponseEntity<List<MyDocumentResponse>>
    getMyDocuments() {

        return ResponseEntity.ok(
                myDocumentService.getMyDocuments()
        );
    }

    /*
     * GET /api/my-account/documents/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<MyDocumentSummaryResponse>
    getMyDocumentSummary() {

        return ResponseEntity.ok(
                myDocumentService.getMyDocumentSummary()
        );
    }

    /*
     * GET /api/my-account/documents/{documentId}
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<MyDocumentResponse>
    getMyDocumentById(
            @PathVariable Long documentId
    ) {
        return ResponseEntity.ok(
                myDocumentService.getMyDocumentById(
                        documentId
                )
        );
    }
}