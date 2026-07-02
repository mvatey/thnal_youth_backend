package org.example.tnal_youth_backend.repository;

import org.example.tnal_youth_backend.model.entity.PasswordResetToken;
import org.example.tnal_youth_backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findTopByUserOrderByCreatedAtDesc(User user);

}