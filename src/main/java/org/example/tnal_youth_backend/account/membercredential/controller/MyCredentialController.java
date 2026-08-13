package org.example.tnal_youth_backend.account.membercredential.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.membercredential.service.MyCredentialService;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialRequest;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-account/credentials")
@RequiredArgsConstructor
@Tag(
        name = "A. My Account - Credentials",
        description = "លិខិតបញ្ជាក់សមត្ថភាព (my - account )"
)
@PreAuthorize("isAuthenticated() and !hasRole('ADMIN')")
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

}
