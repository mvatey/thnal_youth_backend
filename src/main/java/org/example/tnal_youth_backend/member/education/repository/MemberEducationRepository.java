package org.example.tnal_youth_backend.member.education.repository;

import org.example.tnal_youth_backend.member.education.entity.MemberEducation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberEducationRepository
        extends JpaRepository<MemberEducation, Long> {

    List<MemberEducation>
    findAllByMemberIdOrderByStartDateDescIdDesc(
            Long memberId
    );

    Optional<MemberEducation>
    findByIdAndMemberId(
            Long id,
            Long memberId
    );
}