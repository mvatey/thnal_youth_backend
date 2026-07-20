package org.example.tnal_youth_backend.authentication.repository;

import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    // Authentication methods

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByPhoneOrEmailIgnoreCase(
            String phone,
            String email
    );

    // User CRUD validation methods

    boolean existsByPhone(String phone);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByMemberId(Long memberId);

    boolean existsByPhoneAndIdNot(
            String phone,
            Long id
    );

    boolean existsByEmailIgnoreCaseAndIdNot(
            String email,
            Long id
    );

    boolean existsByMemberIdAndIdNot(
            Long memberId,
            Long id
    );

    List<User> findAllByOrderByCreatedAtDescIdDesc();
}