package org.example.tnal_youth_backend.member.repository;

import org.example.tnal_youth_backend.member.entity.MemberFamily;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberFamilyRepository extends JpaRepository<MemberFamily, Long> {
    List<MemberFamily> findByMemberId(Long memberId);
}
