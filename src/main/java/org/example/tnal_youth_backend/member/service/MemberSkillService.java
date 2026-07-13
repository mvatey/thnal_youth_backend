package org.example.tnal_youth_backend.member.service;

import org.example.tnal_youth_backend.member.dto.MemberSkillDto;
import org.example.tnal_youth_backend.member.entity.Member;
import org.example.tnal_youth_backend.member.entity.MemberSkill;
import org.example.tnal_youth_backend.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.repository.MemberSkillRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberSkillService {
    private final MemberSkillRepository skillRepository;
    private final MemberRepository memberRepository;

    public MemberSkillService(MemberSkillRepository skillRepository, MemberRepository memberRepository) {
        this.skillRepository = skillRepository;
        this.memberRepository = memberRepository;
    }

    public List<MemberSkill> getSkillsByMember(Long memberId) {
        return skillRepository.findByMemberId(memberId);
    }

    public MemberSkill getSkillById(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill not found"));
    }

    public MemberSkill createSkill(MemberSkillDto dto) {
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        MemberSkill skill = new MemberSkill();
        skill.setMember(member);
        skill.setLanguageEn(dto.getLanguageEn());
        skill.setLanguageKh(dto.getLanguageKh());
        skill.setListeningLevel(dto.getListeningLevel());
        skill.setReadingLevel(dto.getReadingLevel());
        skill.setSpeakingLevel(dto.getSpeakingLevel());
        skill.setWritingLevel(dto.getWritingLevel());
        skill.setSkill(dto.getSkill());
        skill.setCulturalLevel(dto.getCulturalLevel());

        return skillRepository.save(skill);
    }

    public MemberSkill updateSkill(Long id, MemberSkillDto dto) {
        MemberSkill skill = getSkillById(id);
        skill.setLanguageEn(dto.getLanguageEn());
        skill.setLanguageKh(dto.getLanguageKh());
        skill.setListeningLevel(dto.getListeningLevel());
        skill.setReadingLevel(dto.getReadingLevel());
        skill.setSpeakingLevel(dto.getSpeakingLevel());
        skill.setWritingLevel(dto.getWritingLevel());
        skill.setSkill(dto.getSkill());
        skill.setCulturalLevel(dto.getCulturalLevel());
        return skillRepository.save(skill);
    }

    public void deleteSkill(Long id) {
        skillRepository.deleteById(id);
    }
}
