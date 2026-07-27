package org.example.tnal_youth_backend.member.politicalaffiliation.repository;

import org.example.tnal_youth_backend.member.politicalaffiliation.entity.MemberPoliticalAffiliation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberPoliticalAffiliationRepository
        extends JpaRepository<MemberPoliticalAffiliation, Long> {

    List<MemberPoliticalAffiliation>
    findAllByMemberIdOrderByIdAsc(
            Long memberId
    );

    Optional<MemberPoliticalAffiliation>
    findByIdAndMemberId(
            Long affiliationId,
            Long memberId
    );
}