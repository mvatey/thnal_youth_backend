package org.example.tnal_youth_backend.authentication.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.request.SendActivationOtpRequest;
import org.example.tnal_youth_backend.authentication.model.request.SetActivationPasswordRequest;
import org.example.tnal_youth_backend.authentication.model.request.VerifyActivationOtpRequest;
import org.example.tnal_youth_backend.authentication.model.response.ApiResponse;
import org.example.tnal_youth_backend.authentication.service.AccountActivationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/activation")
@RequiredArgsConstructor
@Tag(
        name = "Authentication - Account Activation",
        description = "Activate newly created member accounts"
)
public class AccountActivationController {

    private final AccountActivationService
            accountActivationService;

    /*
     * Sends an activation OTP to the user's email.
     *
     * POST /api/auth/activation/send-otp
     */
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse> sendOtp(
            @Valid
            @RequestBody
            SendActivationOtpRequest request
    ) {
        return ResponseEntity.ok(
                accountActivationService
                        .sendActivationOtp(
                                request
                        )
        );
    }

    /*
     * Checks whether the submitted activation OTP is valid.
     *
     * POST /api/auth/activation/verify-otp
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(
            @Valid
            @RequestBody
            VerifyActivationOtpRequest request
    ) {
        return ResponseEntity.ok(
                accountActivationService
                        .verifyActivationOtp(
                                request
                        )
        );
    }

    /*
     * Verifies the OTP again, creates the first password,
     * and activates the account.
     *
     * POST /api/auth/activation/set-password
     */
    @PostMapping("/set-password")
    public ResponseEntity<ApiResponse> setPassword(
            @Valid
            @RequestBody
            SetActivationPasswordRequest request
    ) {
        return ResponseEntity.ok(
                accountActivationService
                        .setInitialPassword(
                                request
                        )
        );
    }
}