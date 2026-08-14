package org.example.tnal_youth_backend.authentication.repository;

import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailOrPhone(
            String email,
            String phone
    );

    Optional<User> findByMemberId(
            Long memberId
    );

    // -------------------------
    // Exists
    // -------------------------

    boolean existsByMemberId(
            Long memberId
    );

    boolean existsByPhone(
            String phone
    );

    boolean existsByEmailIgnoreCase(
            String email
    );

    boolean existsByPhoneAndIdNot(
            String phone,
            Long id
    );

    boolean existsByEmailIgnoreCaseAndIdNot(
            String email,
            Long id
    );

    // -------------------------
    // Counts
    // -------------------------

    long countByStatus(
            UserStatus status
    );

    /*
     * "Users" (as opposed to members / branch staff) are accounts
     * whose ROLE is not tied to a branch or member — today that's
     * ADMIN and VIEWER (see the CREATABLE_ROLES set in
     * UserManagementServiceImpl, which is what's actually passed
     * in here).
     *
     * NOTE: this used to filter on memberId IS NULL instead. That
     * turned out to be unreliable — some legacy/seed BRANCH_LEADER
     * accounts (see V324__activate_branch_staff_login_accounts.sql)
     * were created directly via SQL with member_id left NULL, so
     * they slipped through a memberId-based filter. Role is the
     * real source of truth for "is this a branch/member account."
     */
    long countByRoleIn(
            Collection<UserRole> roles
    );

    long countByRoleInAndStatus(
            Collection<UserRole> roles,
            UserStatus status
    );

    // -------------------------
    // Lists
    // -------------------------

    List<User> findAllByOrderByCreatedAtDescIdDesc();

    List<User> findAllByRoleInOrderByCreatedAtDescIdDesc(
            Collection<UserRole> roles
    );


}
