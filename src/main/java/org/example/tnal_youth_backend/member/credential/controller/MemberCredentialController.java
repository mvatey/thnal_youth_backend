package org.example.tnal_youth_backend.member.credential.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialRequest;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialResponse;
import org.example.tnal_youth_backend.member.credential.service.MemberCredentialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members/{memberId}/credentials")
@RequiredArgsConstructor
@Tag(
        name = "B. Member Page - Credentials ",
        description = "Manage credential for a selected member"
)
public class MemberCredentialController {

    private final MemberCredentialService memberCredentialService;

    @GetMapping
    public ResponseEntity<List<MemberCredentialResponse>> getAll(
            @PathVariable Long memberId
    ) {
        List<MemberCredentialResponse> credentials =
                memberCredentialService.getAllByMemberId(memberId);

        return ResponseEntity.ok(credentials);
    }

//    @GetMapping("/{credentialId}")
//    public ResponseEntity<MemberCredentialResponse> getById(
//            @PathVariable Long memberId,
//            @PathVariable Long credentialId
//    ) {
//        MemberCredentialResponse credential =
//                memberCredentialService.getById(
//                        memberId,
//                        credentialId
//                );
//
//        return ResponseEntity.ok(credential);
//    }

    @PostMapping
    public ResponseEntity<MemberCredentialResponse> create(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberCredentialRequest request
    ) {
        MemberCredentialResponse credential =
                memberCredentialService.create(
                        memberId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(credential);
    }

    @PutMapping("/{credentialId}")
    public ResponseEntity<MemberCredentialResponse> update(
            @PathVariable Long memberId,
            @PathVariable Long credentialId,
            @Valid @RequestBody MemberCredentialRequest request
    ) {
        MemberCredentialResponse credential =
                memberCredentialService.update(
                        memberId,
                        credentialId,
                        request
                );

        return ResponseEntity.ok(credential);
    }

    @DeleteMapping("/{credentialId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long memberId,
            @PathVariable Long credentialId
    ) {
        memberCredentialService.delete(
                memberId,
                credentialId
        );

        return ResponseEntity.noContent().build();
    }
}