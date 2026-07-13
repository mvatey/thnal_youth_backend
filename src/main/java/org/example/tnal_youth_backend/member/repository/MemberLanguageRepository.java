package org.example.tnal_youth_backend.member.repository;

import org.example.tnal_youth_backend.member.entity.MemberLanguage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberLanguageRepository extends JpaRepository<MemberLanguage, Long> {
    List<MemberLanguage> findByMemberId(Long memberId);
}
