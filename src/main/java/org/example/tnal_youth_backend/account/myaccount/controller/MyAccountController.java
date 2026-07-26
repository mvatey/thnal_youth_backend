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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/my-account")
@RequiredArgsConstructor
@Tag(
        name = "A. My Account - my-account",
        description = "My-Account ( My-Account )"
)
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