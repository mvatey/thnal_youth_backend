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

    Optional<User> findByTelegramChatId(
            Long telegramChatId
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

    // Role-scoped counts/lists are retained for callers that genuinely
    // need them. The admin Users page itself uses the unscoped findAll/count
    // methods because it must display every login account.
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
