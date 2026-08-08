package org.example.tnal_youth_backend.member.language.repository;

import org.example.tnal_youth_backend.member.language.entity.MemberLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberLanguageRepository
        extends JpaRepository<MemberLanguage, Long> {

    List<MemberLanguage>
    findAllByMember_IdOrderByIdAsc(
            Long memberId
    );

    Optional<MemberLanguage>
    findByIdAndMember_Id(
            Long languageId,
            Long memberId
    );

    boolean
    existsByMember_IdAndLanguageNameIgnoreCase(
            Long memberId,
            String languageName
    );

    boolean
    existsByMember_IdAndLanguageNameIgnoreCaseAndIdNot(
            Long memberId,
            String languageName,
            Long languageId
    );

}