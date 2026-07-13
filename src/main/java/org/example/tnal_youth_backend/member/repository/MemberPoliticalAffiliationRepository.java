package org.example.tnal_youth_backend.member.repository;

import org.example.tnal_youth_backend.member.entity.MemberPoliticalAffiliation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MemberPoliticalAffiliationRepository extends JpaRepository<MemberPoliticalAffiliation, Long> {
    List<MemberPoliticalAffiliation> findByMemberId(Long memberId);
}
