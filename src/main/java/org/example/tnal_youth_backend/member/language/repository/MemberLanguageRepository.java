package org.example.tnal_youth_backend.member.language.repository;

import org.example.tnal_youth_backend.member.language.entity.MemberLanguage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberLanguageRepository
        extends JpaRepository<MemberLanguage, Long> {

    List<MemberLanguage> findAllByMemberIdOrderByIdAsc(
            Long memberId
    );

    Optional<MemberLanguage> findByIdAndMemberId(
            Long id,
            Long memberId
    );

    boolean existsByMemberIdAndLanguageNameIgnoreCase(
            Long memberId,
            String languageName
    );

    boolean existsByMemberIdAndLanguageNameIgnoreCaseAndIdNot(
            Long memberId,
            String languageName,
            Long id
    );
}