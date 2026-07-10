package org.example.tnal_youth_backend.member.repository;

import org.example.tnal_youth_backend.member.entity.MemberPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberPositionRepository extends JpaRepository<MemberPosition, Long> {
    Optional<MemberPosition> findByCode(String code);
}

