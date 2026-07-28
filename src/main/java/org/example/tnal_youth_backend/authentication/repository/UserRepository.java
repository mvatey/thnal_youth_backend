package org.example.tnal_youth_backend.authentication.repository;

import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository
        extends JpaRepository<User, Long> {

    /*
     * Authentication
     */

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByEmailOrPhone(
            String email,
            String phone
    );

    Optional<User> findByPhoneOrEmailIgnoreCase(
            String phone,
            String email
    );

    /*
     * Member account relationship
     */

    Optional<User> findByMemberId(Long memberId);

    boolean existsByMemberId(Long memberId);

    /*
     * Create validation
     */

    boolean existsByPhone(String phone);

    boolean existsByEmailIgnoreCase(String email);

    /*
     * Update validation
     */

    boolean existsByPhoneAndIdNot(
            String phone,
            Long id
    );

    boolean existsByEmailIgnoreCaseAndIdNot(
            String email,
            Long id
    );

    /*
     * User listing
     */

    List<User> findAllByOrderByCreatedAtDescIdDesc();
}