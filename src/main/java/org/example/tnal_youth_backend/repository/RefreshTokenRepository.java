package org.example.tnal_youth_backend.repository;

import org.example.tnal_youth_backend.model.entity.RefreshToken;
import org.example.tnal_youth_backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(UUID token);

    void deleteByUser(User user);

}