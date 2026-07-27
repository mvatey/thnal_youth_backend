package org.example.tnal_youth_backend.member.language.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.language.dto.request.MemberLanguageRequest;
import org.example.tnal_youth_backend.member.language.dto.response.MemberLanguageResponse;
import org.example.tnal_youth_backend.member.language.service.MemberLanguageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members/{memberId}/languages")
@RequiredArgsConstructor
@Tag(
        name = "B. Member Page - Languages",
        description = "Manage languages for a selected member"
)
public class MemberLanguageController {

    private final MemberLanguageService languageService;

    @GetMapping
    public ResponseEntity<List<MemberLanguageResponse>>
    getByMemberId(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                languageService.getByMemberId(memberId)
        );
    }

    @PostMapping
    public ResponseEntity<MemberLanguageResponse> create(
            @PathVariable Long memberId,

            @Valid
            @RequestBody
            MemberLanguageRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        languageService.create(
                                memberId,
                                request
                        )
                );
    }

    @PutMapping("/{languageId}")
    public ResponseEntity<MemberLanguageResponse> update(
            @PathVariable Long memberId,
            @PathVariable Long languageId,

            @Valid
            @RequestBody
            MemberLanguageRequest request
    ) {
        return ResponseEntity.ok(
                languageService.update(
                        memberId,
                        languageId,
                        request
                )
        );
    }

    @DeleteMapping("/{languageId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long memberId,
            @PathVariable Long languageId
    ) {
        languageService.delete(
                memberId,
                languageId
        );

        return ResponseEntity.noContent().build();
    }
}