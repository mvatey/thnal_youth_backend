package org.example.tnal_youth_backend.authentication.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/*
 * Request payload for an ADMIN creating a standalone login account.
 * The account may use any application role. BRANCH_LEADER, SECRETARY,
 * and MEMBER accounts require branchId so their authorization scope is
 * explicit even though member_id remains NULL.
 *
 * The created user's member_id is always left NULL by the service
 * layer. Creating a login here never creates a Member record.
 *
 * password is optional. When omitted, the account is created in
 * PENDING_ACTIVATION status with an unusable placeholder password
 * hash, the same way MemberServiceImpl provisions member/branch-
 * staff logins — the new user sets their own first password through
 * the existing OTP-based activation flow (/auth/activation/send-otp
 * -> verify-otp -> set-password). When an admin does supply a
 * password here, it becomes the account's real password hash right
 * away (so the admin can hand it to the person directly) — the
 * account still starts PENDING_ACTIVATION and still requires the
 * same OTP-based activation before it can log in; the person just
 * already knows what to type at the "set password" step. Email is
 * required either way: it's how the OTP is delivered.
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

    @NotBlank(message = "Role is required")
    private String role;

    /**
     * Required for MEMBER, SECRETARY, and BRANCH_LEADER standalone
     * accounts. Optional for ADMIN and VIEWER.
     */
    private Long branchId;

    /** Required only when role is VIEWER. */
    private String viewerScope;

    /**
     * Optional. When provided, becomes the account's real password
     * immediately instead of the usual unusable placeholder — see the
     * class-level note above.
     */
    @Size(min = 6, message = "Password must contain at least 6 characters")
    private String password;
}
