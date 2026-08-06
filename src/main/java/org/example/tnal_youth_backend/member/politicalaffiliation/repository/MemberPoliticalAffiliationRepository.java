package org.example.tnal_youth_backend.member.politicalaffiliation.repository;

import org.example.tnal_youth_backend.member.politicalaffiliation.entity.MemberPoliticalAffiliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberPoliticalAffiliationRepository
        extends JpaRepository<MemberPoliticalAffiliation, Long> {

    List<MemberPoliticalAffiliation>
    findAllByMember_IdOrderByStartDateDescIdDesc(
            Long memberId
    );

    Optional<MemberPoliticalAffiliation>
    findByIdAndMember_Id(
            Long affiliationId,
            Long memberId
    );
}