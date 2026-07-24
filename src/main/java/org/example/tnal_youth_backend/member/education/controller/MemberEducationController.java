package org.example.tnal_youth_backend.member.education.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.education.dto.request.MemberEducationRequest;
import org.example.tnal_youth_backend.member.education.dto.response.MemberEducationResponse;
import org.example.tnal_youth_backend.member.education.service.MemberEducationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//@Tag(name = "Ed Member Page")
@RequestMapping(
        "/api/members/{memberId}/education"
)
@RequiredArgsConstructor
public class MemberEducationController {

    private final MemberEducationService educationService;

    @GetMapping
    public ResponseEntity<List<MemberEducationResponse>>
    getByMemberId(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                educationService.getByMemberId(memberId)
        );
    }

    @PostMapping
    public ResponseEntity<MemberEducationResponse>
    create(
            @PathVariable Long memberId,

            @Valid
            @RequestBody
            MemberEducationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        educationService.create(
                                memberId,
                                request
                        )
                );
    }

    @PutMapping("/{educationId}")
    public ResponseEntity<MemberEducationResponse>
    update(
            @PathVariable Long memberId,
            @PathVariable Long educationId,

            @Valid
            @RequestBody
            MemberEducationRequest request
    ) {
        return ResponseEntity.ok(
                educationService.update(
                        memberId,
                        educationId,
                        request
                )
        );
    }

    @DeleteMapping("/{educationId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long memberId,
            @PathVariable Long educationId
    ) {
        educationService.delete(
                memberId,
                educationId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}