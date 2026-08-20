package org.example.tnal_youth_backend.authentication.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/*
 * Request payload for an ADMIN creating a new, branch-less user
 * account (used for VIEWER accounts, and any additional ADMIN
 * accounts that don't need to go through the member/branch flow).
 *
 * The created user's member_id is always left NULL by the service
 * layer — this endpoint is not for onboarding members, branch
 * leaders, or secretaries, which are created through their own
 * existing flows elsewhere in the app.
 *
 * No password is collected here. The account is created in
 * PENDING_ACTIVATION status with an unusable placeholder password
 * hash, the same way MemberServiceImpl provisions member/branch-
 * staff logins — the new user sets their own first password
 * through the existing OTP-based activation flow
 * (/auth/activation/send-otp -> verify-otp -> set-password), which
 * is why email is required here: that's how the OTP is delivered.
 */
@Getter
@Setter
public class CreateUserRequest {

    @NotBlank(message = "Khmer full name is required")
    @Size(max = 500, message = "Khmer full name must not exceed 500 characters")
    private String fullNameKm;

    @Size(max = 500, message = "English full name must not exceed 500 characters")
    private String fullNameEn;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[0-9+() -]{6,20}$",
            message = "Phone number format is invalid"
    )
    private String phone;

    @NotBlank(message = "Email is required to send the account activation code")
    @Email(message = "Email format is invalid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    /*
     * Restricted to non-branch-linked roles by the service layer.
     * Today that means ADMIN or VIEWER.
     */
    @NotBlank(message = "Role is required")
    private String role;
}
