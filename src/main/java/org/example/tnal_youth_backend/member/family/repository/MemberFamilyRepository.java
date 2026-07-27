package org.example.tnal_youth_backend.member.family.repository;

import org.example.tnal_youth_backend.member.family.entity.MemberFamily;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberFamilyRepository
        extends JpaRepository<MemberFamily, Long> {

    List<MemberFamily> findAllByMemberIdOrderByIdAsc(
            Long memberId
    );

    Optional<MemberFamily> findByIdAndMemberId(
            Long id,
            Long memberId
    );
}