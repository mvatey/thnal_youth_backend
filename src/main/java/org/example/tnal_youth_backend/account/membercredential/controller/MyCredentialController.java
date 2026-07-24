package org.example.tnal_youth_backend.account.membercredential.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.membercredential.service.MyCredentialService;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialRequest;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-account/credentials")
@RequiredArgsConstructor
@Tag(
        name = "A. My Account - Credentials",
        description = "លិខិតបញ្ជាក់សមត្ថភាព (my - account )"
)
public class MyCredentialController {

    private final MyCredentialService myCredentialService;

    /*
     * GET /api/my-account/credentials
     */
    @GetMapping
    public ResponseEntity<List<MemberCredentialResponse>>
    getMyCredentials() {

        return ResponseEntity.ok(
                myCredentialService.getMyCredentials()
        );
    }

    /*
     * GET /api/my-account/credentials/{credentialId}
     */
    @GetMapping("/{credentialId}")
    public ResponseEntity<MemberCredentialResponse>
    getMyCredentialById(
            @PathVariable Long credentialId
    ) {
        return ResponseEntity.ok(
                myCredentialService.getMyCredentialById(
                        credentialId
                )
        );
    }

    /*
     * POST /api/my-account/credentials
     */
    @PostMapping
    public ResponseEntity<MemberCredentialResponse>
    createMyCredential(
            @Valid
            @RequestBody
            MemberCredentialRequest request
    ) {
        MemberCredentialResponse response =
                myCredentialService.createMyCredential(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /*
     * PUT /api/my-account/credentials/{credentialId}
     */
    @PutMapping("/{credentialId}")
    public ResponseEntity<MemberCredentialResponse>
    updateMyCredential(
            @PathVariable Long credentialId,

            @Valid
            @RequestBody
            MemberCredentialRequest request
    ) {
        return ResponseEntity.ok(
                myCredentialService.updateMyCredential(
                        credentialId,
                        request
                )
        );
    }

    /*
     * DELETE /api/my-account/credentials/{credentialId}
     */
    @DeleteMapping("/{credentialId}")
    public ResponseEntity<Void>
    deleteMyCredential(
            @PathVariable Long credentialId
    ) {
        myCredentialService.deleteMyCredential(
                credentialId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}