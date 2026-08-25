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
 * password is required and becomes the account's real password hash
 * immediately — the account is created ACTIVE and can log in right
 * away with phone/email + this password. Unlike member-linked
 * accounts (see MemberServiceImpl.createPendingUserAccount), a
 * standalone account created here never goes through OTP-based
 * activation: the admin already knows the password, so there's
 * nothing left to verify. OTP activation stays reserved for accounts
 * with a memberId, where the person setting up the account is not
 * the admin.
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
     * Required — becomes the account's real password immediately.
     * See the class-level note above.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must contain at least 6 characters")
    private String password;
}
