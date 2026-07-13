package org.example.tnal_youth_backend.member.service;

import org.example.tnal_youth_backend.member.dto.MemberLanguageDto;
import org.example.tnal_youth_backend.member.entity.Member;
import org.example.tnal_youth_backend.member.entity.MemberLanguage;
import org.example.tnal_youth_backend.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.repository.MemberLanguageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberLanguageService {
    private final MemberLanguageRepository languageRepository;
    private final MemberRepository memberRepository;

    public MemberLanguageService(MemberLanguageRepository languageRepository, MemberRepository memberRepository) {
        this.languageRepository = languageRepository;
        this.memberRepository = memberRepository;
    }

    public List<MemberLanguage> getLanguagesByMember(Long memberId) {
        return languageRepository.findByMemberId(memberId);
    }

    public MemberLanguage getLanguageById(Long id) {
        return languageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Language not found"));
    }

    public MemberLanguage createLanguage(MemberLanguageDto dto) {
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        MemberLanguage language = new MemberLanguage();
        language.setMember(member);
        language.setLanguageEn(dto.getLanguageEn());
        language.setLanguageKh(dto.getLanguageKh());
        language.setProficiencyLevel(dto.getProficiencyLevel());

        return languageRepository.save(language);
    }

    public MemberLanguage updateLanguage(Long id, MemberLanguageDto dto) {
        MemberLanguage language = getLanguageById(id);
        language.setLanguageEn(dto.getLanguageEn());
        language.setLanguageKh(dto.getLanguageKh());
        language.setProficiencyLevel(dto.getProficiencyLevel());
        return languageRepository.save(language);
    }

    public void deleteLanguage(Long id) {
        languageRepository.deleteById(id);
    }
}
