package org.example.tnal_youth_backend.account.myaccount.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.myaccount.dto.request.ChangeMyPasswordRequest;
import org.example.tnal_youth_backend.account.myaccount.dto.request.UpdateMyAccountRequest;
import org.example.tnal_youth_backend.account.myaccount.dto.response.MyAccountResponse;
import org.example.tnal_youth_backend.account.myaccount.dto.response.MyAccountSummaryResponse;
import org.example.tnal_youth_backend.account.myaccount.service.MyAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController("legacyMyAccountController")
@RequestMapping("/api/legacy/my-account")
@RequiredArgsConstructor
@Tag(
        name = "A. My Account - my-account",
        description = "My-Account ( My-Account )"
)
@PreAuthorize("isAuthenticated() and !hasRole('ADMIN')")
public class MyAccountController {

    private final MyAccountService myAccountService;

    /*
     * Blue Profile Card + Personal Information Tab
     *
     * GET /api/my-account
     */
    @GetMapping
    public ResponseEntity<MyAccountResponse>
    getMyAccount() {

        return ResponseEntity.ok(
                myAccountService.getMyAccount()
        );
    }

    /*
     * Top Summary Cards
     *
     * GET /api/my-account/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<MyAccountSummaryResponse>
    getMyAccountSummary() {

        return ResponseEntity.ok(
                myAccountService.getMyAccountSummary()
        );
    }

    /*
     * Update Blue Profile Card + Personal Information Tab
     *
     * PUT /api/my-account
     */
    @PutMapping
    public ResponseEntity<MyAccountResponse>
    updateMyAccount(
            @Valid
            @RequestBody
            UpdateMyAccountRequest request
    ) {
        return ResponseEntity.ok(
                myAccountService.updateMyAccount(
                        request
                )
        );
    }

    /*
     * Upload and assign the authenticated member's profile photo.
     * The physical image is stored in uploads/images by FileService.
     */
    @PostMapping(
            value = "/profile-photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<MyAccountResponse> updateProfilePhoto(
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                myAccountService.updateProfilePhoto(file)
        );
    }

    /*
     * PATCH /api/my-account/password
     */
    @PatchMapping("/password")
    public ResponseEntity<Void>
    changeMyPassword(
            @Valid
            @RequestBody
            ChangeMyPasswordRequest request
    ) {
        myAccountService.changeMyPassword(
                request
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}
