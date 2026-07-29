package org.example.tnal_youth_backend.authentication.repository;

import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

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
    // Lists
    // -------------------------

    List<User> findAllByOrderByCreatedAtDescIdDesc();


}