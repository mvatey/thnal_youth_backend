package org.example.tnal_youth_backend.authentication.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.tnal_youth_backend.common.validation.PasswordPolicy;

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
 * away with phone/email + this password, same as a member-linked
 * account (see MemberServiceImpl.createActiveUserAccount), which
 * instead starts on a shared default password and is required to set
 * a real one on first login (User.mustChangePassword) since the admin
 * setting it up isn't the person who'll actually use it.
 *
 * Phone and email are each fully optional -- a username-only account
 * (neither set) is a deliberate, supported choice, not an incomplete
 * one, since username is always required above and this path never
 * depends on OTP delivery at creation time either way. Password reset
 * for such an account goes through staff (see AuthController's
 * forgot-password flow), not self-service OTP.
 */
@Getter
@Setter
public class CreateUserRequest {

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

    /**
     * Required — becomes the account's real password immediately.
     * See the class-level note above.
     */
    @NotBlank(message = "Password is required")
    @Pattern(regexp = PasswordPolicy.REGEX, message = PasswordPolicy.MESSAGE)
    private String password;
}
