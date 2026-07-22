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

    /*
     * Used by JwtAuthenticationFilter,
     * AuthServiceImpl and ForgotPasswordServiceImpl.
     */
    Optional<User> findByEmailOrPhone(
            String email,
            String phone
    );

    /*
     * You can keep this existing method if another class uses it.
     */
    Optional<User> findByPhoneOrEmailIgnoreCase(
            String phone,
            String email
    );

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