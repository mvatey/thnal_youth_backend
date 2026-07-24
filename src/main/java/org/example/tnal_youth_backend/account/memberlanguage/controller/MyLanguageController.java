package org.example.tnal_youth_backend.account.memberlanguage.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.memberlanguage.service.MyLanguageService;
import org.example.tnal_youth_backend.member.language.dto.request.MemberLanguageRequest;
import org.example.tnal_youth_backend.member.language.dto.response.MemberLanguageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-account/languages")
@RequiredArgsConstructor
@Tag(
        name = "A. My Account - Languages",
        description = "ភាសា ( My-Account )"
)
public class MyLanguageController {

    private final MyLanguageService myLanguageService;

    /*
     * GET ALL
     * GET /api/my-account/languages
     */

    @GetMapping
    public ResponseEntity<List<MemberLanguageResponse>>
    getMyLanguages() {

        return ResponseEntity.ok(
                myLanguageService.getMyLanguages()
        );
    }

    /*
     * GET ONE
     * GET /api/my-account/languages/{languageId}
     */

    @GetMapping("/{languageId}")
    public ResponseEntity<MemberLanguageResponse>
    getMyLanguageById(
            @PathVariable Long languageId
    ) {
        return ResponseEntity.ok(
                myLanguageService.getMyLanguageById(
                        languageId
                )
        );
    }

    /*
     * CREATE
     * POST /api/my-account/languages
     */

    @PostMapping
    public ResponseEntity<MemberLanguageResponse>
    createMyLanguage(
            @Valid
            @RequestBody
            MemberLanguageRequest request
    ) {
        MemberLanguageResponse response =
                myLanguageService.createMyLanguage(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /*
     * UPDATE
     * PUT /api/my-account/languages/{languageId}
     */

    @PutMapping("/{languageId}")
    public ResponseEntity<MemberLanguageResponse>
    updateMyLanguage(
            @PathVariable Long languageId,

            @Valid
            @RequestBody
            MemberLanguageRequest request
    ) {
        return ResponseEntity.ok(
                myLanguageService.updateMyLanguage(
                        languageId,
                        request
                )
        );
    }

    /*
     * DELETE
     * DELETE /api/my-account/languages/{languageId}
     */

    @DeleteMapping("/{languageId}")
    public ResponseEntity<Void>
    deleteMyLanguage(
            @PathVariable Long languageId
    ) {
        myLanguageService.deleteMyLanguage(
                languageId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}