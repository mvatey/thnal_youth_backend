package org.example.tnal_youth_backend.authentication.model.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.tnal_youth_backend.common.validation.PasswordPolicy;

/*
 * Request payload for an ADMIN editing an existing standalone login
 * account (member_id IS NULL) — see CreateUserRequest for how these
 * accounts are created. Editing a member-linked account is handled
 * through the Member Page's own personal-info flow instead, not here.
 *
 * password is optional, same as at creation — leave it blank to keep
 * the account's current password unchanged.
 *
 * Phone and email are each optional, but at least one is required —
 * matches CreateUserRequest, so a user created with only one of them
 * doesn't immediately fail validation on its next edit.
 */
@Getter
@Setter
public class UpdateUserRequest {

    @NotBlank(message = "Khmer full name is required")
    @Size(max = 500, message = "Khmer full name must not exceed 500 characters")
    private String fullNameKm;

    @Size(max = 500, message = "English full name must not exceed 500 characters")
    private String fullNameEn;

    @NotBlank(message = "Username is required")
    @Size(max = 255, message = "Username must not exceed 255 characters")
    private String username;

    @Pattern(
            regexp = "^$|^[0-9+() -]{6,20}$",
            message = "Phone number format is invalid"
    )
    private String phone;

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

    /** Optional — leave blank to keep the current password unchanged. */
    @Pattern(regexp = PasswordPolicy.REGEX_OR_BLANK, message = PasswordPolicy.MESSAGE)
    private String password;

    /**
     * Optional. Lets an admin activate/deactivate an account they're not
     * currently using — ACTIVE or INACTIVE only. Leave blank to keep the
     * account's current status unchanged (e.g. PENDING_ACTIVATION or LOCKED
     * stay as-is, since those are system-managed, not admin-toggled).
     */
    private String status;

    @AssertTrue(message = "Either phone number or email is required")
    public boolean isPhoneOrEmailPresent() {
        return (phone != null && !phone.isBlank())
                || (email != null && !email.isBlank());
    }
}
